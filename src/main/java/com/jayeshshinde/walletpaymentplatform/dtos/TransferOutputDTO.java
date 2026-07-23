package com.jayeshshinde.walletpaymentplatform.dtos;

//@AllArgsConstructor
//@Data
//@NoArgsConstructor
//@Builder
//public class TransferOutputDTO {
//
//    public UUID id;
//
//    public UUID fromWalletId;
//
//    public UUID toWalletId;
//
//    public Long amount;
//
//    public TransferStatus status;
//    public String reason;
//}

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
