package com.kaan.ApachePdf.Document.controller;

import com.kaan.ApachePdf.Document.model.Belge;
import com.kaan.ApachePdf.Document.model.PdfFactory;
import com.kaan.ApachePdf.Document.model.PdfGenerator;
import com.kaan.ApachePdf.Document.service.Impl.BasariBelgesiImpl;
import com.kaan.ApachePdf.Document.service.Impl.BelgeServiceImpl;
import com.kaan.ApachePdf.Document.service.Impl.KisiKartiImpl;
import com.kaan.ApachePdf.Document.service.Impl.MailServiceImpl;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("api/pdf/generate")
@RequiredArgsConstructor
public class PdfController {

    private final BasariBelgesiImpl basariBelgesi;
    private final KisiKartiImpl kisiKarti;
    private final BelgeServiceImpl belgeServiceImpl;
    private final MailServiceImpl mailService;

    // Basarı Belgesi PDF Oluşturma
    @PostMapping("/BasariBelgesi")
    public ResponseEntity<byte[]> generateBasariBelgesi(
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

        // **3️⃣ Factory Pattern ile ilgili PDF servisini al ve belgeyi oluştur**
        PdfGenerator pdfGenerator = PdfFactory.pdfPattern("BasariBelgesi");
        return pdfGenerator.generateBasariBelgesi(ad_soyad, tarih);
    }

    // Kisi Kartı PDF Oluşturma
    @PostMapping("/KisiKarti")
    public ResponseEntity<byte[]> generateKisiKarti(
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

        // **3️⃣ Factory Pattern ile ilgili PDF servisini al ve belgeyi oluştur**
        PdfGenerator pdfGenerator = PdfFactory.pdfPattern("KisiKarti");
        return pdfGenerator.generateKisiKarti(ad_soyad, adres, dogumYeri, tckn, cinsiyet);
    }

//    @GetMapping("/download")
//    public ResponseEntity<Resource> downloadAndVerify(@RequestParam String belgeNo) {
//        try {
//            // Belge doğrulama işlemi
//            boolean isVerified = belgeServiceImpl.belgeDogrula(belgeNo);
//            if (!isVerified) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                        .body(null);
//            }
//
//            // PDF oluşturma ve indirme işlemi
//            byte[] pdfData = belgeServiceImpl.generatePdf(belgeNo);
//            ByteArrayResource resource = new ByteArrayResource(pdfData);
//
//            return ResponseEntity.ok()
//                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=belge_" + belgeNo + ".pdf")
//                    .contentLength(pdfData.length)
//                    .body(resource);
//
//        } catch (IOException e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(null);
//        }
//    }
}
