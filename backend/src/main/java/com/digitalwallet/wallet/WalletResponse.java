package com.digitalwallet.wallet;

import java.math.BigDecimal;

public record WalletResponse(Long id, BigDecimal balance) {
}
