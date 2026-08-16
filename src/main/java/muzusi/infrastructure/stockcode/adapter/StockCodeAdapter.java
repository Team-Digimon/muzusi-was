package muzusi.infrastructure.stockcode.adapter;

import lombok.RequiredArgsConstructor;
import muzusi.application.stockcode.exception.StockCodeException;
import muzusi.application.stockcode.port.StockCodePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class StockCodeAdapter implements StockCodePort {
    @Value("${data.stock-code-size}")
    private int stockCodeSize;
    
    @Value("${data.stock-code-path}")
    private String stockCodePath;
    
    /**
     * 주식 종목 코드 목록을 반환하는 메서드
     *
     * <p> {@link #stockCodePath} 경로로 부터 파일을 읽어 주식 종목 코드를 반환한다.
     *
     * @return  주식 종목 코드 목록
     */
    @Override
    public List<String> getAllStockCodes() {
        ClassPathResource source = new ClassPathResource(stockCodePath);
        List<String> result = new ArrayList<>(stockCodeSize);
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(source.getInputStream()))) {
            String code = null;
            
            while ((code = br.readLine()) != null) {
                result.add(code);
            }
        } catch (IOException e) {
            throw new StockCodeException("주식 종목 코드를 불러오는데 실패하였습니다.", e);
        }
        
        return result;
    }
}
