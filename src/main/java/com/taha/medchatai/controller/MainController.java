package com.taha.medchatai.controller;

import com.taha.medchatai.dto.LoginDto;
import com.taha.medchatai.dto.UserRegistrationDto;
import com.taha.medchatai.entity.User;
import com.taha.medchatai.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class MainController {

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login(Model model, @RequestParam(value = "error", required = false) String error,
                       @RequestParam(value = "logout", required = false) String logout) {
        model.addAttribute("loginDto", new LoginDto());

        if (error != null) {
            model.addAttribute("errorMessage", "Geçersiz email veya şifre!");
        }

        if (logout != null) {
            model.addAttribute("successMessage", "Başarıyla çıkış yaptınız!");
        }

        return "login";
    }

    @PostMapping("/login")
    public String loginUser(@ModelAttribute("loginDto") LoginDto loginDto,
                           Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByEmail(loginDto.getEmail());

            if (user == null) {
                model.addAttribute("errorMessage", "Bu email adresi ile kayıtlı kullanıcı bulunamadı!");
                model.addAttribute("loginDto", new LoginDto());
                return "login";
            }

            // Şifre kontrolü
            if (!userService.checkPassword(loginDto.getPassword(), user.getPassword())) {
                model.addAttribute("errorMessage", "Geçersiz şifre!");
                model.addAttribute("loginDto", new LoginDto());
                return "login";
            }

            HttpSession session = request.getSession();
            session.setAttribute("loggedInUser", user);
            session.setAttribute("userEmail", user.getEmail());

            return "redirect:/dashboard";

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Giriş sırasında bir hata oluştu: " + e.getMessage());
            model.addAttribute("loginDto", new LoginDto());
            return "login";
        }
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("userRegistrationDto", new UserRegistrationDto());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("userRegistrationDto") UserRegistrationDto registrationDto,
                              Model model, RedirectAttributes redirectAttributes) {
        try {
            if (userService.existsByEmail(registrationDto.getEmail())) {
                model.addAttribute("errorMessage", "Bu email adresi zaten kayıtlı!");
                return "register";
            }

            userService.registerUser(registrationDto);
            redirectAttributes.addFlashAttribute("successMessage", "Kayıt başarılı! Giriş yapabilirsiniz.");
            return "redirect:/login";

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Kayıt sırasında bir hata oluştu: " + e.getMessage());
            return "register";
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpServletRequest request) {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login?error";
        }

        model.addAttribute("user", user);
        return "dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/login?logout";
    }
}
