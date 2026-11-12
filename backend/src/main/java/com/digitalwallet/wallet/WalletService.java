package com.digitalwallet.wallet;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Locale;

import com.digitalwallet.error.ApiException;
import com.digitalwallet.transaction.Transaction;
import com.digitalwallet.transaction.TransactionMapper;
import com.digitalwallet.transaction.TransactionRepository;
import com.digitalwallet.transaction.TransactionStatus;
import com.digitalwallet.transaction.TransactionType;
import com.digitalwallet.user.CurrentUserService;
import com.digitalwallet.user.User;
import com.digitalwallet.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletService {

    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public WalletService(
            CurrentUserService currentUserService,
            UserRepository userRepository,
            WalletRepository walletRepository,
            TransactionRepository transactionRepository) {
        this.currentUserService = currentUserService;
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public WalletResponse getMyWallet() {
        Wallet wallet = myWallet();
        return new WalletResponse(wallet.getId(), wallet.getBalance());
    }

    @Transactional
    public MoneyMovementResponse deposit(AmountRequest request) {
        BigDecimal amount = normalize(request.amount());
        Wallet wallet = lockMyWallet();
        wallet.setBalance(scaled(wallet.getBalance().add(amount)));
        Transaction transaction = record(TransactionType.DEPOSIT, amount, null, wallet);
        return new MoneyMovementResponse(wallet.getBalance(), TransactionMapper.toResponse(transaction));
    }

    @Transactional
    public MoneyMovementResponse withdraw(AmountRequest request) {
        BigDecimal amount = normalize(request.amount());
        Wallet wallet = lockMyWallet();
        requireFunds(wallet, amount);
        wallet.setBalance(scaled(wallet.getBalance().subtract(amount)));
        Transaction transaction = record(TransactionType.WITHDRAWAL, amount, wallet, null);
        return new MoneyMovementResponse(wallet.getBalance(), TransactionMapper.toResponse(transaction));
    }

    @Transactional
    public MoneyMovementResponse transfer(TransferRequest request) {
        BigDecimal amount = normalize(request.amount());
        User sender = currentUserService.requireUser();
        String recipientEmail = request.recipientEmail().trim().toLowerCase(Locale.ROOT);
        if (sender.getEmail().equalsIgnoreCase(recipientEmail)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "You cannot send money to yourself");
        }

        User recipient = userRepository.findByEmail(recipientEmail)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No account exists for that email"));
        if (sender.getId().equals(recipient.getId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "You cannot send money to yourself");
        }

        long firstUserId = Math.min(sender.getId(), recipient.getId());
        long secondUserId = Math.max(sender.getId(), recipient.getId());
        Wallet first = lockWallet(firstUserId, recipient.getId());
        Wallet second = lockWallet(secondUserId, recipient.getId());
        Wallet senderWallet = sender.getId() == firstUserId ? first : second;
        Wallet recipientWallet = senderWallet == first ? second : first;

        requireFunds(senderWallet, amount);
        senderWallet.setBalance(scaled(senderWallet.getBalance().subtract(amount)));
        recipientWallet.setBalance(scaled(recipientWallet.getBalance().add(amount)));

        Transaction transaction = record(TransactionType.TRANSFER, amount, senderWallet, recipientWallet);
        return new MoneyMovementResponse(senderWallet.getBalance(), TransactionMapper.toResponse(transaction));
    }

    private Wallet myWallet() {
        User user = currentUserService.requireUser();
        return walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Wallet not found"));
    }

    private Wallet lockMyWallet() {
        User user = currentUserService.requireUser();
        return walletRepository.findByUserIdForUpdate(user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Wallet not found"));
    }

    private Wallet lockWallet(long userId, Long recipientId) {
        return walletRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        userId == recipientId ? "That account does not have a wallet" : "Wallet not found"));
    }

    private void requireFunds(Wallet wallet, BigDecimal amount) {
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Insufficient balance");
        }
    }

    private Transaction record(TransactionType type, BigDecimal amount, Wallet sender, Wallet receiver) {
        Transaction transaction = new Transaction();
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setSenderWallet(sender);
        transaction.setReceiverWallet(receiver);
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setCreatedAt(Instant.now());
        transaction.setFlagged(false);
        return transactionRepository.save(transaction);
    }

    private static BigDecimal normalize(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Amount must be above zero");
        }
        try {
            return amount.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Amount must have at most 2 decimal places");
        }
    }

    private static BigDecimal scaled(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_EVEN);
    }
}
