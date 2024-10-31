package bestDAOU.PicMessage_backend.service;

import bestDAOU.PicMessage_backend.dto.MessageDto;

import java.util.List;

public interface MessageService {
    MessageDto sendMessage(MessageDto messageDto);
    MessageDto getMessageById(Long messageId);
    List<MessageDto> getMessagesByMemberId(Long memberId);
    List<MessageDto> getMessagesByFriendId(Long friendId);
    void deleteMessage(Long messageId);
}
