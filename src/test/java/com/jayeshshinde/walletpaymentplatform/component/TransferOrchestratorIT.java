package com.jayeshshinde.walletpaymentplatform.component;

import com.jayeshshinde.walletpaymentplatform.controller.TransferController;
import com.jayeshshinde.walletpaymentplatform.dtos.TransferInputDTO;
import com.jayeshshinde.walletpaymentplatform.dtos.TransferOutputDTO;
import com.jayeshshinde.walletpaymentplatform.entity.LedgerEntry;
import com.jayeshshinde.walletpaymentplatform.enums.LedgerEntryType;
import com.jayeshshinde.walletpaymentplatform.exceptions.ReplayNotReadyException;
import com.jayeshshinde.walletpaymentplatform.repository.LedgerEntryRepository;
import com.jayeshshinde.walletpaymentplatform.repository.TransferRepository;
import com.jayeshshinde.walletpaymentplatform.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TransferOrchestratorIT {

    @Autowired
    private TransferOrchestrator transferOrchestrator;

    @Autowired
    private LedgerEntryRepository ledgerEntryRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransferController transferController;

    @Test
    void testTransferOrchestratorIdempotency() throws ExecutionException, InterruptedException {
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


    @Test
    void testCreateTransferContention() throws ExecutionException, InterruptedException {
        List<UUID> idempotencyKeys = new ArrayList<>(List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()));
        int threadCount = 5;
        UUID fromWalletId1 = UUID.fromString("561e3f7b-3ba5-46ba-a0be-010c29ed40a6");
        UUID fromWalletId2 = UUID.fromString("a09e973b-27d2-43ff-84d6-ed2cae7d94da");
        UUID toWalletId = UUID.fromString("fa247d06-52fd-47f0-a70e-80317d2fec64");
        Long beforeTransferBalanceFromWallet1 = ledgerEntryRepository.calculateBalanceByWalletId(fromWalletId1);
        Long beforeTransferBalanceFromWallet2 = ledgerEntryRepository.calculateBalanceByWalletId(fromWalletId2);
        Long beforeTransferBalanceToWalletId = ledgerEntryRepository.calculateBalanceByWalletId(toWalletId);

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        List<Future<TransferOutputDTO>> futures = new ArrayList<>();
        List<TransferOutputDTO> transferOutputDTOList = new ArrayList<>();
        List<TransferInputDTO> transferInputDTOList = new ArrayList<>();
        transferInputDTOList.add(new TransferInputDTO(fromWalletId1, toWalletId, 1L));
        transferInputDTOList.add(new TransferInputDTO(fromWalletId2, toWalletId, 1L));
        transferInputDTOList.add(new TransferInputDTO(fromWalletId1, toWalletId, 1L));
        transferInputDTOList.add(new TransferInputDTO(fromWalletId2, toWalletId, 1L));
        transferInputDTOList.add(new TransferInputDTO(fromWalletId1, toWalletId, 1L));
        ExecutorService lockHolderExecutor = Executors.newSingleThreadExecutor();
        CountDownLatch lockLatch = new CountDownLatch(1);
        lockHolderExecutor.submit(() -> {
            transactionTemplate.execute(status -> {
                walletRepository.findWithLockById(toWalletId);
                lockLatch.countDown();
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return null;
            });
        });

        lockLatch.await();

        for (int i = 0; i < threadCount; i++) {
            TransferInputDTO transferInputDTO = transferInputDTOList.get(i);
            UUID idempotencyKey = idempotencyKeys.get(i);
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
        lockHolderExecutor.shutdown();
        assertThat(completed).isTrue();
        for (Future<TransferOutputDTO> future : futures) {
            try {
                transferOutputDTOList.add(future.get());
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        Long afterTransferBalanceFromWallet1 = ledgerEntryRepository.calculateBalanceByWalletId(fromWalletId1);
        Long afterTransferBalanceFromWallet2 = ledgerEntryRepository.calculateBalanceByWalletId(fromWalletId2);
        Long afterTransferBalanceToWalletId = ledgerEntryRepository.calculateBalanceByWalletId(toWalletId);
        assertThat(transferOutputDTOList.size()).isEqualTo(threadCount);
        assertThat(transferOutputDTOList).extracting(TransferOutputDTO::id).doesNotHaveDuplicates();
        assertThat(afterTransferBalanceFromWallet1).isEqualTo(beforeTransferBalanceFromWallet1 - 3);
        assertThat(afterTransferBalanceFromWallet2).isEqualTo(beforeTransferBalanceFromWallet2 - 2);
        assertThat(afterTransferBalanceToWalletId).isEqualTo(beforeTransferBalanceToWalletId + 5);
        transferOutputDTOList.forEach(TransferOutputDTO -> {
            List<LedgerEntry> byTransferId = ledgerEntryRepository.findByTransferId(TransferOutputDTO.id());
            assertThat(byTransferId.size()).isEqualTo(2);
            assertThat(byTransferId).filteredOn(item -> item.getEntryType().equals(LedgerEntryType.CREDIT)).hasSize(1);
            assertThat(byTransferId).filteredOn(item -> item.getEntryType().equals(LedgerEntryType.DEBIT)).hasSize(1);
        });
    }


}