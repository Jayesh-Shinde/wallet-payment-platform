package com.jayeshshinde.walletpaymentplatform.dtos;

import com.jayeshshinde.walletpaymentplatform.enums.TransferStatus;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TransferOutputDTO(
        @NotNull UUID id,
        @NotNull UUID fromWalletId,
        @NotNull UUID toWalletId,
        @NotNull Long amount,
        @NotNull TransferStatus status,
        String reason
) {

}
