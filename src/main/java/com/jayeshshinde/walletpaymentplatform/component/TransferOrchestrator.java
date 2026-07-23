package com.jayeshshinde.walletpaymentplatform.component;

import com.jayeshshinde.walletpaymentplatform.dtos.TransferInputDTO;
import com.jayeshshinde.walletpaymentplatform.dtos.TransferOutputDTO;
import com.jayeshshinde.walletpaymentplatform.exceptions.ReplayNotReadyException;
import com.jayeshshinde.walletpaymentplatform.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TransferOrchestrator {
    private final TransferService transferService;
    private final IdempotencyService idempotencyService;
    private final ObjectMapper objectMapper;

    @Retryable(maxRetries = 3,
            includes = {ObjectOptimisticLockingFailureException.class, PessimisticLockingFailureException.class,
                    ReplayNotReadyException.class},
            delay = 500,
            multiplier = 2,
            jitter = 50)
    public TransferOutputDTO createTransfer(@Valid TransferInputDTO transferInputDTO, UUID idempotencyKey) {
        try {
            idempotencyService.tryInsert(idempotencyKey);
            transferService.checkWalletType(transferInputDTO.fromWalletId(), transferInputDTO.toWalletId());
            return transferService.createTransfer(transferInputDTO, idempotencyKey);
        } catch (DataIntegrityViolationException e) {
            var jsonNode = idempotencyService.findById(idempotencyKey).getResponseData();
            if (jsonNode == null) {
                throw new ReplayNotReadyException("this exact operation is still in flight — wait and retry the same key ");
            }
            return objectMapper.treeToValue(jsonNode, TransferOutputDTO.class);
        }

    }
}
