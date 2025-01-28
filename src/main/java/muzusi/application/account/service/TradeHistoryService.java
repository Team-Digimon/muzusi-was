package muzusi.application.account.service;

import lombok.RequiredArgsConstructor;
import muzusi.application.account.dto.TradeInfoDto;
import muzusi.domain.account.entity.Account;
import muzusi.domain.account.exception.AccountErrorType;
import muzusi.domain.account.service.AccountService;
import muzusi.domain.trade.service.TradeService;
import muzusi.global.exception.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TradeHistoryService {
    private final AccountService accountService;
    private final TradeService tradeService;

    @Transactional(readOnly = true)
    public List<TradeInfoDto> getTradesByAccountId(Long userId, Long accountId) {
        Account account = accountService.readById(accountId)
                .orElseThrow(() -> new CustomException(AccountErrorType.NOT_FOUND));

        if (!account.getUser().getId().equals(userId))
            throw new CustomException(AccountErrorType.UNAUTHORIZED_ACCESS);

        return tradeService.readByAccountId(accountId)
                .stream().map(TradeInfoDto::fromEntity)
                .toList();
    }
}
