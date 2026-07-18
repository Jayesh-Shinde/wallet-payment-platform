package com.jayeshshinde.walletpaymentplatform.component;

import com.jayeshshinde.walletpaymentplatform.entity.IdempotencyRecord;
import com.jayeshshinde.walletpaymentplatform.repository.IdempotencyRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class IdempotencyService {
    private final IdempotencyRecordRepository idempotencyRecordRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void tryInsert(UUID idempotencyKey) {
        IdempotencyRecord item = new IdempotencyRecord(idempotencyKey);
        idempotencyRecordRepository.save(item);
        idempotencyRecordRepository.flush();
    }

    public IdempotencyRecord findById(UUID idempotencyKey) {
        return idempotencyRecordRepository.findById(idempotencyKey).orElseThrow();
    }
}
