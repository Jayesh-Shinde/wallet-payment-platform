package com.jayeshshinde.walletpaymentplatform.dtos;

import com.jayeshshinde.walletpaymentplatform.enums.TransferStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
public class TransferOutputDTO {

    public UUID id;

    public UUID fromWalletId;

    public UUID toWalletId;

    public Long amount;

    public TransferStatus status;
    public String reason;
}
