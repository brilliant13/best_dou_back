package bestDAOU.PicMessage_backend.service.impl;

import bestDAOU.PicMessage_backend.dto.MessageDto;
import bestDAOU.PicMessage_backend.entity.Member;
import bestDAOU.PicMessage_backend.entity.Friends;
import bestDAOU.PicMessage_backend.entity.Message;
import bestDAOU.PicMessage_backend.exception.ResourceNotFoundException;
import bestDAOU.PicMessage_backend.mapper.MessageMapper;
import bestDAOU.PicMessage_backend.repository.MessageRepository;
import bestDAOU.PicMessage_backend.repository.MemberRepository;
import bestDAOU.PicMessage_backend.repository.FriendsRepository;
import bestDAOU.PicMessage_backend.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private FriendsRepository friendsRepository;

    @Override
    public MessageDto sendMessage(MessageDto messageDto) {
        Member member = memberRepository.findById(messageDto.getMember_id())
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
        Friends friend = friendsRepository.findById(messageDto.getFriends_id())
                .orElseThrow(() -> new ResourceNotFoundException("Friend not found"));

        Message message = MessageMapper.mapToMessage(messageDto, member, friend);
        Message savedMessage = messageRepository.save(message);
        return MessageMapper.mapToMessageDto(savedMessage);
    }

    @Override
    public MessageDto getMessageById(Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with id: " + messageId));
        return MessageMapper.mapToMessageDto(message);
    }

    @Override
    public List<MessageDto> getMessagesByMemberId(Long memberId) {
        return messageRepository.findByMemberId(memberId).stream()
                .map(MessageMapper::mapToMessageDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<MessageDto> getMessagesByFriendId(Long friendId) {
        return messageRepository.findByFriendsId(friendId).stream()
                .map(MessageMapper::mapToMessageDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteMessage(Long messageId) {
        messageRepository.deleteById(messageId);
    }

    // memberId와 friendsId를 동시에 조회하는 메서드 구현
    @Override
    public List<MessageDto> getMessagesByMemberIdAndFriendsId(Long memberId, Long friendsId) {
        List<Message> messages = messageRepository.findByMemberIdAndFriendsId(memberId, friendsId);
        return messages.stream()
                .map(MessageMapper::mapToMessageDto) // Message를 MessageDto로 변환
                .collect(Collectors.toList());
    }
}
