package com.jayeshshinde.walletpaymentplatform.service;

import com.jayeshshinde.walletpaymentplatform.entity.EventTransfer;
import com.jayeshshinde.walletpaymentplatform.repository.EventTransferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventTransferServiceImpl implements EventTransferService {
    private final EventTransferRepository eventTransferRepository;

    @Override
    @Transactional
    public List<EventTransfer> claimTransferCompleteEvent() {
        List<EventTransfer> claimedPayload = eventTransferRepository.fetchClaimTransferCompleteEvent(Instant.now(), 5);
        List<UUID> claimedUuids = claimedPayload.stream().map(EventTransfer::getId).toList();
        eventTransferRepository.claimTransferCompleteEvent(claimedUuids, Instant.now().plusSeconds(60), Instant.now());
        return claimedPayload;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void incrementAttempts(UUID claimedUuid) {

    }
}
