package com.taha.medchatai.service;

import com.taha.medchatai.dto.DiagnosisResult;
import com.taha.medchatai.entity.User;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DiagnoseService {

    private final GeminiService geminiService;

    /**
     * Semptomların analizini yaparak DiagnosisResult döner - DiagnoseController için
     */
    public DiagnosisResult analyzeSymptoms(String symptoms, User user) {
        String prompt = buildPromptForStructured(user, symptoms);
        try {
            DiagnosisResult result = geminiService.generateStructuredText(prompt, DiagnosisResult.class);

            // Eğer JSON parsing başarısızsa, basit analiz yap
            if (result == null) {
                result = new DiagnosisResult();
                String simpleAnalysis = geminiService.generateText(buildPrompt(user, symptoms));
                result.setAnalysis(simpleAnalysis);
                result.setEmergencyLevel("LOW");
            }

            return result;
        } catch (Exception e) {
            // Hata durumunda basit analiz döndür
            DiagnosisResult fallback = new DiagnosisResult();
            String simpleAnalysis = geminiService.generateText(buildPrompt(user, symptoms));
            fallback.setAnalysis(simpleAnalysis);
            fallback.setEmergencyLevel("LOW");
            fallback.setDisclaimer("Analiz sırasında bir sorun oluştu, basit analiz yapıldı.");
            return fallback;
        }
    }

    /**
     * Semptomların analizini yaparak yapılandırılmış sonuç döner
     */
    public DiagnosisResult diagnoseStructured(User user, String symptoms) {
        String prompt = buildPromptForStructured(user, symptoms);
        try {
            return geminiService.generateStructuredText(prompt, DiagnosisResult.class);
        } catch (Exception e) {
            // JSON yanıt alınamazsa basit metin yanıtı döndürmek için fallback
            DiagnosisResult fallback = new DiagnosisResult();
            fallback.setDisclaimer("API yanıtı yapılandırılmış formatta alınamadı: " + e.getMessage());
            return fallback;
        }
    }

    /**
     * Metin formatında teşhis yanıtı - önceki versiyon için geriye dönük uyumluluk
     */
    public String diagnose(User user, String symptoms) {
        String prompt = buildPrompt(user, symptoms);
        return geminiService.generateText(prompt);
    }

    /**
     * Basit metin yanıtı için prompt oluşturma
     */
    public String buildPrompt(User user, String symptoms) {
        return "UYARI! Ben bir doktor değilim. Bilgi amaçlı sonuçlar. \n" +
                "Kişi Bilgileri:\n- Yaş: " + user.getAge() + "\n- Cinsiyet: " + user.getGender() +
                "\n- Kronik: " + user.getChronicDiseases() + "\n- Semptomlar:" + symptoms +
                "\n\nLütfen muhtemel tanıları maddeleyerek, olası sebepleri ve acil durumda ne yapması gerektiğini açıkla. Kısa tavsiyeler ver. Eğer acil semptom varsa bunu vurgula.";
    }

    /**
     * JSON formatında yapılandırılmış yanıt için prompt oluşturma
     */
    public String buildPromptForStructured(User user, String symptoms) {
        return "UYARI! Ben bir doktor değilim. Bilgi amaçlı sonuçlar.\n" +
                "Kişi Bilgileri:\n- Yaş: " + user.getAge() + "\n- Cinsiyet: " + user.getGender() +
                "\n- Kronik: " + user.getChronicDiseases() + "\n- Semptomlar: " + symptoms +
                "\n\nLütfen aşağıdaki formatı kullanarak JSON yanıtı ver:\n" +
                "{\n" +
                "  \"analysis\": \"[Semptomların genel analizi ve değerlendirmesi - detaylı açıklama]\",\n" +
                "  \"possibleConditions\": [\n" +
                "    { \"name\": \"[hastalık adı]\", \"likelihood\": \"[low/moderate/high]\", \"description\": \"[kısa açıklama]\" }\n" +
                "  ],\n" +
                "  \"redFlags\": [\"[acil müdahale gerektiren belirti 1]\", \"[acil müdahale gerektiren belirti 2]\"],\n" +
                "  \"urgencyLevel\": \"[self-care/doctor-visit/emergency]\",\n" +
                "  \"emergencyLevel\": \"[LOW/MEDIUM/HIGH]\",\n" +
                "  \"recommendations\": [\"[öneri 1]\", \"[öneri 2]\"],\n" +
                "  \"disclaimer\": \"[yasal uyarı]\"\n" +
                "}\n\n" +
                "Not:\n" +
                "- Analysis alanında semptomların detaylı değerlendirmesini yap\n" +
                "- En olası 3-5 durumu listele\n" +
                "- Acil durum belirtileri varsa mutlaka redFlags listesinde belirt\n" +
                "- Acil durumlarda urgencyLevel'ı 'emergency' ve emergencyLevel'ı 'HIGH' olarak işaretle\n" +
                "- Her zaman klinik değerlendirme önerilmeli";
    }
}
