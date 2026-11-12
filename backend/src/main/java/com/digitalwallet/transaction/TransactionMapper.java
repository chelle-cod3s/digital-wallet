package com.digitalwallet.transaction;

import com.digitalwallet.user.User;
import com.digitalwallet.wallet.Wallet;

public final class TransactionMapper {

    private TransactionMapper() {
    }

    public static TransactionResponse toResponse(Transaction transaction) {
        Wallet sender = transaction.getSenderWallet();
        Wallet receiver = transaction.getReceiverWallet();
        return new TransactionResponse(
                transaction.getId(),
                transaction.getType().name(),
                transaction.getAmount(),
                transaction.getStatus().name(),
                transaction.getCreatedAt(),
                transaction.isFlagged(),
                transaction.getFlagReason(),
                idOf(sender),
                emailOf(sender),
                nameOf(sender),
                idOf(receiver),
                emailOf(receiver),
                nameOf(receiver));
    }

    private static Long idOf(Wallet wallet) {
        return wallet == null ? null : wallet.getId();
    }

    private static String emailOf(Wallet wallet) {
        User user = userOf(wallet);
        return user == null ? null : user.getEmail();
    }

    private static String nameOf(Wallet wallet) {
        User user = userOf(wallet);
        return user == null ? null : user.getName();
    }

    private static User userOf(Wallet wallet) {
        return wallet == null ? null : wallet.getUser();
    }
}
