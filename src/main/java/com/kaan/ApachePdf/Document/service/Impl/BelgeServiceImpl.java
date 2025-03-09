package com.kaan.ApachePdf.Document.service.Impl;

import com.kaan.ApachePdf.Document.model.Belge;
import com.kaan.ApachePdf.Document.repository.BelgeRepository;
import com.kaan.ApachePdf.Document.service.BelgeService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import jakarta.mail.MessagingException;

@Service
@RequiredArgsConstructor
public class BelgeServiceImpl implements BelgeService {

    private static final Logger logger = LoggerFactory.getLogger(BelgeServiceImpl.class);

    private final BelgeRepository belgeRepository;
    private final JavaMailSender mailSender;

    @Override
    public Belge belgeKaydet(String adSoyad, String email, String belgeTuru) throws MessagingException {
        String belgeNo = UUID.randomUUID().toString();

        Belge belge = Belge.builder()
                .belgeNo(belgeNo)
                .belgeTuru(belgeTuru)
                .adSoyad(adSoyad)
                .email(email)
                .indirmeTarihi(LocalDateTime.now())
                .dogrulandi(false) // ***Doğrulama işlemi burada FALSE olarak ayarlanıyor***
                .build();

        Belge savedBelge = belgeRepository.save(belge);

        return savedBelge;
    }

    @Override
    public boolean belgeDogrula(String belgeNo) {
        Optional<Belge> belgeOpt = belgeRepository.findByBelgeNo(belgeNo);

        if (belgeOpt.isEmpty()) {
            logger.warn("Belge bulunamadı: {}", belgeNo);
            return false;
        }

        Belge mevcutBelge = belgeOpt.get();

        // Eğer belge zaten doğrulandıysa, tekrar işlem yapma
        if (mevcutBelge.isDogrulandi()) {
            logger.info("Bu belge zaten doğrulandı: {}", belgeNo);
            return false;
        }

        // **Sadece kullanıcı doğrulama linkine tıkladığında güncelleniyor**
        mevcutBelge.setDogrulandi(true);
        belgeRepository.save(mevcutBelge);

        logger.info("Belge doğrulandı: {}", belgeNo);
        return true;  // Başarıyla doğrulandı
    }


}
