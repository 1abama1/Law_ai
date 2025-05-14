package org.example.bank.Config;

import org.example.bank.Service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class SchedulerConfig {

    @Autowired
    private LoanService loanService;

    /**
     * Метод для автоматического списания ежемесячных платежей по кредитам
     * Запускается каждый месяц 1 числа в 00:00
     */
    @Scheduled(cron = "0 0 0 1 * ?")
    public void processMonthlyPayments() {
        loanService.processMonthlyPayments();
    }
} 