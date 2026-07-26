package com.jayeshshinde.walletpaymentplatform.component;

import com.jayeshshinde.walletpaymentplatform.dtos.TransferInputDTO;
import com.jayeshshinde.walletpaymentplatform.dtos.TransferOutputDTO;
import com.jayeshshinde.walletpaymentplatform.exceptions.IdempotencyKeyConflictException;
import com.jayeshshinde.walletpaymentplatform.exceptions.ReplayNotReadyException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TransferOrchestrator {
    private final IdempotencyService idempotencyService;
    private final ObjectMapper objectMapper;
    private final TransferRetryFacade transferRetryFacade;

    public TransferOutputDTO createTransfer(@Valid TransferInputDTO transferInputDTO, UUID idempotencyKey) {
        try {
            idempotencyService.tryInsert(idempotencyKey);
            return transferRetryFacade.createTransfer(transferInputDTO, idempotencyKey);
        } catch (IdempotencyKeyConflictException e) {
            var jsonNode = idempotencyService.findById(idempotencyKey).getResponseData();
            if (jsonNode == null) {
                throw new ReplayNotReadyException("this exact operation is still in flight — wait and retry the same key ");
            }
            return objectMapper.treeToValue(jsonNode, TransferOutputDTO.class);
        }
    }
}
