package bestDAOU.PicMessage_backend.repository;

import bestDAOU.PicMessage_backend.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByMemberId(Long memberId);
    List<Message> findByFriendsId(Long friendsId);
}
