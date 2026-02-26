package org.example.plus.domain.account.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.plus.common.entity.Account;
import org.example.plus.domain.account.repository.AccountRepository;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    @Transactional
    public void withdraw(Long accountId, int amount) {

        Account account = accountRepository.findById(accountId).orElseThrow();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        account.decrease(amount);
        log.info(Thread.currentThread().getName() + " → 출금 완료 (잔액: " + account.getBalance() + ")");
    }

    @Transactional
    public void withdrawWithLock(Long accountId, int amount) {
        Account account = accountRepository.findByIdForLOCK(accountId); // 🔒 락 획득
        log.info(Thread.currentThread().getName() + " → 락 획득 완료");

        account.decrease(amount);
        log.info(Thread.currentThread().getName() + " → 출금 완료 (잔액: " + account.getBalance() + ")");
    }

    // ⚠️ 트랜잭션은 짧게 유지해야 함
}
