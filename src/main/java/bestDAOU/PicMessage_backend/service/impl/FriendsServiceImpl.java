package bestDAOU.PicMessage_backend.service.impl;

import bestDAOU.PicMessage_backend.dto.FriendsDto;
import bestDAOU.PicMessage_backend.entity.Friends;
import bestDAOU.PicMessage_backend.entity.Member;
import bestDAOU.PicMessage_backend.exception.ResourceNotFoundException;
import bestDAOU.PicMessage_backend.mapper.FriendsMapper;
import bestDAOU.PicMessage_backend.repository.FriendsRepository;
import bestDAOU.PicMessage_backend.repository.MemberRepository;
import bestDAOU.PicMessage_backend.service.FriendsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FriendsServiceImpl implements FriendsService {

    @Autowired
    private FriendsRepository friendsRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Override
    public FriendsDto addFriend(FriendsDto friendsDto, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + memberId));
        Friends friend = FriendsMapper.mapToFriends(friendsDto, member);
        Friends savedFriend = friendsRepository.save(friend);
        return FriendsMapper.mapToFriendsDto(savedFriend);
    }

    @Override
    public FriendsDto getFriendById(Long friendId) {
        Friends friend = friendsRepository.findById(friendId)
                .orElseThrow(() -> new ResourceNotFoundException("Friend not found with id: " + friendId));
        return FriendsMapper.mapToFriendsDto(friend);
    }

    @Override
    public List<FriendsDto> getFriendsByMemberId(Long memberId) {
        return friendsRepository.findByMemberId(memberId).stream()
                .map(FriendsMapper::mapToFriendsDto)
                .collect(Collectors.toList());
    }

    @Override
    public FriendsDto updateFriend(Long friendId, FriendsDto friendsDto) {
        Friends friend = friendsRepository.findById(friendId)
                .orElseThrow(() -> new ResourceNotFoundException("Friend not found with id: " + friendId));
        friend.setTags(friendsDto.getTags());
        friend.setMemos(friendsDto.getMemos());
        friend.setTones(friendsDto.getTones());

        Friends updatedFriend = friendsRepository.save(friend);
        return FriendsMapper.mapToFriendsDto(updatedFriend);
    }

    @Override
    public void deleteFriend(Long friendId) {
        friendsRepository.deleteById(friendId);
    }
}
