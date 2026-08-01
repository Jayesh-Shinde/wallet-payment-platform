package com.jayeshshinde.walletpaymentplatform.service;

import com.jayeshshinde.walletpaymentplatform.entity.EventTransfer;
import com.jayeshshinde.walletpaymentplatform.repository.EventTransferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventTransferPublishServiceImpl implements EventTransferPublishService {
    private final EventTransferRepository eventTransferRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String transferCompleteTopic = "transfer_complete";

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publishTransferCompleteEvent(EventTransfer eventTransfer) {
        try {
            kafkaTemplate.send(transferCompleteTopic, eventTransfer.getTransferId().toString(), eventTransfer.getPayload().toString());
            eventTransfer.markProcessed();
        } catch (Exception e) {
            eventTransfer.incrementAttempts();
        } finally {
            eventTransferRepository.save(eventTransfer);
        }
    }
}
