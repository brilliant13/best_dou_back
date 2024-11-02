package bestDAOU.PicMessage_backend.repository;

import bestDAOU.PicMessage_backend.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByMemberId(Long memberId);
    List<Message> findByFriendsId(Long friendsId);

    // memberId와 friendsId로 조회하는 메서드 추가
    List<Message> findByMemberIdAndFriendsId(Long memberId, Long friendsId);
}
