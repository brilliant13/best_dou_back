package bestDAOU.PicMessage_backend.controller;

import bestDAOU.PicMessage_backend.service.RequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ppurio")
public class PpurioController {

    @Autowired
    private final RequestService requestService;

    // 문자 발송 요청 (메시지와 수신자 정보 외부 입력)
    @PostMapping("/send")
    public Map<String, Object> sendMessage() {
        return requestService.requestSend();
    }

    // 예약 취소 요청
    @PostMapping("/cancel")
    public Map<String, Object> cancelMessage() {
        return requestService.requestCancel();
    }
}
