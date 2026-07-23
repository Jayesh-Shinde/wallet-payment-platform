package com.jayeshshinde.walletpaymentplatform.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TransferInputDTO(
        @NotNull UUID fromWalletId,
        @NotNull UUID toWalletId,
        @Min(value = 1) Long amount
) {
}

