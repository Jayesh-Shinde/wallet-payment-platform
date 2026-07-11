package com.jayeshshinde.walletpaymentplatform.dtos;

import com.jayeshshinde.walletpaymentplatform.enums.TransferStatus;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.util.UUID;

@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
public class TransferDTO {
    public UUID id;
    @NonNull
    public UUID fromWalletId;
    @NonNull
    public UUID toWalletId;
    @Min(value = 1)
    public Long amount;
    public TransferStatus status;
}
