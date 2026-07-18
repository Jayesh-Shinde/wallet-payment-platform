package com.jayeshshinde.walletpaymentplatform.controller;

import com.jayeshshinde.walletpaymentplatform.component.TransferOrchestrator;
import com.jayeshshinde.walletpaymentplatform.dtos.TransferInputDTO;
import com.jayeshshinde.walletpaymentplatform.dtos.TransferOutputDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TransferController {
    private final TransferOrchestrator transferOrchestrator;

    @PostMapping("/api/transfer")

    public TransferOutputDTO createTransfer(
            @Valid @RequestBody TransferInputDTO transferInputDTO,
            @RequestAttribute(value = "X-Idempotency-Key", required = true) UUID idempotencyKey
    ) {
        return transferOrchestrator.createTransfer(transferInputDTO, idempotencyKey);
    }
}
