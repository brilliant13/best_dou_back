package bestDAOU.PicMessage_backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.RandomStringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class RequestService {

    @Value("${ppurio.api.key}")
    private String PpurioAiApiKey;

    private static final Integer TIME_OUT = 5000;
    private String API_KEY = PpurioAiApiKey;
    private static final String PPURIO_ACCOUNT = "jjww6367";
    private static final String FROM = "01076826007";
    private static final String FILE_PATH = "src/main/resources/static/animal.jpg";
    private static final String URI = "https://message.ppurio.com";

    public Map<String, Object> requestSend() {
        String basicAuthorization = Base64.getEncoder().encodeToString((PPURIO_ACCOUNT + ":" + API_KEY).getBytes());

        Map<String, Object> tokenResponse = getToken(URI, basicAuthorization); // 토큰 발급
        Map<String, Object> sendResponse = send(URI, (String) tokenResponse.get("token")); // 발송 요청

        return sendResponse;
    }

    public Map<String, Object> requestCancel() {
        String basicAuthorization = Base64.getEncoder().encodeToString((PPURIO_ACCOUNT + ":" + API_KEY).getBytes());

        Map<String, Object> tokenResponse = getToken(URI, basicAuthorization); // 토큰 발급
        Map<String, Object> cancelResponse = cancel(URI, (String) tokenResponse.get("token")); // 예약 취소 요청

        return cancelResponse;
    }


    /**
     * Access Token 발급 요청 (한 번 발급된 토큰은 24시간 유효합니다.)
     * @param baseUri 요청 URI ex) https://message.ppurio.com
     * @param BasicAuthorization "계정:연동 개발 인증키"를 Base64 인코딩한 문자열
     * @return Map
     */
    private Map<String, Object> getToken(String baseUri, String BasicAuthorization) {
        HttpURLConnection conn = null;
        try {
            Request request = new Request(baseUri + "/v1/token", "Basic " + BasicAuthorization);
            conn = createConnection(request);
            return getResponseBody(conn);
        } catch (IOException e) {
            throw new RuntimeException("API 요청과 응답 실패", e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * 문자 발송 요청
     * @param baseUri 요청 URI ex) https://message.ppurio.com
     * @param accessToken 토큰 발급 API를 통해 발급 받은 Access Token
     * @return Map
     */
    private Map<String, Object> send(String baseUri, String accessToken) {
        HttpURLConnection conn = null;
        try {
            String bearerAuthorization = String.format("%s %s", "Bearer", accessToken);
            //String bearerAuthorization = String.format("Bearer %s", accessToken);
            Request httpRequest = new Request(baseUri + "/v1/message", bearerAuthorization);
            conn = createConnection(httpRequest, createSendTestParams()); // request 객체를 이용
            return getResponseBody(conn);
        } catch (IOException e) {
            throw new RuntimeException("API 요청과 응답 실패", e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }


    /**
     * 예약발송 취소 요청
     * @param baseUri 요청 URI ex) https://message.ppurio.com
     * @param accessToken 토큰 발급 API를 통해 발급 받은 Access Token
     * @return Map
     */
    private Map<String, Object> cancel(String baseUri, String accessToken) {
        HttpURLConnection conn = null;
        try {
            String token = String.format("%s %s", "Bearer", accessToken);
            //String token = String.format("Bearer %s", accessToken);
            Request request = new Request(baseUri + "/v1/cancel", token);
            conn = createConnection(request, createCancelTestParams());
            return getResponseBody(conn);
        } catch (IOException e) {
            throw new RuntimeException("API 요청과 응답 실패", e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private <T> HttpURLConnection createConnection(Request request, T requestObject) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonInputString = objectMapper.writeValueAsString(requestObject);
        HttpURLConnection connect = createConnection(request);
        connect.setDoOutput(true);

        try (OutputStream os = connect.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        return connect;
    }

    private HttpURLConnection createConnection(Request request) throws IOException {
        URL url = new URL(request.getRequestUri());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", request.getAuthorization());
        conn.setConnectTimeout(TIME_OUT);
        conn.setReadTimeout(TIME_OUT);
        return conn;
    }

    private Map<String, Object> getResponseBody(HttpURLConnection conn) {
        InputStream inputStream;
        try {
            int responseCode = conn.getResponseCode();
            inputStream = (responseCode == 200) ? conn.getInputStream() : conn.getErrorStream();
        } catch (IOException e) {
            throw new RuntimeException("API 응답 코드 확인 실패", e);
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String inputLine;
            StringBuilder responseBody = new StringBuilder();
            while ((inputLine = br.readLine()) != null) {
                responseBody.append(inputLine);
            }
            return convertJsonToMap(responseBody.toString());
        } catch (IOException e) {
            throw new RuntimeException("API 응답을 읽는 데 실패했습니다.", e);
        }
    }

    private Map<String, Object> convertJsonToMap(String jsonString) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 변환 실패: " + jsonString, e);
        }
    }


    private Map<String, Object> createSendTestParams() throws IOException {
        HashMap<String, Object> params = new HashMap<>();
        params.put("account", PPURIO_ACCOUNT);
        params.put("messageType", "SMS");
        params.put("from", FROM);
        params.put("content", "[*이름*], hello this is [*1*]");
        params.put("duplicateFlag", "Y");
        params.put("rejectType", "AD");
        params.put("targetCount", 1);
        params.put("targets", List.of(
                Map.of("to", "01076826007",
                        "name", "tester",
                        "changeWord", Map.of(
                                "var1", "ppurio api world"))
        ));
        params.put("files", List.of(
                createFileTestParams(FILE_PATH)
        ));
        params.put("refKey", RandomStringUtils.random(32, true, true));
        return params;
    }

    private Map<String, Object> createFileTestParams(String filePath) throws IOException {
        File file = new File(filePath);

        if (!file.exists() || !file.isFile()) {
            throw new FileNotFoundException("파일이 존재하지 않거나 유효하지 않습니다: " + filePath);
        }

        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            byte[] fileBytes = new byte[(int) file.length()];
            int readBytes = fileInputStream.read(fileBytes);

            if (readBytes != file.length()) {
                throw new IOException("파일을 완전히 읽지 못했습니다.");
            }

            String encodedFileData = Base64.getEncoder().encodeToString(fileBytes);
            Map<String, Object> params = new HashMap<>();
            params.put("size", file.length());
            params.put("name", file.getName());
            params.put("data", encodedFileData);
            return params;
        }
    }



    private Map<String, Object> createCancelTestParams() {
        HashMap<String, Object> params = new HashMap<>();
        params.put("account", PPURIO_ACCOUNT);
            params.put("messageKey", "230413110135117SMS029914servsUBn");
        return params;
    }
}

class Request {
    private String requestUri;
    private String authorization;

    public Request(String requestUri, String authorization) {
        this.requestUri = requestUri;
        this.authorization = authorization;
    }

    public String getRequestUri() {
        return requestUri;
    }

    public String getAuthorization() {
        return authorization;
    }
}
