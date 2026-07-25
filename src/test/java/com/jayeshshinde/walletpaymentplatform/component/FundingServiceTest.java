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
class FundingServiceTest {

    @Autowired
    private FundingService fundingService;

    @Test
    void transferFunds() {
        UUID fromWalletId = UUID.fromString("cf3b1447-6270-4637-ad78-f1ca79c87374");
        UUID toWalletId = UUID.fromString("fa247d06-52fd-47f0-a70e-80317d2fec64");
        TransferInputDTO transferInputDTO = new TransferInputDTO(fromWalletId, toWalletId, 1000000L);
        UUID idempotencyKey = UUID.randomUUID();
        TransferOutputDTO transferOutputDTO = fundingService.transferFunds(transferInputDTO, idempotencyKey);
        assert transferOutputDTO.status().equals(TransferStatus.COMPLETED);

    }
}