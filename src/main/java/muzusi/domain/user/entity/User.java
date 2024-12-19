package muzusi.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import muzusi.domain.user.type.OAuthPlatform;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity(name = "user")
@DynamicUpdate
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE user SET deleted_at = NOW() WHERE id = ?")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String nickname;

    @Column(name = "attempt_count")
    private int attemptCount;

    @ColumnDefault("NULL")
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Enumerated(EnumType.STRING)
    private OAuthPlatform platform;

    @Builder
    public User(String username, String nickname, OAuthPlatform platform) {
        this.username = username;
        this.nickname = nickname;
        this.platform = platform;
    }
}