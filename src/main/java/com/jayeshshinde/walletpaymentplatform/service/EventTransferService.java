package com.jayeshshinde.walletpaymentplatform.service;

import com.jayeshshinde.walletpaymentplatform.entity.EventTransfer;

import java.util.List;
import java.util.UUID;

public interface EventTransferService {
    List<EventTransfer> claimTransferCompleteEvent();

    void incrementAttempts(UUID claimedUuid);
}
