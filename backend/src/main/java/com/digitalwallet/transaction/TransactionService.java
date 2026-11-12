package com.digitalwallet.transaction;

import java.util.List;

import com.digitalwallet.error.ApiException;
import com.digitalwallet.user.CurrentUserService;
import com.digitalwallet.user.User;
import com.digitalwallet.wallet.Wallet;
import com.digitalwallet.wallet.WalletRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

    private final CurrentUserService currentUserService;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public TransactionService(
            CurrentUserService currentUserService,
            WalletRepository walletRepository,
            TransactionRepository transactionRepository) {
        this.currentUserService = currentUserService;
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> myHistory() {
        User user = currentUserService.requireUser();
        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Wallet not found"));
        return transactionRepository.findByWalletIdNewestFirst(wallet.getId()).stream()
                .map(TransactionMapper::toResponse)
                .toList();
    }
}
