package bestDAOU.PicMessage_backend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/images")
@CrossOrigin("*") // React 앱의 URL을 명시
public class ImageGenerationController {

    @Value("${gpt.api.key}")
    private String openAiApiKey;

    @PostMapping("/generate")
    public ResponseEntity<Map<String, String>> generateImage(@RequestBody Map<String, String> requestData) {
        // 요청 데이터에서 카테고리 정보 추출
        String style = requestData.get("style");
        String subject = requestData.get("subject");
        String emotion = requestData.get("emotion");
        String background = requestData.get("background");
        String message = requestData.get("message");
        String focus = "Birthday";
        String mood = "evoking a joyous mood in a party setting";

        // DALL-E 3 프롬프트 생성
        String prompt = String.format(
            "Please draw a picture that meets the following [conditions].\n"
                + "\n"
                + "1. There shouldn't be any text in the image.\n"
                + "2. A %s style illustration for a %s purpose, conveying a %s atmosphere in a %s background setting. Absolutely no text, letters, words, or characters anywhere in the picture.",
            style,
            subject,
            emotion,
            background
        );

        System.out.println("프롬프트 내용 = " + prompt);


        // 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + openAiApiKey);

        // 요청 페이로드 설정
        Map<String, Object> payload = new HashMap<>();
        //prompt = "Create an Illustration based on the message: 'Congratulations! Let's celebrate this special day together!' and 'Focusing on Birthday and 'evoking a joyous mood in a party setting.'\n";
        payload.put("prompt", prompt);
        payload.put("n", 1);
        payload.put("size", "256x256");

        // HttpEntity 객체 생성
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        // RestTemplate을 사용하여 OpenAI API 호출
        RestTemplate restTemplate = new RestTemplate();
        String apiUrl = "https://api.openai.com/v1/images/generations"; // DALL-E의 최신 모델을 호출하는 엔드포인트

        try {
            ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                // 응답에서 이미지 URL 추출
                String imageUrl = (String) ((Map<String, Object>) ((List<Object>) response.getBody().get("data")).get(0)).get("url");

                // 성공적으로 생성된 경우 성공 메시지와 URL 반환
                Map<String, String> responseData = new HashMap<>();
                responseData.put("status", "success");
                responseData.put("message", "이미지가 성공적으로 생성되었습니다.");
                responseData.put("imageUrl", imageUrl);
                return ResponseEntity.ok(responseData);
            } else {
                // 실패한 경우 실패 메시지 반환
                Map<String, String> responseData = new HashMap<>();
                responseData.put("status", "error");
                responseData.put("message", "이미지 생성에 실패했습니다. 서버 응답 코드: " + response.getStatusCode());
                return ResponseEntity.status(response.getStatusCode()).body(responseData);
            }
        } catch (Exception e) {
            // 예외 발생 시 에러 메시지 반환
            e.printStackTrace();
            Map<String, String> responseData = new HashMap<>();
            responseData.put("status", "error");
            responseData.put("message", "이미지 생성 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(responseData);
        }
    }
}
