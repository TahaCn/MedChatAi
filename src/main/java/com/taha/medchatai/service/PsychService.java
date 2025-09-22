package com.taha.medchatai.service;

import com.taha.medchatai.entity.ConversationHistory;
import com.taha.medchatai.entity.User;
import com.taha.medchatai.repository.ConversationHistoryRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class PsychService {

    private final GeminiService geminiService;
    private final ConversationHistoryRepository conversationHistoryRepository;

    /**
     * Geçmiş sohbet desteği ile psikolojik destek sohbeti
     */
    public String chatWithHistory(User user, String sessionId, String message) {
        // Eğer session ID yoksa yeni bir tane oluştur
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = generateSessionId();
        }

        // Son 10 mesajı getir (performans için sınırlı)
        List<ConversationHistory> recentHistory = conversationHistoryRepository
                .findLastNMessages(user, sessionId, 10);

        // Geçmiş mesajları string formatına çevir
        List<String> historyStrings = recentHistory.stream()
                .map(ch -> String.format("[%s] %s", ch.getMessageType(), ch.getMessageContent()))
                .collect(Collectors.toList());

        // Kullanıcı mesajını kaydet
        saveMessage(user, sessionId, "USER", message);

        // AI yanıtını oluştur
        String prompt = buildPsychSupportPrompt(user, historyStrings, message);
        String response = geminiService.generateTextWithHistory(prompt, historyStrings);

        // AI yanıtını kaydet
        saveMessage(user, sessionId, "ASSISTANT", response);

        log.info("Psikolojik destek sohbeti tamamlandı. User: {}, Session: {}", user.getId(), sessionId);
        return response;
    }

    /**
     * Basit sohbet (geçmiş olmadan) - geriye dönük uyumluluk
     */
    public String chat(User user, List<String> history, String message) {
        String prompt = "Sen empatik bir destekleyici sohbet botusun. Kullanıcı: " + user.getFirstName() +
                       "\nKonuşma geçmişi:\n" + String.join("\n", history) +
                       "\nYeni mesaj:\n" + message +
                       "\nNazik, destekleyici ve yönlendirici cevap ver. Eğer intihar/akut risk belirtileri varsa acil yardım çağrılmasını öner.";
        return geminiService.generateText(prompt);
    }

    /**
     * Kullanıcının mevcut sohbet oturumlarını listele
     */
    public List<String> getUserSessions(User user) {
        return conversationHistoryRepository.findDistinctSessionIdsByUser(user);
    }

    /**
     * Belirli bir oturumdaki son N mesajı getir
     */
    public List<ConversationHistory> getSessionHistory(User user, String sessionId, int limit) {
        return conversationHistoryRepository.findLastNMessages(user, sessionId, limit);
    }

    /**
     * Acil durum tespiti - risk kelimelerini kontrol eder
     */
    public boolean detectEmergency(String message) {
        String lowerMessage = message.toLowerCase();
        String[] riskKeywords = {
            "intihar", "ölmek", "bitir", "kendime zarar",
            "yaşamak istemiyorum", "çaresizim", "umutsuzum", "son vermek"
        };

        for (String keyword : riskKeywords) {
            if (lowerMessage.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Ruh hali analizi için özel prompt
     */
    public String analyzeMood(User user, String message) {
        String prompt = String.format("""
            Kullanıcı: %s (%d yaş)
            Mesaj: %s
            
            Bu mesajdan kullanıcının ruh halini analiz et:
            - Genel duygu durumu (1-10 skala)
            - Tespit edilen duygular
            - Risk faktörleri (varsa)
            - Destekleyici öneriler
            - Professional yardım gerekip gerekmediği
            
            Empati ile yaklaş ve yapıcı önerilerde bulun.
            """, user.getFirstName(), user.getAge(), message);

        return geminiService.generateText(prompt);
    }

    private String buildPsychSupportPrompt(User user, List<String> history, String message) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Sen deneyimli, empatik bir psikolojik destek uzmanısın.\n\n");
        prompt.append("Kullanıcı Profili:\n");
        prompt.append("- İsim: ").append(user.getFirstName()).append("\n");
        prompt.append("- Yaş: ").append(user.getAge()).append("\n\n");

        if (!history.isEmpty()) {
            prompt.append("Önceki Sohbet Geçmişi:\n");
            for (String historyItem : history) {
                prompt.append(historyItem).append("\n");
            }
            prompt.append("\n");
        }

        prompt.append("Yeni Mesaj: ").append(message).append("\n\n");

        prompt.append("Yanıtın şu özelliklere sahip olmalı:\n");
        prompt.append("- Empatik ve anlayışlı\n");
        prompt.append("- Yargılayıcı olmayan\n");
        prompt.append("- Destekleyici ve umut verici\n");
        prompt.append("- Pratik önerileri içeren\n");
        prompt.append("- Geçmiş sohbeti dikkate alan\n");
        prompt.append("- Eğer ciddi risk belirtileri varsa professional yardım öner\n\n");

        // Acil durum kontrolü
        if (detectEmergency(message)) {
            prompt.append("ÖNEMLİ: Bu mesajda risk belirtileri tespit edildi. ");
            prompt.append("Acil professional yardım alınmasını nazikçe öner.\n");
            prompt.append("Acil durum hatları: 112, Türkiye Psikiyatri Derneği: 444 7 999\n\n");
        }

        prompt.append("Sıcak, destekleyici ve professional bir yanıt ver.");

        return prompt.toString();
    }

    private void saveMessage(User user, String sessionId, String messageType, String content) {
        try {
            ConversationHistory history = new ConversationHistory();
            history.setUser(user);
            history.setSessionId(sessionId);
            history.setMessageType(messageType);
            history.setMessageContent(content);
            conversationHistoryRepository.save(history);
        } catch (Exception e) {
            log.error("Sohbet geçmişi kaydedilirken hata: ", e);
        }
    }

    private String generateSessionId() {
        return "psych_" + UUID.randomUUID().toString().substring(0, 8);
    }
}
