package org.example.plus;

import org.example.plus.common.entity.Account;
import org.example.plus.domain.account.repository.AccountRepository;
import org.example.plus.domain.account.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
public class PessimisticLockTest {

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    AccountService accountService;

    @Test
    void 동시에_트랜잭션_출금_테스트_락없음() {

        // Account 생성
        Account account = new Account(100);
        accountRepository.save(account);

        // 동시에 접근할 수 있는 환경 설정
        // 동시에 3명이 작업 수행
        ExecutorService executor = Executors.newFixedThreadPool(3);

        Runnable task1 = () -> {
            try {
                accountService.withdraw(account.getId(), 10);
            } catch (Exception e) {
                System.out.println("실패");
            }
        };

        Runnable task2 = () -> {
            try {
                accountService.withdraw(account.getId(), 10);
            } catch (Exception e) {
                System.out.println("실패");
            }
        };

        Runnable task3 = () -> {
            try {
                accountService.withdraw(account.getId(), 10);
            } catch (Exception e) {
                System.out.println("실패");
            }
        };

        // executor에게 총 3명의 사용자가 동시에 실행하게 만듦
        // -> task1, task2, task3 실행
        executor.submit(task1);
        executor.submit(task2);
        executor.submit(task3);

        // 지금까지 만들어둔 환경 실행
        executor.shutdown();

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Account result = accountRepository.findById(account.getId()).orElseThrow();

        System.out.println("최종 잔액: " + result.getBalance());
        // 예상값: 70
        // 실제값: 90
    }

    @Test
    void 동시에_트랜잭션_출금_테스트_락있음() {

        // Account 생성
        Account account = new Account(100);
        accountRepository.save(account);

        // 동시에 접근할 수 있는 환경 설정
        // 동시에 3명이 작업 수행
        ExecutorService executor = Executors.newFixedThreadPool(3);

        Runnable task1 = () -> {
            try {
                accountService.withdrawWithLock(account.getId(), 10);
            } catch (Exception e) {
                System.out.println("실패");
            }
        };

        Runnable task2 = () -> {
            try {
                accountService.withdrawWithLock(account.getId(), 10);
            } catch (Exception e) {
                System.out.println("실패");
            }
        };

        Runnable task3 = () -> {
            try {
                accountService.withdrawWithLock(account.getId(), 10);
            } catch (Exception e) {
                System.out.println("실패");
            }
        };

        // executor에게 총 3명의 사용자가 동시에 실행하게 만듦
        // -> task1, task2, task3 실행
        executor.submit(task1);
        executor.submit(task2);
        executor.submit(task3);

        // 지금까지 만들어둔 환경 실행
        executor.shutdown();

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Account result = accountRepository.findById(account.getId()).orElseThrow();

        System.out.println("최종 잔액: " + result.getBalance());
        // 예상값: 70
        // 실제값: 70
    }
}
