package com.digitalwallet.wallet;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping
    public WalletResponse getWallet() {
        return walletService.getMyWallet();
    }

    @PostMapping("/deposit")
    public MoneyMovementResponse deposit(@Valid @RequestBody AmountRequest request) {
        return walletService.deposit(request);
    }

    @PostMapping("/withdraw")
    public MoneyMovementResponse withdraw(@Valid @RequestBody AmountRequest request) {
        return walletService.withdraw(request);
    }

    @PostMapping("/transfer")
    public MoneyMovementResponse transfer(@Valid @RequestBody TransferRequest request) {
        return walletService.transfer(request);
    }
}
