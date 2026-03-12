package org.example.service;

import org.example.api.ErrorCode;
import org.example.exception.BusinessException;
import org.example.model.WalletEntity;
import org.example.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class WalletService {

    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Transactional
    public BigDecimal addMoney(String phone, BigDecimal amount) {
        WalletEntity wallet = walletRepository.findById(phone)
                .orElse(new WalletEntity(phone, BigDecimal.ZERO));
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);
        return wallet.getBalance();
    }

    @Transactional
    public void debit(String phone, BigDecimal amount) {
        WalletEntity wallet = walletRepository.findById(phone)
                .orElse(new WalletEntity(phone, BigDecimal.ZERO));
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_BALANCE, "Insufficient wallet balance");
        }
        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);
    }

    @Transactional
    public void credit(String phone, BigDecimal amount) {
        addMoney(phone, amount);
    }

    @Transactional(readOnly = true)
    public BigDecimal getBalance(String phone) {
        return walletRepository.findById(phone)
                .map(WalletEntity::getBalance)
                .orElse(BigDecimal.ZERO);
    }
}
