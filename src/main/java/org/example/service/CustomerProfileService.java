package org.example.service;

import org.example.api.ErrorCode;
import org.example.dto.BankAccountRequest;
import org.example.dto.BankAccountResponse;
import org.example.dto.KycProfileResponse;
import org.example.dto.KycRequest;
import org.example.exception.BusinessException;
import org.example.model.BankAccountEntity;
import org.example.model.KycProfileEntity;
import org.example.model.KycStatus;
import org.example.repository.BankAccountRepository;
import org.example.repository.KycProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerProfileService {

    private final KycProfileRepository kycProfileRepository;
    private final BankAccountRepository bankAccountRepository;

    public CustomerProfileService(KycProfileRepository kycProfileRepository, BankAccountRepository bankAccountRepository) {
        this.kycProfileRepository = kycProfileRepository;
        this.bankAccountRepository = bankAccountRepository;
    }

    @Transactional
    public KycProfileResponse upsertKyc(KycRequest request) {
        KycProfileEntity entity = kycProfileRepository.findById(request.getPhone())
                .orElse(new KycProfileEntity(request.getPhone(), request.getFullName(), request.getEmail(), panLast4(request.getPanNumber()), KycStatus.PENDING));

        entity.setFullName(request.getFullName());
        entity.setEmail(request.getEmail());
        entity.setPanLast4(panLast4(request.getPanNumber()));
        entity.setStatus(KycStatus.VERIFIED);

        return toKycResponse(kycProfileRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public KycProfileResponse getKyc(String phone) {
        KycProfileEntity entity = kycProfileRepository.findById(phone)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "KYC profile not found"));
        return toKycResponse(entity);
    }

    @Transactional(readOnly = true)
    public void ensureKycVerified(String phone) {
        KycProfileEntity entity = kycProfileRepository.findById(phone)
                .orElseThrow(() -> new BusinessException(ErrorCode.KYC_NOT_COMPLETED, "KYC is required for this operation"));
        if (entity.getStatus() != KycStatus.VERIFIED) {
            throw new BusinessException(ErrorCode.KYC_NOT_COMPLETED, "KYC must be verified for this operation");
        }
    }

    @Transactional
    public BankAccountResponse addBankAccount(BankAccountRequest request) {
        ensureKycVerified(request.getPhone());
        BankAccountEntity entity = new BankAccountEntity(
                request.getPhone(),
                request.getAccountHolder(),
                request.getBankName(),
                accountLast4(request.getAccountNumber()),
                request.getIfscCode(),
                request.getUpiId()
        );
        return toBankResponse(bankAccountRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<BankAccountResponse> getBankAccounts(String phone) {
        return bankAccountRepository.findByPhoneOrderByIdDesc(phone)
                .stream()
                .map(this::toBankResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BankAccountResponse getBankAccount(String phone, Long bankAccountId) {
        BankAccountEntity entity = bankAccountRepository.findByIdAndPhone(bankAccountId, phone)
                .orElseThrow(() -> new BusinessException(ErrorCode.BANK_ACCOUNT_NOT_FOUND, "Beneficiary bank account not found"));
        return toBankResponse(entity);
    }

    private String panLast4(String panNumber) {
        return panNumber.substring(panNumber.length() - 4);
    }

    private String accountLast4(String accountNumber) {
        return accountNumber.substring(accountNumber.length() - 4);
    }

    private KycProfileResponse toKycResponse(KycProfileEntity entity) {
        return new KycProfileResponse(
                entity.getPhone(),
                entity.getFullName(),
                entity.getEmail(),
                "XXXXXX" + entity.getPanLast4(),
                entity.getStatus()
        );
    }

    private BankAccountResponse toBankResponse(BankAccountEntity entity) {
        return new BankAccountResponse(
                entity.getId(),
                entity.getPhone(),
                entity.getAccountHolder(),
                entity.getBankName(),
                "XXXX" + entity.getAccountLast4(),
                entity.getIfscCode(),
                entity.getUpiId()
        );
    }
}

