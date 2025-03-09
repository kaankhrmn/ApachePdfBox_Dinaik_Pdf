package com.kaan.ApachePdf.Document.controller;

import com.kaan.ApachePdf.Document.model.Belge;
import com.kaan.ApachePdf.Document.model.PdfFactory;
import com.kaan.ApachePdf.Document.model.PdfGenerator;
import com.kaan.ApachePdf.Document.repository.BelgeRepository;
import com.kaan.ApachePdf.Document.service.Impl.BelgeServiceImpl;
import com.kaan.ApachePdf.Document.service.Impl.MailServiceImpl;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequestMapping("api/pdf/generate")
@RequiredArgsConstructor
public class PdfController {

    private final BelgeServiceImpl belgeServiceImpl;
    private final MailServiceImpl mailService;
    private final BelgeRepository belgeRepository;

    // Basarı Belgesi PDF Oluşturma
    @PostMapping("/BasariBelgesi")
    public ResponseEntity<Void> generateBasariBelgesi(
            @RequestParam String ad_soyad,
            @RequestParam String email,
            @RequestParam String tarih
    ) {
        // **1️⃣ Belgeyi kaydediyoruz ve doğrulama maili gönderiyoruz**
        Belge belge = null;
        try {
            belge = belgeServiceImpl.belgeKaydet(ad_soyad, email, "BasariBelgesi");
            String verificationLink = "http://localhost:8080/verify?belgeNo=" + belge.getBelgeNo();  // Doğrulama linki
            mailService.sendVerificationEmail(email, verificationLink);  // Dinamik mail gönderimi
        } catch (MessagingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Mail gönderme hatası
        }

        // **2️⃣ Doğrulama linki gönderildi, kullanıcı doğrulama yaptı mı kontrol edelim**
        String belgeNo = belge.getBelgeNo();
        boolean dogrulamaSonucu = belgeServiceImpl.belgeDogrula(belgeNo);

        if (!dogrulamaSonucu) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null); // ❌ Yetkisiz erişim
        }

        return ResponseEntity.status(HttpStatus.OK).build(); // Başarılı işlem
    }

    // Kisi Kartı PDF Oluşturma
    @PostMapping("/KisiKarti")
    public ResponseEntity<Void> generateKisiKarti(
            @RequestParam String ad_soyad,
            @RequestParam String email,
            @RequestParam String adres,
            @RequestParam String dogumYeri,
            @RequestParam String tckn,
            @RequestParam String cinsiyet
    ) {
        // **1️⃣ Belgeyi kaydediyoruz ve doğrulama maili gönderiyoruz**
        Belge belge = null;
        try {
            belge = belgeServiceImpl.belgeKaydet(ad_soyad, email, "KisiKarti");
            String verificationLink = "http://localhost:8080/verify?belgeNo=" + belge.getBelgeNo();  // Doğrulama linki
            mailService.sendVerificationEmail(email, verificationLink);  // Dinamik mail gönderimi
        } catch (MessagingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Mail gönderme hatası
        }

        // **2️⃣ Doğrulama linki gönderildi, kullanıcı doğrulama yaptı mı kontrol edelim**
        String belgeNo = belge.getBelgeNo();
        boolean dogrulamaSonucu = belgeServiceImpl.belgeDogrula(belgeNo);

        if (!dogrulamaSonucu) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null); // ❌ Yetkisiz erişim
        }

        return ResponseEntity.status(HttpStatus.OK).build(); // Başarılı işlem
    }


    @GetMapping("/downloadBasariBelgesi")
    public ResponseEntity<byte[]> downloadBasariBelgesi(@RequestParam String belgeNo) {
        // **1️⃣ Belge numarası veritabanında var mı kontrol et**
        Optional<Belge> belgeOpt = belgeRepository.findByBelgeNo(belgeNo);

        if (belgeOpt.isEmpty()) {
            // Belge bulunamadığında 404 döndürür ve Swagger'da indirme butonunu görünmez yapar
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(("Belge bulunamadı: " + belgeNo).getBytes());
        }

        // **2️⃣ Belgeyi Veritabanından Çek ve Kullanıcı Bilgilerini Al**
        Belge belge = belgeOpt.get();
        String adSoyad = belge.getAdSoyad();

        // **3️⃣ Factory Pattern ile BasariBelgesi PDF servisini al ve belgeyi oluştur**
        PdfGenerator pdfGenerator = PdfFactory.pdfPattern("BasariBelgesi");

        try {
            // Basari Belgesi için PDF oluşturuluyor, POST'tan gelen bilgileri kullanıyoruz
            return pdfGenerator.generateBasariBelgesi(adSoyad, LocalDate.now().toString());
        } catch (Exception e) {
            // Hata durumunda uygun HTTP kodu ve mesajı döndür
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("PDF oluşturulurken bir hata oluştu.".getBytes());
        }
    }



    // Kisi Kartı PDF İndirme (GET İşlemi)
    @GetMapping("/downloadKisiKarti")
    public ResponseEntity<byte[]> downloadKisiKarti(@RequestParam String belgeNo) {
        // **1️⃣ Belge numarası veritabanında var mı kontrol et**
        Optional<Belge> belgeOpt = belgeRepository.findByBelgeNo(belgeNo);
        if (belgeOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(("Belge bulunamadı: " + belgeNo).getBytes());
        }

        // **2️⃣ Factory Pattern ile Kisi Kartı PDF servisini al ve belgeyi oluştur**
        PdfGenerator pdfGenerator = PdfFactory.pdfPattern("KisiKarti");

        try {
            // Kisi Kartı için PDF oluşturuluyor
            return pdfGenerator.generateKisiKarti("Kaan Kahraman", "Trabzon", "Akçaabat", "123456789", "Erkek");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

