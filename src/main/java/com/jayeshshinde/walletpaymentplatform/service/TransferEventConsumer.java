package com.jayeshshinde.walletpaymentplatform.service;

import com.jayeshshinde.walletpaymentplatform.entity.NotificationEvent;
import com.jayeshshinde.walletpaymentplatform.enums.NotificationEventStatus;
import com.jayeshshinde.walletpaymentplatform.enums.NotificationEventType;
import com.jayeshshinde.walletpaymentplatform.exceptions.DuplicateNotificationEventException;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransferEventConsumer {


    private final ObjectMapper objectMapper;
    private final NotificationEventService notificationEventService;

    @KafkaListener(topics = "transfer_complete", groupId = "transfer-group")
    public void processTransferComplete(String payload, Acknowledgment acknowledgment) {
        try {

            System.out.println("Received transfer complete event: " + payload);
            if (payload.equals("error")) {
                throw new RuntimeException("error while processing transfer complete");
            }
            JsonNode jsonNode = objectMapper.readTree(payload);
            UUID evenId = UUID.fromString(jsonNode.get("eventId").asString());
            NotificationEvent notificationEvent = new NotificationEvent(evenId,
                    NotificationEventType.TRANSFER_COMPLETE,
                    NotificationEventStatus.PROCESSED, jsonNode);
            notificationEventService.processNotificationEvent(notificationEvent);
        } catch (DuplicateNotificationEventException e) {
            System.out.println("Duplicate notification event: " + e.getMessage());
            acknowledgment.acknowledge();
        }
    }
}
