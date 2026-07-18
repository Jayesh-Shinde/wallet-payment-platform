package com.jayeshshinde.walletpaymentplatform.service;

import com.jayeshshinde.walletpaymentplatform.dtos.TransferInputDTO;
import com.jayeshshinde.walletpaymentplatform.dtos.TransferOutputDTO;
import com.jayeshshinde.walletpaymentplatform.entity.IdempotencyRecord;
import com.jayeshshinde.walletpaymentplatform.entity.LedgerEntry;
import com.jayeshshinde.walletpaymentplatform.entity.Transfer;
import com.jayeshshinde.walletpaymentplatform.entity.Wallet;
import com.jayeshshinde.walletpaymentplatform.enums.LedgerEntryType;
import com.jayeshshinde.walletpaymentplatform.enums.WalletStatus;
import com.jayeshshinde.walletpaymentplatform.enums.WalletType;
import com.jayeshshinde.walletpaymentplatform.mapper.TransferInputMapper;
import com.jayeshshinde.walletpaymentplatform.mapper.TransferOutputMapper;
import com.jayeshshinde.walletpaymentplatform.repository.IdempotencyRecordRepository;
import com.jayeshshinde.walletpaymentplatform.repository.LedgerEntryRepository;
import com.jayeshshinde.walletpaymentplatform.repository.TransferRepository;
import com.jayeshshinde.walletpaymentplatform.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    @Override
    @Transactional
    public TransferOutputDTO createTransfer(TransferInputDTO transferInputDTO, UUID idempotencyKey) {
        if (transferInputDTO.getAmount() < 1) {
            throw new IllegalArgumentException("Amount must be greater than 0.");
        }
        if (transferInputDTO.getFromWalletId().equals(transferInputDTO.getToWalletId())) {
            throw new IllegalArgumentException("From Wallet Id must be different from to Wallet Id.");
        }
        IdempotencyRecord idempotencyRecord = idempotencyRecordRepository.getReferenceById(idempotencyKey);
        List<UUID> walletIds = new ArrayList<>();
        walletIds.add(transferInputDTO.getFromWalletId());
        walletIds.add(transferInputDTO.getToWalletId());
        walletIds.sort(Comparator.naturalOrder());
        Wallet fromWallet = null;
        Wallet toWallet = null;
        for (UUID walletId : walletIds) {
            var wallet = walletRepository.findWithLockById(walletId)
                    .orElseThrow(() -> new NoSuchElementException("wallet not found."));
            if (walletId.equals(transferInputDTO.getFromWalletId())) {
                fromWallet = wallet;
            }
            if (walletId.equals(transferInputDTO.getToWalletId())) {
                toWallet = wallet;
            }
        }
        Transfer transfer = transferMapper.toTransfer(transferInputDTO);
        boolean sufficientBalance;
        if (fromWallet.getWalletType().equals(WalletType.SYSTEM)) {
            sufficientBalance = true;
        } else {
            Long fromWalletBalance = ledgerEntryRepository.calculateBalanceByWalletId(transferInputDTO.getFromWalletId(), LedgerEntryType.DEBIT);
            sufficientBalance = fromWalletBalance < transferInputDTO.getAmount();
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
        return response;

    }

    public void checkWalletType(UUID fromWalletId, UUID toWalletId) {
        Wallet toWallet = walletRepository.findById(toWalletId).orElseThrow(() -> new NoSuchElementException("toWallet wallet not found."));
        Wallet fromWallet = walletRepository.findById(fromWalletId).orElseThrow(() -> new NoSuchElementException("fromWallet wallet not found."));
        if (toWallet.getWalletType() == WalletType.SYSTEM ||
                fromWallet.getWalletType() == WalletType.SYSTEM) {
            throw new IllegalArgumentException("System type wallet can not be used.");
        }
    }

    private static boolean validateWalletStatusForTransfer(Wallet fromWallet, Wallet toWallet) {
        return !(fromWallet.getStatus() == WalletStatus.DEACTIVATED ||
                toWallet.getStatus() == WalletStatus.DEACTIVATED ||
                fromWallet.getStatus() == WalletStatus.PENDING_VERIFICATION ||
                toWallet.getStatus() == WalletStatus.PENDING_VERIFICATION);
    }
}
