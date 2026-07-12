package com.jayeshshinde.walletpaymentplatform.service;

import com.jayeshshinde.walletpaymentplatform.dtos.TransferDTO;
import com.jayeshshinde.walletpaymentplatform.entity.LedgerEntry;
import com.jayeshshinde.walletpaymentplatform.entity.Transfer;
import com.jayeshshinde.walletpaymentplatform.entity.Wallet;
import com.jayeshshinde.walletpaymentplatform.enums.LedgerEntryType;
import com.jayeshshinde.walletpaymentplatform.enums.WalletStatus;
import com.jayeshshinde.walletpaymentplatform.mapper.TransferMapper;
import com.jayeshshinde.walletpaymentplatform.repository.LedgerEntryRepository;
import com.jayeshshinde.walletpaymentplatform.repository.TransferRepository;
import com.jayeshshinde.walletpaymentplatform.repository.WalletRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {
    private final TransferRepository transferRepository;
    private final WalletRepository walletRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final TransferMapper transferMapper;

    @Override
    @Transactional
    public TransferDTO createTransfer(@Valid TransferDTO transferDTO) {
        if (transferDTO.getAmount() < 1) {
            throw new IllegalArgumentException("Amount must be greater than 0.");
        }
        if (transferDTO.getFromWalletId().equals(transferDTO.getToWalletId())) {
            throw new IllegalArgumentException("From Wallet Id must be different from to Wallet Id.");
        }

        List<UUID> walletIds = new ArrayList<>();
        walletIds.add(transferDTO.getFromWalletId());
        walletIds.add(transferDTO.getToWalletId());
        walletIds.sort(Comparator.naturalOrder());
        List<Wallet> wallets = new ArrayList<>();
        for (UUID walletId : walletIds) {
            wallets.add(walletRepository.findWithLockByWalletId(walletId)
                    .orElseThrow(() -> new NoSuchElementException("wallet not found.")));
        }
        Transfer transfer = transferMapper.toTransfer(transferDTO);

        Long fromWalletBalance = ledgerEntryRepository.calculateBalanceByWalletId(transferDTO.getFromWalletId(), LedgerEntryType.DEBIT);
        boolean sufficientBalance = fromWalletBalance < transferDTO.getAmount();
        if (sufficientBalance) {
            transfer.applyStatusReason("insufficient_balance");
        }
        boolean walletStatusValidation = validateWalletStatusForTransfer(wallets.getFirst(), wallets.getLast());
        if (walletStatusValidation) {
            transfer.applyStatusReason("wallet_status_validation");
        }

        Transfer saveTransfer = transferRepository.save(transfer);
        if (!walletStatusValidation || sufficientBalance) {
            saveTransfer.failedTransfer();
            return transferMapper.toTransferDTO(saveTransfer);
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
        return transferMapper.toTransferDTO(saveTransfer);
    }

    private static boolean validateWalletStatusForTransfer(Wallet wallet1, Wallet wallet2) {
        return !(wallet1.getStatus() == WalletStatus.DEACTIVATED ||
                wallet2.getStatus() == WalletStatus.DEACTIVATED ||
                wallet2.getStatus() == WalletStatus.PENDING_VERIFICATION ||
                wallet1.getStatus() == WalletStatus.PENDING_VERIFICATION);
    }
}
