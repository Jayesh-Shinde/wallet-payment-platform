package com.jayeshshinde.walletpaymentplatform.component;

import com.jayeshshinde.walletpaymentplatform.entity.IdempotencyRecord;
import com.jayeshshinde.walletpaymentplatform.exceptions.IdempotencyKeyConflictException;
import com.jayeshshinde.walletpaymentplatform.repository.IdempotencyRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
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
        try {
            IdempotencyRecord item = new IdempotencyRecord(idempotencyKey);
            idempotencyRecordRepository.save(item);
            idempotencyRecordRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new IdempotencyKeyConflictException(e.getMessage());
        }
    }

    public IdempotencyRecord findById(UUID idempotencyKey) {
        return idempotencyRecordRepository.findById(idempotencyKey).orElseThrow();
    }
}
