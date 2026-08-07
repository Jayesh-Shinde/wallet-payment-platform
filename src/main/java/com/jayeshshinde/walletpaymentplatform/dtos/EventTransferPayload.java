package com.jayeshshinde.walletpaymentplatform.dtos;

import java.util.UUID;

public record EventTransferPayload(
        UUID eventId,
        UUID fromWalletId,
        UUID toWalletId,
        Long amount
) {
}
