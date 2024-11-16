package bestDAOU.PicMessage_backend.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/images")
@CrossOrigin(origins = "http://localhost:3000") // React 앱의 URL을 명시
public class ImageGenerationController {

    @Value("${openai.api.key}")
    private String openAiApiKey;

    @PostMapping("/generate")
    public ResponseEntity<Map<String, String>> generateImage(@RequestBody Map<String, String> requestData) {
        // 요청 데이터에서 카테고리 정보 추출
        String style = requestData.get("style");
        String subject = requestData.get("subject");
        String emotion = requestData.get("emotion");
        String background = requestData.get("background");
        String message = requestData.get("message");

        // DALL-E 3 프롬프트 생성
        String prompt = String.format(
            "Please draw an image that fits the following [conditions].\n"
                + "\n"
                + "[conditions]\n"
                + "- Draw it in an realistic style.\n"
                + "- Please create the image for Apartment.\n"
                + "- No text, labels, or any written characters should appear in the image.\n"
                + "- Set the overall color to the color for Apartment\n"
                + "- Set the atmosphere to 'happy' and reflect it in the image",
            style, emotion, background, message
        );

        System.out.println(prompt);

        // 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + openAiApiKey);

        // 요청 페이로드 설정
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", "dall-e-3"); // DALL-E 3 모델 지정
        payload.put("prompt", prompt);
        payload.put("n", 1);
        payload.put("size", "1024x1024");
        payload.put("quality", "standard");

        // HttpEntity 객체 생성
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        // RestTemplate을 사용하여 OpenAI API 호출
        RestTemplate restTemplate = new RestTemplate();
        String apiUrl = "https://api.openai.com/v1/images/generations"; // OpenAI DALL-E 3 API 엔드포인트

        try {
            ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                // 응답에서 이미지 URL 추출
                String imageUrl = (String) ((Map<String, Object>) ((List<Object>) response.getBody().get("data")).get(0)).get("url");

                // 이미지 URL에서 이미지 다운로드
                byte[] imageBytes = downloadImage(imageUrl);

                // 이미지 크기 조절
                byte[] resizedImageBytes = resizeImage(imageBytes, 300, 300);
                System.out.println("이미지 사이즈 변경");

                // Base64로 변환
                String base64Image = Base64.getEncoder().encodeToString(resizedImageBytes);
                System.out.println("Base64로 변환");

                // 성공적으로 생성된 경우 성공 메시지와 Base64 반환
                Map<String, String> responseData = new HashMap<>();
                responseData.put("status", "success");
                responseData.put("message", "이미지가 성공적으로 생성되었습니다.");
                responseData.put("imageBase64", base64Image);
                System.out.println("이미지 생성 완료");
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

    private byte[] downloadImage(String imageUrl) throws Exception {
        URL url = new URL(imageUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        System.out.println("이미지 다운로드");
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ByteArrayInputStream inputStream = new ByteArrayInputStream(connection.getInputStream().readAllBytes())) {
            return inputStream.readAllBytes();
        }
    }

    private byte[] resizeImage(byte[] originalImageBytes, int targetWidth, int targetHeight) throws Exception {
        // InputStream으로 변환
        ByteArrayInputStream inputStream = new ByteArrayInputStream(originalImageBytes);
        BufferedImage originalImage = ImageIO.read(inputStream);

        // 새로운 크기의 BufferedImage 생성
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, originalImage.getType());
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g.dispose();

        // 다시 ByteArrayOutputStream으로 변환
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(resizedImage, "png", outputStream);

        return outputStream.toByteArray();
    }
}
