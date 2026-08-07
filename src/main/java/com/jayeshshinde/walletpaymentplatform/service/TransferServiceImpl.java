package com.jayeshshinde.walletpaymentplatform.service;

import com.jayeshshinde.walletpaymentplatform.dtos.EventTransferPayload;
import com.jayeshshinde.walletpaymentplatform.dtos.TransferInputDTO;
import com.jayeshshinde.walletpaymentplatform.dtos.TransferOutputDTO;
import com.jayeshshinde.walletpaymentplatform.entity.*;
import com.jayeshshinde.walletpaymentplatform.enums.EventTransferType;
import com.jayeshshinde.walletpaymentplatform.enums.LedgerEntryType;
import com.jayeshshinde.walletpaymentplatform.enums.WalletStatus;
import com.jayeshshinde.walletpaymentplatform.enums.WalletType;
import com.jayeshshinde.walletpaymentplatform.mapper.TransferInputMapper;
import com.jayeshshinde.walletpaymentplatform.mapper.TransferOutputMapper;
import com.jayeshshinde.walletpaymentplatform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.*;

@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {
    private final TransferRepository transferRepository;
    private final WalletRepository walletRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final TransferInputMapper transferMapper;
    private final TransferOutputMapper transferOutputMapper;
    private final ObjectMapper objectMapper;
    private final EventTransferRepository eventTransferRepository;

    @Override
    @Transactional
    public TransferOutputDTO createTransfer(TransferInputDTO transferInputDTO, UUID idempotencyKey) {
        if (transferInputDTO.amount() < 1) {
            throw new IllegalArgumentException("Amount must be greater than 0.");
        }

        IdempotencyRecord idempotencyRecord = idempotencyRecordRepository.getReferenceById(idempotencyKey);
        List<UUID> walletIds = new ArrayList<>();
        walletIds.add(transferInputDTO.fromWalletId());
        walletIds.add(transferInputDTO.toWalletId());
        walletIds.sort(Comparator.naturalOrder());
        Wallet fromWallet = null;
        Wallet toWallet = null;
        for (UUID walletId : walletIds) {
            var wallet = walletRepository.findWithLockById(walletId)
                    .orElseThrow(() -> new NoSuchElementException("wallet not found."));
            if (walletId.equals(transferInputDTO.fromWalletId())) {
                fromWallet = wallet;
            }
            if (walletId.equals(transferInputDTO.toWalletId())) {
                toWallet = wallet;
            }
        }
        Transfer transfer = transferMapper.toTransfer(transferInputDTO);
        boolean sufficientBalance;
        if (fromWallet.getWalletType().equals(WalletType.SYSTEM)) {
            sufficientBalance = true;
        } else {
            Long fromWalletBalance = ledgerEntryRepository.calculateBalanceByWalletId(transferInputDTO.fromWalletId());
            sufficientBalance = fromWalletBalance >= transferInputDTO.amount();
        }

        if (!sufficientBalance) {
            transfer.applyStatusReason("insufficient_balance");
        }
        boolean walletStatusValidation = validateWalletStatusForTransfer(fromWallet, toWallet);
        if (!walletStatusValidation) {
            transfer.applyStatusReason("wallet_status_validation");
        }

        Transfer saveTransfer = transferRepository.save(transfer);
        if (!walletStatusValidation || !sufficientBalance) {
            saveTransfer.failedTransfer();
            return transferOutputMapper.toTransferOutputDTO(saveTransfer);
        }

        saveTransfer.initiateTransfer();
        LedgerEntry debit = new LedgerEntry(saveTransfer.getId(),
                LedgerEntryType.DEBIT,
                saveTransfer.getAmount(),
                saveTransfer.getFromWalletId());
        ledgerEntryRepository.save(debit);
        LedgerEntry credit = new LedgerEntry(saveTransfer.getId(),
                LedgerEntryType.CREDIT,
                saveTransfer.getAmount(),
                saveTransfer.getToWalletId());
        ledgerEntryRepository.save(credit);
        saveTransfer.completeTransfer();
        var response = transferOutputMapper.toTransferOutputDTO(saveTransfer);

        idempotencyRecord.setResponseData(objectMapper.valueToTree(response));
        UUID evenId = UUID.randomUUID();
        JsonNode jsonNode = objectMapper.valueToTree(new EventTransferPayload(evenId, saveTransfer.getFromWalletId(), saveTransfer.getToWalletId(), saveTransfer.getAmount()));
        EventTransfer eventTransfer = new EventTransfer(evenId, EventTransferType.TRANSFER_COMPLETE, saveTransfer.getId(), jsonNode);
        eventTransferRepository.save(eventTransfer);
        return response;

    }

    public void checkWalletType(UUID fromWalletId, UUID toWalletId) {

        if (fromWalletId.equals(toWalletId)) {
            throw new IllegalArgumentException("From Wallet Id must be different from to Wallet Id.");
        }
        List<Wallet> allById = walletRepository.findAllById(List.of(fromWalletId, toWalletId));
        if (allById.size() < 2) {
            throw new NoSuchElementException("wallet not found.");
        }
        allById.forEach(wallet -> {
            if (wallet.getWalletType().equals(WalletType.SYSTEM)) {
                throw new IllegalArgumentException("Wallet Type must not be SYSTEM.");
            }
        });
    }

    private static boolean validateWalletStatusForTransfer(Wallet fromWallet, Wallet toWallet) {
        return !(fromWallet.getStatus() == WalletStatus.DEACTIVATED ||
                toWallet.getStatus() == WalletStatus.DEACTIVATED ||
                fromWallet.getStatus() == WalletStatus.PENDING_VERIFICATION ||
                toWallet.getStatus() == WalletStatus.PENDING_VERIFICATION);
    }
}
