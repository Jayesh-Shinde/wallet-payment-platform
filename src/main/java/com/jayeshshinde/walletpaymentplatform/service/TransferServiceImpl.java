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

import java.util.NoSuchElementException;

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

        Wallet fromWallet = walletRepository.findById(transferDTO.getFromWalletId())
                .orElseThrow(() -> new NoSuchElementException("From wallet not found."));
        Wallet toWallet = walletRepository.findById(transferDTO.getToWalletId())
                .orElseThrow(() -> new NoSuchElementException("To wallet not found."));
        if (fromWallet.getBalance() < transferDTO.getAmount()) {
            throw new IllegalArgumentException("Not enough balance to transfer.");
        }
        
        validateWalletStatusForTransfer(fromWallet, toWallet);

        Transfer saveTransfer = transferRepository.save(transferMapper.toTransfer(transferDTO));
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
        return transferMapper.toTransferDTO(saveTransfer);
    }

    private static void validateWalletStatusForTransfer(Wallet fromWallet, Wallet toWallet) {
        if (fromWallet.getStatus() == WalletStatus.DEACTIVATED ||
                toWallet.getStatus() == WalletStatus.DEACTIVATED ||
                fromWallet.getStatus() == WalletStatus.PENDING_VERIFICATION ||
                toWallet.getStatus() == WalletStatus.PENDING_VERIFICATION) {
            throw new IllegalArgumentException("Either from wallet or to wallet is deactivated or PENDING VERIFICATION");
        }
    }
}
