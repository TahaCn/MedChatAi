package com.taha.medchatai.controller;

import com.taha.medchatai.dto.ChatRequest;
import com.taha.medchatai.dto.DiagnosisResult;
import com.taha.medchatai.entity.User;
import com.taha.medchatai.service.DiagnoseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class DiagnoseController {

    @Autowired
    private DiagnoseService diagnoseService;

    @GetMapping("/symptom-analysis")
    public String symptomAnalysis(Model model, HttpServletRequest request) {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login?error";
        }

        model.addAttribute("user", user);
        return "symptom-analysis";
    }

    @PostMapping("/api/diagnose")
    @ResponseBody
    public ResponseEntity<DiagnosisResult> diagnose(@RequestBody ChatRequest request, HttpServletRequest httpRequest) {
        try {
            HttpSession session = httpRequest.getSession();
            User user = (User) session.getAttribute("loggedInUser");

            if (user == null) {
                DiagnosisResult errorResult = new DiagnosisResult();
                errorResult.setAnalysis("Lütfen giriş yapın.");
                return ResponseEntity.status(401).body(errorResult);
            }

            DiagnosisResult result = diagnoseService.analyzeSymptoms(request.getMessage(), user);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            DiagnosisResult errorResult = new DiagnosisResult();
            errorResult.setAnalysis("Üzgünüm, analiz sırasında bir hata oluştu. Lütfen tekrar deneyin.");
            return ResponseEntity.ok(errorResult);
        }
    }
}
