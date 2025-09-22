# MedChatAI - Akıllı Tıbbi Asistan Sistemi

## 📋 Genel Bakış

MedChatAI, Google Gemini AI teknolojisini kullanarak geliştirilmiş kapsamlı bir tıbbi asistan web uygulamasıdır. Kullanıcılarına semptom analizi, ilaç etkileşim kontrolü ve psikolojik destek hizmetleri sunar.

### 🚀 Kullanılan Teknolojiler

#### Backend
- **Java 17** - Ana programlama dili
- **Spring Boot 3.5.5** - Web framework
- **Spring Security** - Güvenlik ve kimlik doğrulama
- **Spring Data JPA** - Veri erişim katmanı
- **H2 Database** - Geliştirme ortamı için hafif veritabanı
- **Maven** - Proje yönetimi ve bağımlılık yönetimi
- **Lombok** - Kod temizliği için annotation'lar

#### Frontend
- **Thymeleaf** - Server-side template engine
- **Bootstrap 5.1.3** - Responsive UI framework
- **HTML5 & CSS3** - Modern web standartları
- **JavaScript** - Client-side programlama
- **Font Awesome** - İkon kütüphanesi

#### AI Integration
- **Google Gemini AI API** - Doğal dil işleme ve metin üretimi
- **RESTful API** - AI servis entegrasyonu

#### Security
- **Spring Security** - Authentication & Authorization
- **Session Management** - Oturum yönetimi
- **Environment Variables** - API key güvenliği

---

## 🎯 İşlevler ve Metotlar

### 1. 🩺 Semptom Analizi Sistemi (DiagnoseService)

#### Ana İşlevler:
- **Semptom Değerlendirmesi**: Kullanıcıdan alınan semptomları AI ile analiz eder
- **Risk Seviyesi Belirleme**: Acil durum, doktor ziyareti veya kendi kendine bakım önerisi
- **Muhtemel Tanılar**: Semptomların işaret edebileceği hastalık listesi
- **Kişiselleştirilmiş Analiz**: Kullanıcının yaş, cinsiyet ve kronik hastalık bilgilerine göre özelleştirilmiş değerlendirme

#### Kullanılan Metotlar:
```java
// Yapılandırılmış semptom analizi
public DiagnosisResult analyzeSymptoms(String symptoms, User user)

// JSON formatında detaylı analiz
public DiagnosisResult diagnoseStructured(User user, String symptoms)

// Basit metin formatında analiz
public String diagnose(User user, String symptoms)
```

#### Özellikler:
- ✅ Acil durum tespiti ve uyarı sistemi
- ✅ Yaş ve cinsiyet bazlı risk değerlendirmesi
- ✅ Kronik hastalık etkileşim analizi

### 2. 💊 İlaç Bilgi ve Etkileşim Sistemi (MedicineService)

#### Ana İşlevler:
- **İlaç Etkileşim Kontrolü**: İki veya daha fazla ilaç arasındaki etkileşimleri analiz eder
- **İlaç Bilgi Sorgulama**: Detaylı ilaç monografisi ve kullanım bilgileri
- **Kişiselleştirilmiş Uyarılar**: Kullanıcının sağlık profiline göre özel uyarılar
- **Çoklu İlaç Analizi**: Birden fazla ilacın birlikte kullanımının güvenliği

#### Kullanılan Metotlar:
```java
// İki ilaç etkileşim kontrolü
public MedicineInteractionResult checkInteractionStructured(User user, String medicineA, String medicineB)

// İlaç bilgi sorgulama
public MedicineInfoResult getMedicineInfoStructured(User user, String medicineName)

// Çoklu ilaç etkileşim analizi
public List<MedicineInteractionResult> checkMultipleInteractions(User user, List<String> medications)
```

#### Özellikler:
- ✅ Etkileşim şiddeti değerlendirmesi (Hafif/Orta/Şiddetli)
- ✅ Risk grupları tanımlaması
- ✅ Kullanım önerileri
- ✅ Kontrendikasyon uyarıları

### 3. 🧠 Psikolojik Destek Sistemi (PsychService)

#### Ana İşlevler:
- **Duygusal Destek**: AI tabanlı empatik sohbet deneyimi
- **Sohbet Geçmişi**: Önceki konuşmaları hatırlayarak context-aware yanıtlar
- **Ruh Hali Takibi**: Kullanıcının duygusal durumunu analiz eder
- **Kişiselleştirilmiş Öneriler**: Kullanıcının ihtiyaçlarına göre özelleştirilmiş tavsiyeler

#### Kullanılan Metotlar:
```java
// Geçmiş sohbetlerle birlikte yanıt üretme
public String generateResponse(String message, User user)

// Sohbet geçmişini veritabanına kaydetme
public void saveConversation(User user, String userMessage, String aiResponse)

// Kullanıcının sohbet geçmişini getirme
public List<String> getConversationHistory(User user)
```

#### Özellikler:
- ✅ Sohbet geçmişi kaydetme ve geri çağırma
- ✅ Duygusal zeka tabanlı yanıtlar
- ✅ Kriz durumu tespiti
- ✅ Uzun süreli destek imkanı

### 4. 🔧 Ortak AI Servisi (GeminiService)

#### Ana İşlevler:
- **Metin Üretimi**: Tüm servisler için ortak AI metin üretimi
- **Yapılandırılmış Yanıt**: JSON formatında yapılandırılmış AI yanıtları
- **Bağlam Farkındalığı**: Sohbet geçmişi ile context-aware AI

#### Kullanılan Metotlar:
```java
// Basit metin üretimi
public String generateText(String prompt)

// JSON formatında yapılandırılmış yanıt
public <T> T generateStructuredText(String prompt, Class<T> responseType)

// Sohbet geçmişi ile metin üretimi
public String generateTextWithHistory(String prompt, List<String> conversationHistory)
```


## 🛡️ Güvenlik Özellikleri

### Authentication & Authorization
- **Session-based Authentication**: Güvenli oturum yönetimi
- **Password Hashing**: BCrypt ile şifre hashleme
- **CSRF Protection**: Cross-site request forgery koruması
- **Secure Headers**: XSS ve diğer saldırılara karşı koruma

### API Güvenliği
- **Environment Variables**: API key'lerin güvenli saklanması
- **Input Validation**: Girdi doğrulama ve sanitizasyon
- **Error Handling**: Güvenli hata yönetimi

### Data Protection
- **Session Management**: Otomatik oturum sona erdirme
- **Sensitive Data Masking**: Hassas verilerin loglanmaması

---

## 📱 Kullanıcı Arayüzü

### Responsive Design
- **Mobile-First**: Mobil cihaz uyumlu tasarım
- **Bootstrap Grid**: Esnek grid sistemi
- **Modern UI**: Kullanıcı dostu arayüz

### Sayfalar ve İşlevleri
1. **Ana Sayfa (index.html)**: Sistem tanıtımı ve genel bilgiler
2. **Kayıt Ol (register.html)**: Yeni kullanıcı kaydı
3. **Giriş Yap (login.html)**: Kullanıcı girişi
4. **Dashboard (dashboard.html)**: Ana kontrol paneli
5. **Semptom Analizi (symptom-analysis.html)**: AI destekli semptom değerlendirmesi
6. **İlaç Analizi (medication.html)**: İlaç bilgi ve etkileşim sistemi
7. **Psikolojik Destek (psychological-chat.html)**: AI sohbet sistemi

---

## 🔄 API Endpoints

### Authentication Endpoints
```
POST /register - Kullanıcı kaydı
POST /login - Kullanıcı girişi
POST /logout - Çıkış yapma
```

### Medical Analysis Endpoints
```
POST /api/diagnose - Semptom analizi
POST /api/medicine/interaction - İlaç etkileşimi kontrolü
POST /api/medicine/info - İlaç bilgisi sorgulama
```

### Psychological Support Endpoints
```
POST /api/psych/chat - Psikolojik destek sohbeti
GET /api/psych/history - Sohbet geçmişi
```

---

## 🏁 Sonuç

MedChatAI, modern web teknolojileri ve yapay zeka entegrasyonu ile geliştirilmiş kapsamlı bir tıbbi asistan sistemidir. Sistem, kullanıcıların sağlık konularında bilinçli kararlar almasına yardımcı olmak amacıyla tasarlanmıştır.


### ⚠️ Önemli Uyarı:
Bu sistem **TIBBİ TAVSİYE VEREN BİR SİSTEM DEĞİLDİR**. Tüm sonuçlar bilgilendirme amaçlıdır. Sağlık sorunları için mutlaka sağlık personellerine başvurun.

