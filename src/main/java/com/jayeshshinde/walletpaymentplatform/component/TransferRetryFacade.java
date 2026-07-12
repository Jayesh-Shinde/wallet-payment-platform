package com.jayeshshinde.walletpaymentplatform.component;

import com.jayeshshinde.walletpaymentplatform.dtos.TransferDTO;
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
    public TransferDTO createTransfer(@Valid TransferDTO transferDTO) {
        return transferService.createTransfer(transferDTO);
    }
}
