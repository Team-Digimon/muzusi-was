package muzusi.application.news.port;

import java.util.List;
import java.util.Map;

public interface FetchNewsPort {
    List<Map<String, String>> getNews(String query);
}
