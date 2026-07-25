package com.jayeshshinde.walletpaymentplatform.component;

import com.jayeshshinde.walletpaymentplatform.dtos.TransferInputDTO;
import com.jayeshshinde.walletpaymentplatform.dtos.TransferOutputDTO;
import com.jayeshshinde.walletpaymentplatform.entity.LedgerEntry;
import com.jayeshshinde.walletpaymentplatform.enums.LedgerEntryType;
import com.jayeshshinde.walletpaymentplatform.exceptions.ReplayNotReadyException;
import com.jayeshshinde.walletpaymentplatform.repository.LedgerEntryRepository;
import com.jayeshshinde.walletpaymentplatform.repository.TransferRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TransferOrchestratorTest {

    @Autowired
    private TransferOrchestrator transferOrchestrator;

    @Autowired
    private LedgerEntryRepository ledgerEntryRepository;

    @Autowired
    private TransferRepository transferRepository;

    @Test
    void testTransferOrchestrator() throws ExecutionException, InterruptedException {
        int threadCount = 5;
        UUID idempotencyKey = UUID.randomUUID();
        UUID fromWalletId = UUID.fromString("561e3f7b-3ba5-46ba-a0be-010c29ed40a6");
        UUID toWalletId = UUID.fromString("a09e973b-27d2-43ff-84d6-ed2cae7d94da");
        TransferInputDTO transferInputDTO = new TransferInputDTO(fromWalletId,
                toWalletId,
                1L);

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        List<Future<TransferOutputDTO>> futures = new ArrayList<>();
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        Long transferCountFromWalletBefore = transferRepository.countByFromWalletId(fromWalletId);
        for (int i = 0; i < threadCount; i++) {
            futures.add(executorService.submit(() -> {
                try {
                    startLatch.await();
                    return transferOrchestrator.createTransfer(transferInputDTO, idempotencyKey);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    endLatch.countDown();
                }
            }));
        }
        startLatch.countDown();

        boolean completed = endLatch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();
        assertThat(completed).isTrue();
        int successfulTransfers = 0;
        int duplicateTransfer = 0;
        List<TransferOutputDTO> transferOutputDTOList = new ArrayList<>();
        for (Future<TransferOutputDTO> future : futures) {
            try {
                transferOutputDTOList.add(future.get());
                successfulTransfers++;
            } catch (ExecutionException e) {
                if (e.getCause() instanceof ReplayNotReadyException) {
                    duplicateTransfer++;
                } else {
                    throw e;
                }
            }
        }

        Long transferCountFromWalletAfter = transferRepository.countByFromWalletId(fromWalletId);
        UUID transferId = transferOutputDTOList.getFirst().id();

        List<LedgerEntry> byTransferId = ledgerEntryRepository.findByTransferId(transferId);
        assertThat(successfulTransfers + duplicateTransfer).isEqualTo(threadCount);
        assertThat(transferOutputDTOList).extracting(TransferOutputDTO::id).containsOnly(transferId);

        assertThat(byTransferId.size()).isEqualTo(2);
        assertThat(byTransferId).extracting(LedgerEntry::getAmount).containsOnly(1L);
        assertThat(byTransferId).filteredOn(item -> item.getEntryType().equals(LedgerEntryType.DEBIT)).hasSize(1);
        assertThat(byTransferId).filteredOn(item -> item.getEntryType().equals(LedgerEntryType.CREDIT)).hasSize(1);
        assertThat(transferCountFromWalletBefore + 1).isEqualTo(transferCountFromWalletAfter);

    }


}