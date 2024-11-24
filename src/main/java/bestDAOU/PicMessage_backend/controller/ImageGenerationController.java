package bestDAOU.PicMessage_backend.controller;

import java.util.List;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/images")
@CrossOrigin(origins = "http://localhost:3000")
@EnableAsync // 비동기 처리를 활성화
public class ImageGenerationController {

    @Value("${gpt.api.key}")
    private String openAiApiKey;

    private final ImageService imageService;

    public ImageGenerationController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateImage(@RequestBody Map<String, String> requestData) {
        String style = requestData.get("style");
        String keyword = requestData.get("keyword");
        String subject = requestData.get("subject");
        String emotion = requestData.get("emotion");
        String background = requestData.get("background");
        String message = requestData.get("message");

        String promptGPT = "This is a prompt to create an image to be sent with when you send a group text using dall-e-3. Please analyze the prompt and improve it for image generation.\n"
            + "All you have to do is give me an improved prompt";

        // DALL-E 프롬프트 생성
        String prompt = String.format(
            "%s"
                + "Please draw an image that fits the following [conditions] and Image must not display text, labels, people, or characters.\n"
                + "\n"
                + "[conditions]\n"
                + "- Draw it in an %s style.\n"
                + "- Please create the image for %s.\n"
                + "- Set the background to the %s\n"
                + "- Set the overall color to the color for %s\n"
                + "- Set the atmosphere to %s and reflect it in the image\n"
                + "- The purpose of the image is a %s",
            promptGPT, style, keyword, background, keyword, emotion, subject
        );
        //System.out.println(prompt);

        try {
            // 비동기 요청 처리
            List<CompletableFuture<String>> futures = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                futures.add(imageService.generateAndProcessImage(prompt, openAiApiKey));
            }

            // 모든 비동기 작업 완료 후 결과 취합
            List<String> base64Images = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                    .map(CompletableFuture::join) // 비동기 결과를 취합
                    .collect(Collectors.toList()))
                .join();

            // 응답 데이터 생성
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("status", "success");
            responseData.put("message", "이미지가 성공적으로 생성되었습니다.");
            responseData.put("images", base64Images);

            return ResponseEntity.ok(responseData);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("status", "error");
            responseData.put("message", "이미지 생성 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(responseData);
        }
    }
}

@Service
class ImageService {
    private static final String CHATGPT_API_URL = "https://api.openai.com/v1/completions";

    @Async
    public CompletableFuture<String> generateAndProcessImage(String originalPrompt, String apiKey) {
        try {
            // 1. ChatGPT를 호출하여 프롬프트 개선
            String improvedPrompt = getImprovedPromptFromChatGPT(originalPrompt, apiKey);
            System.out.println("ChatGPT 프롬프트 : \n" + improvedPrompt);

            // 2. 개선된 프롬프트를 사용하여 DALL-E 3 호출
            String imageUrl = generateImageFromDallE(improvedPrompt, apiKey);
            System.out.println("개선된 프롬프트 : \n" + imageUrl);

            // 3. 이미지 다운로드 및 처리
            byte[] imageBytes = downloadImage(imageUrl);
            byte[] resizedImageBytes = resizeImage(imageBytes, 300, 300);

            // Base64로 변환하여 반환
            return CompletableFuture.completedFuture(Base64.getEncoder().encodeToString(resizedImageBytes));

        } catch (Exception e) {
            e.printStackTrace();
            return CompletableFuture.failedFuture(e);
        }
    }

    private String getImprovedPromptFromChatGPT(String originalPrompt, String apiKey) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        // ChatGPT API에 맞는 요청 데이터 구성
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", "gpt-4"); // 모델 이름
        payload.put("messages", List.of(
            Map.of(
                "role", "system", "content", "You are a helpful assistant that improves prompts for DALL-E image generation."
            ),
            Map.of(
                "role", "user", "content", originalPrompt
            )
        ));
        payload.put("max_tokens", 300);
        payload.put("temperature", 0.7);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        RestTemplate restTemplate = new RestTemplate();

        // ChatGPT의 올바른 엔드포인트
        String CHATGPT_API_URL = "https://api.openai.com/v1/chat/completions";

        ResponseEntity<Map> response = restTemplate.postForEntity(CHATGPT_API_URL, entity, Map.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null) {
                // ChatGPT 응답에서 메시지를 추출
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }
            throw new RuntimeException("Unexpected response format: " + responseBody);
        } else {
            throw new RuntimeException("ChatGPT 호출 실패: " + response.getStatusCode());
        }
    }

    private String generateImageFromDallE(String prompt, String apiKey) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", "dall-e-3");
        payload.put("prompt", prompt);
        payload.put("n", 1);
        payload.put("size", "1024x1024");

        RestTemplate restTemplate = new RestTemplate();
        String apiUrl = "https://api.openai.com/v1/images/generations";

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            return (String) ((Map<String, Object>) ((List<Object>) response.getBody().get("data")).get(0)).get("url");
        } else {
            throw new RuntimeException("DALL-E 3 호출 실패: " + response.getStatusCode());
        }
    }

    private byte[] downloadImage(String imageUrl) throws Exception {
        URL url = new URL(imageUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ByteArrayInputStream inputStream = new ByteArrayInputStream(connection.getInputStream().readAllBytes())) {
            return inputStream.readAllBytes();
        }
    }

    private byte[] resizeImage(byte[] originalImageBytes, int targetWidth, int targetHeight) throws Exception {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(originalImageBytes);
        BufferedImage originalImage = ImageIO.read(inputStream);

        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, originalImage.getType());
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g.dispose();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(resizedImage, "png", outputStream);

        return outputStream.toByteArray();
    }
}
