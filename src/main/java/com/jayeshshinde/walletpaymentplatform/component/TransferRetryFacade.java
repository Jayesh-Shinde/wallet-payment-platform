package com.jayeshshinde.walletpaymentplatform.component;

import com.jayeshshinde.walletpaymentplatform.dtos.TransferInputDTO;
import com.jayeshshinde.walletpaymentplatform.dtos.TransferOutputDTO;
import com.jayeshshinde.walletpaymentplatform.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransferRetryFacade {
    private final TransferService transferService;

    @Retryable(maxRetries = 3,
            includes = {ObjectOptimisticLockingFailureException.class, PessimisticLockingFailureException.class},
            delay = 500,
            multiplier = 2,
            jitter = 50)
    public TransferOutputDTO createTransfer(@Valid TransferInputDTO transferInputDTO) {
        return transferService.createTransfer(transferInputDTO);
    }
}
