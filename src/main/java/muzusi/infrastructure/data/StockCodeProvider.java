package muzusi.infrastructure.data;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class StockCodeProvider {
    private static final int CODE_COUNT = 2750;
    @Value("${data.stock-code-path}")
    String stockCodePath;

    public List<String> getAllStockCodes() {
        ClassPathResource resource = new ClassPathResource(stockCodePath);
        List<String> list = new ArrayList<>(CODE_COUNT);

        try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            String code;
            while ((code = br.readLine()) != null) {
                list.add(code);
            }
        } catch (IOException e) {
            log.error("주식 종목 코드 파일을 열 수 없습니다. / {}", e.getMessage());
        }
        return list;
    }
}