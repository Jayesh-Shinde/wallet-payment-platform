package com.jayeshshinde.walletpaymentplatform.service;

import com.jayeshshinde.walletpaymentplatform.dtos.TransferInputDTO;
import com.jayeshshinde.walletpaymentplatform.dtos.TransferOutputDTO;
import jakarta.validation.Valid;

import java.util.UUID;

public interface TransferService {
    TransferOutputDTO createTransfer(@Valid TransferInputDTO transferInputDTO, UUID idempotency_key);

    void checkWalletType(UUID fromWalletId, UUID toWalletId);
}
