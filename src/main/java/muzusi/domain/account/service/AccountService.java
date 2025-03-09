package muzusi.domain.account.service;

import lombok.RequiredArgsConstructor;
import muzusi.domain.account.entity.Account;
import muzusi.domain.account.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;

    public void save(Account account) {
        accountRepository.save(account);
    }

    public Optional<Account> readById(Long id) {
        return accountRepository.findById(id);
    }

    public List<Account> readAllByUserId(Long userId) {
        return accountRepository.findAllExceptLatestByUserId(userId);
    }

    public Optional<Account> readByUserId(Long userId) {
        return accountRepository.findLatestAccount(userId);
    }

    public List<Account> readAll() {
        return accountRepository.findLatestAccountsForAllUsers();
    }

    public LocalDateTime readCreatedAt(Long userId) {
        return accountRepository.findLatestCreatedAt(userId);
    }
}
