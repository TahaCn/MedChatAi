package com.taha.medchatai.service;

import com.taha.medchatai.dto.MedicineInteractionResult;
import com.taha.medchatai.dto.MedicineInfoResult;
import com.taha.medchatai.entity.User;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class MedicineService {

    private final GeminiService geminiService;

    /**
     * İki ilaç arasındaki etkileşimi yapılandırılmış formatta analiz eder
     */
    public MedicineInteractionResult checkInteractionStructured(User user, String medicineA, String medicineB) {
        String prompt = buildInteractionPrompt(user, medicineA, medicineB);
        try {
            return geminiService.generateStructuredText(prompt, MedicineInteractionResult.class);
        } catch (Exception e) {
            log.error("İlaç etkileşim analizi yapılandırılmış yanıt hatası: ", e);
            MedicineInteractionResult fallback = new MedicineInteractionResult();
            fallback.setHasInteraction(false);
            fallback.setDisclaimer("Yapılandırılmış analiz başarısız. Lütfen eczacı veya doktorunuza danışın.");
            return fallback;
        }
    }

    /**
     * İlaç hakkında detaylı bilgi yapılandırılmış formatta döner - User bilgisi ile
     */
    public MedicineInfoResult getMedicineInfoStructured(User user, String medicineName) {
        String prompt = buildMedicineInfoPromptWithUser(user, medicineName);
        try {
            return geminiService.generateStructuredText(prompt, MedicineInfoResult.class);
        } catch (Exception e) {
            log.error("İlaç bilgisi yapılandırılmış yanıt hatası: ", e);
            MedicineInfoResult fallback = new MedicineInfoResult();
            fallback.setMedicineName(medicineName);
            fallback.setDisclaimer("Yapılandırılmış bilgi alınamadı. Lütfen eczacı veya doktorunuza danışın.");
            return fallback;
        }
    }

    /**
     * İlaç hakkında detaylı bilgi yapılandırılmış formatta döner - sadece ilaç adı ile
     */
    public MedicineInfoResult getMedicineInfoStructured(String medicineName) {
        String prompt = buildMedicineInfoPrompt(medicineName);
        try {
            return geminiService.generateStructuredText(prompt, MedicineInfoResult.class);
        } catch (Exception e) {
            log.error("İlaç bilgisi yapılandırılmış yanıt hatası: ", e);
            MedicineInfoResult fallback = new MedicineInfoResult();
            fallback.setMedicineName(medicineName);
            fallback.setDisclaimer("Yapılandırılmış bilgi alınamadı. Lütfen eczacı veya doktorunuza danışın.");
            return fallback;
        }
    }

    /**
     * Çoklu ilaç etkileşim kontrolü - List döndürür
     */
    public List<MedicineInteractionResult> checkMultipleInteractions(User user, List<String> medications) {
        String prompt = buildMultipleInteractionPrompt(user, medications);
        String response = geminiService.generateText(prompt);

        MedicineInteractionResult result = new MedicineInteractionResult();
        result.setHasInteraction(false);
        result.setSeverity("none");
        result.setDisclaimer("Çoklu ilaç analizi tamamlandı: " + response);

        return List.of(result);
    }

    /**
     * Çoklu ilaç etkileşim kontrolü - String döndürür
     */
    public String checkMultipleInteractionsText(User user, List<String> medicines) {
        if (medicines.size() < 2) {
            return "En az 2 ilaç girmelisiniz.";
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append("Kullanıcı Profili:\n");
        prompt.append("- Yaş: ").append(user.getAge()).append("\n");
        prompt.append("- Cinsiyet: ").append(user.getGender()).append("\n");
        prompt.append("- Kronik Hastalıklar: ").append(user.getChronicDiseases()).append("\n\n");

        prompt.append("İlaç Listesi:\n");
        for (int i = 0; i < medicines.size(); i++) {
            prompt.append(String.format("%d. %s\n", i + 1, medicines.get(i)));
        }

        prompt.append("\nBu ilaçlar arasındaki tüm olası etkileşimleri analiz et:\n");
        prompt.append("- Her ikili etkileşimi kontrol et\n");
        prompt.append("- Ciddiyet derecelerini belirt (hafif/orta/şiddetli)\n");
        prompt.append("- Bu kullanıcı profili için özel riskler var mı?\n");
        prompt.append("- Dikkat edilmesi gereken belirtiler\n");
        prompt.append("- Kullanım önerileri\n\n");
        prompt.append("UYARI: Bu bilgi tıbbi tavsiye değildir. Mutlaka eczacı veya doktorunuza danışın.");

        return geminiService.generateText(prompt.toString());
    }

    public String checkInteraction(User user, String a, String b){
        String prompt = "Kullanıcı: yaş="+user.getAge()+", cinsiyet="+user.getGender()+". İlaç A: "+a+"; İlaç B: "+b+". Bu iki ilaçta etkileşim var mı? Varsa mekanizma ve ciddiyet (hafif/orta/şiddetli) belirt. Hangi hasta gruplarında dikkat gerektiğini yaz. Tıbbi tavsiye değildir.";
        return geminiService.generateText(prompt);
    }

    public String getMedicineInfo(String name){
        String prompt = "İlaç: "+name+". Bu ilacın etken maddesi, kullanım alanları, hangi hastalar için uygun/uygun değil, yaygın ve ciddi yan etkileri kısa şekilde açıkla.";
        return geminiService.generateText(prompt);
    }

    // Private helper methods
    private String buildInteractionPrompt(User user, String medicineA, String medicineB) {
        return "Kullanıcı Profili:\n- Yaş: " + user.getAge() +
               "\n- Cinsiyet: " + user.getGender() +
               "\n- Kronik Hastalıklar: " + user.getChronicDiseases() +
               "\n\nİlaç A: " + medicineA + "\nİlaç B: " + medicineB +
               "\n\nBu iki ilaç arasındaki etkileşimi analiz et ve aşağıdaki JSON formatında yanıt ver:\n" +
               "{\n" +
               "  \"hasInteraction\": true/false,\n" +
               "  \"interactionSeverity\": \"yok/hafif/orta/şiddetli\",\n" +
               "  \"mechanism\": \"etkileşim mekanizması açıklaması\",\n" +
               "  \"symptoms\": [\"belirti1\", \"belirti2\"],\n" +
               "  \"riskGroups\": [\"risk grubu1\", \"risk grubu2\"],\n" +
               "  \"recommendations\": [\"öneri1\", \"öneri2\"],\n" +
               "  \"disclaimer\": \"tıbbi uyarı\"\n" +
               "}\n\nÖzellikle bu kullanıcı profili için riskli durumları vurgula.\nTüm yanıtları Türkçe olarak ver.\nUYARI: Bu analiz tıbbi tavsiye değildir.";
    }

    private String buildMedicineInfoPrompt(String medicineName) {
        return "İlaç: " + medicineName + "\n\nBu ilaç hakkında aşağıdaki JSON formatında detaylı bilgi ver:\n" +
               "{\n" +
               "  \"medicineName\": \"" + medicineName + "\",\n" +
               "  \"activeIngredient\": \"etken madde\",\n" +
               "  \"indications\": [\"kullanım alanı1\", \"kullanım alanı2\"],\n" +
               "  \"contraindications\": [\"kontrendikasyon1\", \"kontrendikasyon2\"],\n" +
               "  \"commonSideEffects\": [\"yaygın yan etki1\", \"yaygın yan etki2\"],\n" +
               "  \"seriousSideEffects\": [\"ciddi yan etki1\", \"ciddi yan etki2\"],\n" +
               "  \"specialGroups\": [\"gebelik durumu\", \"yaşlı hasta uyarıları\"],\n" +
               "  \"disclaimer\": \"tıbbi uyarı\"\n" +
               "}\n\nTüm bilgileri Türkçe olarak ver. Bilgileri güncel ve doğru kaynaklara dayandır.\nUYARI: Bu bilgi tıbbi tavsiye değildir.";
    }

    private String buildMedicineInfoPromptWithUser(User user, String medicineName) {
        return "İlaç Bilgisi Analizi - UYARI: Ben bir eczacı değilim. Bilgi amaçlı sonuçlar.\n" +
                "Kullanıcı: Yaş " + user.getAge() + ", " + user.getGender() +
                (user.getChronicDiseases() != null ? ", Kronik: " + user.getChronicDiseases() : "") + "\n" +
                "İlaç: " + medicineName + "\n\n" +
                "Lütfen bu ilaç hakkında aşağıdaki JSON formatında bilgi ver:\n" +
                "{\n  \"medicineName\": \"[ilaç adı]\",\n  \"activeIngredient\": \"[etken madde]\",\n  \"indications\": [\"[kullanım alanı 1]\"],\n  \"dosage\": \"[dozaj bilgisi]\",\n  \"sideEffects\": [\"[yan etki 1]\"],\n  \"contraindications\": [\"[kontrendikasyon 1]\"],\n  \"warnings\": [\"[uyarı 1]\"],\n  \"disclaimer\": \"[yasal uyarı]\"\n}\n\nTüm yanıtları Türkçe olarak ver. Bu kullanıcı profili için özel uyarılar varsa belirt.";
    }

    private String buildMultipleInteractionPrompt(User user, List<String> medications) {
        return "Çoklu İlaç Etkileşim Analizi - UYARI: Ben bir eczacı değilim.\n" +
                "Kullanıcı: Yaş " + user.getAge() + ", " + user.getGender() + "\n" +
                "İlaçlar: " + String.join(", ", medications) + "\n\n" +
                "Bu ilaçlar arasındaki etkileşimleri analiz et ve önemli uyarıları belirt.";
    }
}
