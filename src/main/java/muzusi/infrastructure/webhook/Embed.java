package muzusi.infrastructure.webhook;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Embed(
        String title,
        String description,
        Integer color
) {
}