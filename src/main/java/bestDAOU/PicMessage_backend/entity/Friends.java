package bestDAOU.PicMessage_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Friends {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String friendName;

    @Column(nullable = false)
    private String friendPhone;

    @ElementCollection
    private List<String> tags = new ArrayList<>(); // 태그 리스트

    @ElementCollection
    private List<String> memos = new ArrayList<>(); // 메모 리스트

    @ElementCollection
    @Enumerated(EnumType.STRING)
    private List<Tone> tones = new ArrayList<>(); // 어조 리스트 (enum형)

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member; // Member와 다대일 관계 설정

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RelationType relationType; // 관계 유형 (친구, 선배, 부모님 등)

}
