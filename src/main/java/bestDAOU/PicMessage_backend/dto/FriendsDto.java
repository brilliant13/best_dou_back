package bestDAOU.PicMessage_backend.dto;

import bestDAOU.PicMessage_backend.entity.RelationType;
import bestDAOU.PicMessage_backend.entity.Tone;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FriendsDto {
    private Long id;
    private String friendName;
    private String friendPhone;
    private List<String> tags = new ArrayList<>(); // 태그 리스트
    private List<String> memos = new ArrayList<>(); // 메모 리스트
    private List<Tone> tones = new ArrayList<>(); // 어조 리스트 (enum형)
    private Long member_id; // Member와 다대일 관계 설정
    private RelationType relationType; // 관계 유형 (친구, 선배, 부모님 등)
}
