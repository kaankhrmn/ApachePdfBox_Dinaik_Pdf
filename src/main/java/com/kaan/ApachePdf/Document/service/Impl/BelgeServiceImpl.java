package com.kaan.ApachePdf.Document.service.Impl;

import com.kaan.ApachePdf.Document.model.Belge;
import com.kaan.ApachePdf.Document.repository.BelgeRepository;
import com.kaan.ApachePdf.Document.service.BelgeService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BelgeServiceImpl implements BelgeService {

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
                .dogrulandi(false)
                .build();

        // Belge veritabanına kaydediliyor
        Belge savedBelge = belgeRepository.save(belge);

        return savedBelge; // E-posta gönderme işlemi yapılmaz, sadece belge kaydedilir
    }

    @Override
    public boolean belgeDogrula(String belgeNo) {
        Optional<Belge> belge = belgeRepository.findByBelgeNo(belgeNo);
        if (belge.isPresent()) {
            Belge mevcutBelge = belge.get();
            if (!mevcutBelge.isDogrulandi()) {
                mevcutBelge.setDogrulandi(true);
                belgeRepository.save(mevcutBelge);
                return true;
            }
        }
        return false;
    }

    public boolean belgeDogrulandiMi(String belgeNo) {
        return belgeRepository.findByBelgeNo(belgeNo)
                .map(Belge::isDogrulandi)
                .orElse(false);
    }
}
