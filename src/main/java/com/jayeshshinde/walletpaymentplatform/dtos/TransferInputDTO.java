package com.jayeshshinde.walletpaymentplatform.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

//@AllArgsConstructor
//@Data
//@NoArgsConstructor
//@Builder
//public class TransferInputDTO {
//    @NotNull
//    public UUID fromWalletId;
//    @NotNull
//    public UUID toWalletId;
//    @Min(value = 1)
//    public Long amount;
//}

public record TransferInputDTO(@NotNull UUID fromWalletId,
                               @NotNull UUID toWalletId,
                               @Min(value = 1) Long amount) {
}

