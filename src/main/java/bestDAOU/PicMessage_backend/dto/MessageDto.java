package bestDAOU.PicMessage_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MessageDto {
    private Long messageId;
    private Long member_id; // 발송자
    private Long friends_id; // 수신자
    private String text; // 메시지 내용
    private LocalDateTime createdDate; // 생성일자
}
