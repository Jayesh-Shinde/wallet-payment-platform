package com.jayeshshinde.walletpaymentplatform.service;

import com.jayeshshinde.walletpaymentplatform.dtos.TransferDTO;
import jakarta.validation.Valid;

public interface TransferService {
    TransferDTO createTransfer(@Valid TransferDTO transferDTO);
}
