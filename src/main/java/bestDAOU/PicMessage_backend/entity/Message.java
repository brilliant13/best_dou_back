package bestDAOU.PicMessage_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class) // 생성일자 자동 설정을 위한 리스너 등록
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long messageId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Member member; // 발송자

    @ManyToOne
    @JoinColumn(name = "friend_id", nullable = false)
    private Friends friends; // 수신자

    @Column(nullable = false)
    private String text; // 메시지 내용

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate; // 생성일자

}
