package com.jayeshshinde.walletpaymentplatform.component;

import com.jayeshshinde.walletpaymentplatform.dtos.TransferInputDTO;
import com.jayeshshinde.walletpaymentplatform.dtos.TransferOutputDTO;
import com.jayeshshinde.walletpaymentplatform.service.TransferService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FundingService {
    private final TransferService transferService;
    private final IdempotencyService idempotencyService;
    private final ObjectMapper objectMapper;

    @Transactional
    public TransferOutputDTO transferFunds(TransferInputDTO transferInputDTO, UUID idempotencyKey) {
        try {
            idempotencyService.tryInsert(idempotencyKey);
            return transferService.createTransfer(transferInputDTO, idempotencyKey);
        } catch (DataIntegrityViolationException e) {
            return objectMapper.treeToValue(idempotencyService.findById(idempotencyKey).getResponseData(), TransferOutputDTO.class);
        }

    }
}
