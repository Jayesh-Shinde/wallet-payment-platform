package com.jayeshshinde.walletpaymentplatform.service;

import com.jayeshshinde.walletpaymentplatform.entity.EventTransfer;

public interface EventTransferPublishService {
    void publishTransferCompleteEvent(EventTransfer eventTransfer);
}
