package com.commerce.controller;

import com.commerce.dto.WalletCreateRequest;
import com.commerce.dto.WalletFilterRequest;
import com.commerce.dto.WalletResponse;
import com.commerce.dto.WalletTransactionRequest;
import com.commerce.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/wallets")
public class WalletController {

    private final WalletService walletService;

    @PostMapping
    public ResponseEntity<WalletResponse> createWallet(
            @Valid @RequestBody WalletCreateRequest request
    ) {
        return ResponseEntity.ok(walletService.createWallet(request));
    }

    @GetMapping
    public ResponseEntity<List<WalletResponse>> getWallets(
            @Valid @ModelAttribute WalletFilterRequest filter
    ) {
        return ResponseEntity.ok(walletService.getWallets(filter));
    }

    @PatchMapping("/{id}/deposit")
    public ResponseEntity<WalletResponse> deposit(
            @PathVariable Long id,
            @Valid @RequestBody WalletTransactionRequest request
    ) {
        return ResponseEntity.ok(walletService.deposit(id, request));
    }

    @PatchMapping("/{id}/withdraw")
    public ResponseEntity<WalletResponse> withdraw(
            @PathVariable Long id,
            @Valid @RequestBody WalletTransactionRequest request
    ) {
        return ResponseEntity.ok(walletService.withdraw(id, request));
    }
}
