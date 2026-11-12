package com.digitalwallet.wallet;

import java.math.BigDecimal;

import com.digitalwallet.transaction.TransactionResponse;

public record MoneyMovementResponse(BigDecimal balance, TransactionResponse transaction) {
}
