package com.taha.medchatai.controller;

import com.taha.medchatai.dto.MedicineInteractionResult;
import com.taha.medchatai.dto.MedicineInfoResult;
import com.taha.medchatai.entity.User;
import com.taha.medchatai.service.MedicineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@Slf4j
public class MedicineController {

    @Autowired
    private MedicineService medicineService;

    /**
     İlaç danışmanlığı sayfasını gösterir
     */
    @GetMapping("/medication")
    public String medicationPage(Model model, HttpServletRequest request) {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login?error";
        }

        model.addAttribute("user", user);
        return "medication";
    }

    /**
     * İlaç etkileşim kontrolü API - Yapılandırılmış yanıt
     */
    @PostMapping("/api/medicine/interaction")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkInteraction(
            @RequestParam String medication1,
            @RequestParam String medication2,
            HttpServletRequest httpRequest) {

        try {
            HttpSession session = httpRequest.getSession();
            User user = (User) session.getAttribute("loggedInUser");

            if (user == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lütfen giriş yapın");
                return ResponseEntity.status(401).body(errorResponse);
            }

            log.info("İlaç etkileşim kontrolü: {} + {} - User: {}",
                    medication1, medication2, user != null ? user.getId() : "anonymous");

            MedicineInteractionResult result = medicineService.checkInteractionStructured(user, medication1, medication2);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", result);

            log.info("İlaç etkileşim kontrolü başarılı - Severity: {}", result.getSeverity());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("İlaç etkileşim kontrolü hatası: ", e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "İlaç etkileşim kontrolü sırasında bir hata oluştu: " + e.getMessage());

            return ResponseEntity.ok(errorResponse);
        }
    }

    /**
     * İlaç bilgisi API
     */
    @PostMapping("/api/medicine/info")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMedicineInfo(@RequestParam String medicineName, HttpServletRequest httpRequest) {
        try {
            HttpSession session = httpRequest.getSession();
            User user = (User) session.getAttribute("loggedInUser");

            if (user == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lütfen giriş yapın");
                return ResponseEntity.status(401).body(errorResponse);
            }

            log.info("İlaç bilgisi sorgusu: {} - User: {}", medicineName, user != null ? user.getId() : "anonymous");

            MedicineInfoResult result = medicineService.getMedicineInfoStructured(user, medicineName);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", result);

            log.info("İlaç bilgisi sorgusu başarılı: {}", medicineName);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("İlaç bilgisi sorgusu hatası: ", e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "İlaç bilgisi alınırken bir hata oluştu: " + e.getMessage());

            return ResponseEntity.ok(errorResponse);
        }
    }

    /**
     * Çoklu ilaç etkileşim kontrolü
     */
    @PostMapping("/api/medicine/multiple-interaction")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkMultipleInteraction(@RequestBody List<String> medications, HttpServletRequest httpRequest) {
        try {
            HttpSession session = httpRequest.getSession();
            User user = (User) session.getAttribute("loggedInUser");

            if (user == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lütfen giriş yapın");
                return ResponseEntity.status(401).body(errorResponse);
            }

            log.info("Çoklu ilaç etkileşim kontrolü: {} ilaç - User: {}",
                    medications.size(), user != null ? user.getId() : "anonymous");

            List<MedicineInteractionResult> results = medicineService.checkMultipleInteractions(user, medications);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", results);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Çoklu ilaç etkileşim kontrolü hatası: ", e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Çoklu ilaç etkileşim kontrolü sırasında bir hata oluştu: " + e.getMessage());

            return ResponseEntity.ok(errorResponse);
        }
    }
}
