package com.kaan.ApachePdf.Document.service.Impl;

import com.kaan.ApachePdf.Document.model.Belge;
import com.kaan.ApachePdf.Document.repository.BelgeRepository;
import com.kaan.ApachePdf.Document.service.BelgeService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BelgeServiceImpl implements BelgeService {

    private static final Logger logger = LoggerFactory.getLogger(BelgeServiceImpl.class);
    private final BelgeRepository belgeRepository;

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

        belgeRepository.save(mevcutBelge);

        logger.info("Belge doğrulandı: {}", belgeNo);
        return true;  // Başarıyla doğrulandı
    }


}
