package muzusi.infrastructure.webhook;

import lombok.Builder;

import java.util.List;

@Builder
public record Message (
    List<Embed> embeds
) {
}