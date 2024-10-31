package bestDAOU.PicMessage_backend.repository;

import bestDAOU.PicMessage_backend.entity.Friends;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendsRepository extends JpaRepository<Friends, Long> {
    List<Friends> findByMemberId(Long memberId);
}
