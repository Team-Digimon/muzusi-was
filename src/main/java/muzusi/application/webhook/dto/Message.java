package muzusi.application.webhook.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record Message (
    List<Embed> embeds
) {
}