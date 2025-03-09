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



    @GetMapping("/verify")
    public ResponseEntity<byte[]> verifyAndDownload(@RequestParam String belgeNo) {
        Optional<Belge> belgeOpt = belgeRepository.findByBelgeNo(belgeNo);

        if (belgeOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Belge bulunamadı.".getBytes());
        }

        Belge belge = belgeOpt.get();

        // Eğer belge zaten doğrulandıysa tekrar doğrulama yapmadan indir
        if (!belge.isDogrulandi()) {
            belge.setDogrulandi(true);
            belgeRepository.save(belge);
        }

        // **Factory Pattern ile ilgili PDF servisini seç**
        PdfGenerator pdfGenerator = PdfFactory.pdfPattern(belge.getBelgeTuru());

        try {
            if ("BasariBelgesi".equalsIgnoreCase(belge.getBelgeTuru())) {
                return pdfGenerator.generateBasariBelgesi(belge.getAdSoyad(), LocalDate.now().toString());
            } else if ("KisiKarti".equalsIgnoreCase(belge.getBelgeTuru())) {
                return pdfGenerator.generateKisiKarti(belge.getAdSoyad(), "Bandırma", "Bandırma", "123456789", "Erkek");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Geçersiz belge türü.".getBytes());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}

