package com.jayeshshinde.walletpaymentplatform.component.scheduler;

import com.jayeshshinde.walletpaymentplatform.entity.EventTransfer;
import com.jayeshshinde.walletpaymentplatform.service.EventTransferPublishService;
import com.jayeshshinde.walletpaymentplatform.service.EventTransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EventTransferRelayer {
    private final EventTransferService eventTransferService;
    private final EventTransferPublishService eventTransferPublishService;

    @Scheduled(fixedDelay = 2000)
    public void relayTransferCompleteEvent() {
        List<EventTransfer> claimedEvent = eventTransferService.claimTransferCompleteEvent();

        for (EventTransfer eventTransfer : claimedEvent) {
            eventTransferPublishService.publishTransferCompleteEvent(eventTransfer);
        }
    }
}
