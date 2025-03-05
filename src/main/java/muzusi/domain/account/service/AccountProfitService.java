package muzusi.domain.account.service;

import lombok.RequiredArgsConstructor;
import muzusi.domain.account.entity.AccountProfit;
import muzusi.domain.account.repository.AccountProfitRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountProfitService {
    private final AccountProfitRepository accountProfitRepository;

    public void saveAll(List<AccountProfit> accountProfits) {
        accountProfitRepository.saveAllInBatch(accountProfits);
    }

    public List<AccountProfit> readByAccountId(Long accountId) {
        return accountProfitRepository.findByAccount_idOrderByCreatedAtDesc(accountId, PageRequest.of(0, 14));
    }

    public void deleteByDateTime(LocalDate dateTime) {
        accountProfitRepository.deleteByDateTimeBefore(dateTime);
    }
}
