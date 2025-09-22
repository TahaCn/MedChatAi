package com.taha.medchatai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GeminiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String apiUrl;

    public GeminiService(@Value("${gemini.api.key:dummy-key}") String apiKey) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.apiKey = apiKey;
        this.apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent";
    }

    /**
     * Basit metin üretimi - tüm service'ler için ortak kullanım
     */
    public String generateText(String prompt) {
        try {
            log.debug("Generating text for prompt: {}", prompt.substring(0, Math.min(100, prompt.length())));

            Map<String, Object> requestBody = createRequestBody(prompt);
            HttpEntity<Map<String, Object>> entity = createHttpEntity(requestBody);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    apiUrl + "?key=" + apiKey, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                String result = extractTextFromResponse(response.getBody());
                log.debug("Generated text response length: {}", result.length());
                return result;
            }

            throw new RuntimeException("Gemini API'den geçerli yanıt alınamadı: " + response.getStatusCode());

        } catch (Exception e) {
            log.error("Gemini API çağrısında hata: ", e);
            return "Üzgünüm, şu anda yanıt veremiyorum. Lütfen daha sonra tekrar deneyin. Hata: " + e.getMessage();
        }
    }

    /**
     * Yapılandırılmış JSON yanıt üretimi - DiagnoseService için
     */
    public <T> T generateStructuredText(String prompt, Class<T> responseType) {
        try {
            log.debug("Generating structured text for type: {}", responseType.getSimpleName());

            String enhancedPrompt = prompt + "\n\nÖNEMLİ: Yanıtını sadece geçerli JSON formatında ver. Başka hiçbir metin ekleme.";

            String jsonResponse = generateText(enhancedPrompt);

            // JSON yanıtını temizle (markdown kod blokları vs. varsa)
            String cleanJson = cleanJsonResponse(jsonResponse);

            return objectMapper.readValue(cleanJson, responseType);

        } catch (Exception e) {
            log.error("Yapılandırılmış yanıt oluşturulurken hata: ", e);
            throw new RuntimeException("JSON yanıt işlenirken hata oluştu: " + e.getMessage(), e);
        }
    }

    /**
     * Sohbet geçmişi ile context-aware metin üretimi - PsychService için
     */
    public String generateTextWithHistory(String prompt, List<String> conversationHistory) {
        try {
            StringBuilder contextualPrompt = new StringBuilder();

            if (conversationHistory != null && !conversationHistory.isEmpty()) {
                contextualPrompt.append("Önceki sohbet geçmişi:\n");
                for (int i = 0; i < conversationHistory.size(); i++) {
                    contextualPrompt.append(String.format("[%d] %s\n", i + 1, conversationHistory.get(i)));
                }
                contextualPrompt.append("\n---\n\n");
            }

            contextualPrompt.append(prompt);

            log.debug("Generating text with history context. History size: {}",
                    conversationHistory != null ? conversationHistory.size() : 0);

            return generateText(contextualPrompt.toString());

        } catch (Exception e) {
            log.error("Geçmişli yanıt oluşturulurken hata: ", e);
            return "Sohbet geçmişi işlenirken bir hata oluştu. Lütfen tekrar deneyin.";
        }
    }

    /**
     * Gemini API için request body oluşturur
     */
    private Map<String, Object> createRequestBody(String prompt) {
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> content = new HashMap<>();
        Map<String, Object> part = new HashMap<>();

        part.put("text", prompt);
        content.put("parts", List.of(part));
        requestBody.put("contents", List.of(content));

        return requestBody;
    }

    /**
     * HTTP entity oluşturur
     */
    private HttpEntity<Map<String, Object>> createHttpEntity(Map<String, Object> requestBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(requestBody, headers);
    }

    /**
     * API yanıtından metin çıkarır
     */
    private String extractTextFromResponse(String responseBody) {
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode candidatesNode = rootNode.path("candidates");

            if (candidatesNode.isArray() && candidatesNode.size() > 0) {
                JsonNode firstCandidate = candidatesNode.get(0);
                JsonNode contentNode = firstCandidate.path("content");
                JsonNode partsNode = contentNode.path("parts");

                if (partsNode.isArray() && partsNode.size() > 0) {
                    return partsNode.get(0).path("text").asText();
                }
            }

            throw new RuntimeException("Yanıt yapısı beklenenden farklı");

        } catch (Exception e) {
            log.error("Yanıt parse edilirken hata: ", e);
            throw new RuntimeException("API yanıtı işlenirken hata oluştu", e);
        }
    }

    /**
     * JSON yanıtını temizler (markdown kod blokları, ekstra metinler vs.)
     */
    private String cleanJsonResponse(String jsonResponse) {
        if (jsonResponse == null) {
            return "{}";
        }

        // Markdown kod bloklarını temizle
        String cleaned = jsonResponse.replaceAll("```json\\s*", "").replaceAll("```\\s*", "");

        // JSON'un başlangıç ve bitiş pozisyonlarını bul
        int startIndex = cleaned.indexOf('{');
        int endIndex = cleaned.lastIndexOf('}');

        if (startIndex >= 0 && endIndex > startIndex) {
            cleaned = cleaned.substring(startIndex, endIndex + 1);
        }

        return cleaned.trim();
    }
}