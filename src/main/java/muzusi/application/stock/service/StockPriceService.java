package muzusi.application.stock.service;

import lombok.RequiredArgsConstructor;
import muzusi.application.stock.dto.StockPriceDto;
import muzusi.domain.stock.exception.StockErrorType;
import muzusi.domain.stock.service.StockService;
import muzusi.global.exception.CustomException;
import muzusi.global.redis.RedisService;
import muzusi.infrastructure.kis.KisConstant;
import muzusi.infrastructure.kis.KisStockClient;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class StockPriceService {
    private final KisStockClient kisStockClient;
    private final StockService stockService;
    private final RedisService redisService;

    /**
     * 주식 현재가 조회 및 값 저장을 위한 메서드.
     * @param code : 주식 종목 코드
     * @return StockPriceDto : 주식 현재가 DTO
     */
    public StockPriceDto getStockPrice(String code) {
        if (!stockService.existsByStockCode(code)) {
            throw new CustomException(StockErrorType.NOT_PREPARED);
        }

        String key = KisConstant.STOCK_PRICE_PREFIX.getValue() + ":" + code;
        StockPriceDto stockPriceDto = (StockPriceDto) redisService.get(key);

        if (stockPriceDto == null || stockPriceDto.time().isBefore(LocalDateTime.now().minusMinutes(5))) {
            stockPriceDto = kisStockClient.getStockPrice(code);
            redisService.set(key, stockPriceDto);
        }

        return stockPriceDto;
    }
}