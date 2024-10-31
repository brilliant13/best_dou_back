package bestDAOU.PicMessage_backend.controller;

import bestDAOU.PicMessage_backend.dto.MessageDto;
import bestDAOU.PicMessage_backend.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @PostMapping
    public ResponseEntity<MessageDto> sendMessage(@RequestBody MessageDto messageDto) {
        MessageDto savedMessage = messageService.sendMessage(messageDto);
        return new ResponseEntity<>(savedMessage, HttpStatus.CREATED);
    }

    @GetMapping("{id}")
    public ResponseEntity<MessageDto> getMessageById(@PathVariable("id") Long messageId) {
        MessageDto messageDto = messageService.getMessageById(messageId);
        return ResponseEntity.ok(messageDto);
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<MessageDto>> getMessagesByMemberId(@PathVariable("memberId") Long memberId) {
        List<MessageDto> messages = messageService.getMessagesByMemberId(memberId);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/friend/{friendId}")
    public ResponseEntity<List<MessageDto>> getMessagesByFriendId(@PathVariable("friendId") Long friendId) {
        List<MessageDto> messages = messageService.getMessagesByFriendId(friendId);
        return ResponseEntity.ok(messages);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<String> deleteMessage(@PathVariable("id") Long messageId) {
        messageService.deleteMessage(messageId);
        return ResponseEntity.ok("Message deleted successfully.");
    }
}
