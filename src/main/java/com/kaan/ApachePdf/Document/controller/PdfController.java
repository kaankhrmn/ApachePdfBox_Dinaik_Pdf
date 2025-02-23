package com.kaan.ApachePdf.Document.controller;

import com.kaan.ApachePdf.Document.service.Impl.BasariBelgesiImpl;
import com.kaan.ApachePdf.Document.service.Impl.KisiKartiImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/pdf/generate")
public class PdfController {

    private final BasariBelgesiImpl basariBelgesi;
    private final KisiKartiImpl kisiKarti;


    public PdfController(BasariBelgesiImpl basariBelgesi, KisiKartiImpl kisiKarti) {
        this.basariBelgesi = basariBelgesi;
        this.kisiKarti = kisiKarti;
    }

    @PostMapping("/BasariBelgesi")
    public ResponseEntity<byte[]> generateBasariBelgesi(
            @RequestParam String type,
            @RequestParam String ad_soyad,
            @RequestParam String tarih
    ) {
        return basariBelgesi.generateDocument(type, ad_soyad, tarih);
    }

    @PostMapping("/KisiKarti")
    public ResponseEntity<byte[]> generateKisiKarti(
            @RequestParam String type,
            @RequestParam String ad_soyad,
            @RequestParam String adres,
            @RequestParam String dogumYeri,
            @RequestParam String tckn,
            @RequestParam String cinsiyet
    ) {
        return kisiKarti.generateDocument(type, ad_soyad, adres, dogumYeri, tckn, cinsiyet);
    }
}
