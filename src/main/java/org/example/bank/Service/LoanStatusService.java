package org.example.bank.Service;

import org.example.bank.Entity.LoanStatus;
import org.example.bank.Repository.LoanStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoanStatusService {
    private final LoanStatusRepository loanStatusRepository;

    @Autowired
    public LoanStatusService(LoanStatusRepository loanStatusRepository) {
        this.loanStatusRepository = loanStatusRepository;
        initializeDefaultLoanStatuses();
    }

    @Transactional
    public void initializeDefaultLoanStatuses() {
        if (loanStatusRepository.findByName("PENDING") == null) {
            LoanStatus pendingStatus = new LoanStatus();
            pendingStatus.setName("PENDING");
            pendingStatus.setDescription("Ожидает одобрения");
            loanStatusRepository.save(pendingStatus);
        }

        if (loanStatusRepository.findByName("ACTIVE") == null) {
            LoanStatus activeStatus = new LoanStatus();
            activeStatus.setName("ACTIVE");
            activeStatus.setDescription("Активный кредит");
            loanStatusRepository.save(activeStatus);
        }

        if (loanStatusRepository.findByName("PAID") == null) {
            LoanStatus paidStatus = new LoanStatus();
            paidStatus.setName("PAID");
            paidStatus.setDescription("Кредит выплачен");
            loanStatusRepository.save(paidStatus);
        }

        if (loanStatusRepository.findByName("REJECTED") == null) {
            LoanStatus rejectedStatus = new LoanStatus();
            rejectedStatus.setName("REJECTED");
            rejectedStatus.setDescription("Кредит отклонен");
            loanStatusRepository.save(rejectedStatus);
        }
    }

    public LoanStatus getLoanStatusByName(String name) {
        return loanStatusRepository.findByName(name);
    }
} 