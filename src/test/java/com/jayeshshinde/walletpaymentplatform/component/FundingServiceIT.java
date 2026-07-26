package com.jayeshshinde.walletpaymentplatform.component;

import com.jayeshshinde.walletpaymentplatform.dtos.TransferInputDTO;
import com.jayeshshinde.walletpaymentplatform.dtos.TransferOutputDTO;
import com.jayeshshinde.walletpaymentplatform.enums.TransferStatus;
import com.jayeshshinde.walletpaymentplatform.service.FundingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest
class FundingServiceIT {

    @Autowired
    private FundingService fundingService;

    @Test
    void transferFunds() {
        UUID fromWalletId = UUID.fromString("cf3b1447-6270-4637-ad78-f1ca79c87374");
        UUID toWalletId = UUID.fromString("a09e973b-27d2-43ff-84d6-ed2cae7d94da");
        TransferInputDTO transferInputDTO = new TransferInputDTO(fromWalletId, toWalletId, 1000000L);
        UUID idempotencyKey = UUID.randomUUID();
        TransferOutputDTO transferOutputDTO = fundingService.transferFunds(transferInputDTO, idempotencyKey);
        assert transferOutputDTO.status().equals(TransferStatus.COMPLETED);

    }
}