package com.jayeshshinde.walletpaymentplatform.service;

import com.jayeshshinde.walletpaymentplatform.dtos.TransferInputDTO;
import com.jayeshshinde.walletpaymentplatform.dtos.TransferOutputDTO;
import jakarta.validation.Valid;

public interface TransferService {
    TransferOutputDTO createTransfer(@Valid TransferInputDTO transferInputDTO);
}
