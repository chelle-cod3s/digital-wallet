package com.digitalwallet.transaction;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(
        Long id,
        String type,
        BigDecimal amount,
        String status,
        Instant createdAt,
        boolean flagged,
        String flagReason,
        Long senderWalletId,
        String senderEmail,
        String senderName,
        Long receiverWalletId,
        String receiverEmail,
        String receiverName
) {
}
