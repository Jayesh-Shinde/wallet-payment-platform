package com.jayeshshinde.walletpaymentplatform.controller;

import com.jayeshshinde.walletpaymentplatform.component.TransferRetryFacade;
import com.jayeshshinde.walletpaymentplatform.dtos.TransferDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TransferController {
    private final TransferRetryFacade transferRetryFacade;

    @PostMapping("/api/transfer")
    public TransferDTO createTransfer(@RequestBody TransferDTO transferDTO) {
        return transferRetryFacade.createTransfer(transferDTO);
    }
}
