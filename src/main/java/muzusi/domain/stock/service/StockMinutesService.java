package muzusi.domain.stock.service;

import lombok.RequiredArgsConstructor;
import muzusi.application.stock.dto.StockChartInfoDto;
import muzusi.domain.stock.entity.StockMinutes;
import muzusi.domain.stock.repository.StockMinutesCacheRepository;
import muzusi.domain.stock.repository.StockMinutesRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockMinutesService {
    private final StockMinutesRepository stockMinutesRepository;
    private final StockMinutesCacheRepository stockMinutesCacheRepository;

    public void save(StockMinutes stockMinutes) {
        stockMinutesRepository.save(stockMinutes);
    }

    public void saveAll(List<StockMinutes> stockMinutesList) {
        stockMinutesRepository.saveAll(stockMinutesList);
    }

    public List<StockMinutes> readByStockCode(String stockCode) {
        return stockMinutesRepository.findByStockCodeOrderByDateAsc(stockCode);
    }

    public void deleteByDateBefore(LocalDate date) {
        stockMinutesRepository.deleteByDateBefore(date);
    }

    public void saveAllInCache(Collection<StockChartInfoDto> stockChartInfoList) {
        stockMinutesCacheRepository.saveAll(stockChartInfoList);
    }

    public List<StockChartInfoDto> readAllInCache(String stockCode) {
        return stockMinutesCacheRepository.findAll(stockCode);
    }

    public void deleteInCache(String stockCode) {
        stockMinutesCacheRepository.delete(stockCode);
    }
}