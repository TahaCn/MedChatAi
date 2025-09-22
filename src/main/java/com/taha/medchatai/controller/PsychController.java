package com.taha.medchatai.controller;

import com.taha.medchatai.dto.ChatRequest;
import com.taha.medchatai.dto.ChatResponse;
import com.taha.medchatai.entity.User;
import com.taha.medchatai.service.PsychService;
import com.taha.medchatai.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.UUID;

@Controller
@Slf4j
public class PsychController {

    @Autowired
    private PsychService psychService;

    @Autowired
    private UserService userService;

    /**
     * Psikolojik sohbet sayfasını gösterir
     */
    @GetMapping("/psychological-chat")
    public String psychologicalChatPage(Model model, HttpServletRequest request) {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login?error";
        }

        model.addAttribute("user", user);
        return "psychological-chat";
    }

    /**
     * Psikolojik sohbet API endpoint'i
     */
    @PostMapping("/api/chat/psychological")
    @ResponseBody
    public ResponseEntity<ChatResponse> psychologicalChat(@RequestBody ChatRequest request, HttpServletRequest httpRequest) {
        try {
            HttpSession session = httpRequest.getSession();
            User user = (User) session.getAttribute("loggedInUser");

            if (user == null) {
                ChatResponse errorResponse = new ChatResponse();
                errorResponse.setResponse("Lütfen giriş yapın.");
                errorResponse.setSuccess(false);
                return ResponseEntity.status(401).body(errorResponse);
            }

            log.info("Psikolojik sohbet isteği alındı. User: {}, Message: {}",
                    user.getId(),
                    request.getMessage().substring(0, Math.min(50, request.getMessage().length())));

            // Acil durum kontrolü
            boolean isEmergency = psychService.detectEmergency(request.getMessage());

            String sessionId = request.getSessionId();
            if (sessionId == null || sessionId.isEmpty()) {
                sessionId = "psych_" + UUID.randomUUID().toString().substring(0, 8);
            }

            // Sohbet geçmişi ile yanıt oluştur
            String response = psychService.chatWithHistory(user, sessionId, request.getMessage());

            // Response oluştur
            ChatResponse chatResponse = new ChatResponse();
            chatResponse.setResponse(response);
            chatResponse.setSessionId(sessionId);
            chatResponse.setEmergency(isEmergency);
            chatResponse.setSuccess(true);

            // Acil durum mesajı ekle
            if (isEmergency) {
                String emergencyMsg = "⚠️ ACİL DURUM TESPİTİ: Mesajınızda risk belirtileri tespit ettim. " +
                        "Lütfen derhal profesyonel yardım alın.\n\n" +
                        "🚨 Acil durum: 112\n";
                chatResponse.setEmergencyMessage(emergencyMsg);
            }

            log.info("Psikolojik sohbet başarılı. Session: {}, Emergency: {}", sessionId, isEmergency);
            return ResponseEntity.ok(chatResponse);

        } catch (Exception e) {
            log.error("Psikolojik sohbet hatası: ", e);

            ChatResponse errorResponse = new ChatResponse();
            errorResponse.setResponse("Üzgünüm, şu anda teknik bir sorun yaşıyorum. Lütfen daha sonra tekrar deneyin. " +
                    "Acil durumlar için 112'yi arayabilirsiniz.");
            errorResponse.setSuccess(false);
            errorResponse.setSessionId(request.getSessionId());
            errorResponse.setEmergency(false);

            return ResponseEntity.ok(errorResponse);
        }
    }

    /**
     * Kullanıcının geçmiş sohbet oturumlarını listeler
     */
    @GetMapping("/api/chat/sessions")
    @ResponseBody
    public ResponseEntity<?> getUserSessions(HttpServletRequest httpRequest) {
        try {
            HttpSession session = httpRequest.getSession();
            User user = (User) session.getAttribute("loggedInUser");

            if (user == null) {
                return ResponseEntity.status(401).body("Lütfen giriş yapın");
            }

            var sessions = psychService.getUserSessions(user);
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            log.error("Session listesi alınırken hata: ", e);
            return ResponseEntity.badRequest().body("Session listesi alınamadı");
        }
    }
}