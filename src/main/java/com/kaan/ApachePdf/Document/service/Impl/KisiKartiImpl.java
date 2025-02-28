package com.kaan.ApachePdf.Document.service.Impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.kaan.ApachePdf.Document.model.CreatePdf;
import com.kaan.ApachePdf.Document.model.PdfPattern;
import com.kaan.ApachePdf.Document.service.KisiKartiService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

@Service
public class KisiKartiImpl implements PdfPattern, KisiKartiService {

    String belge_sahibi = "Kaan Kahraman";
    String belge_tarihi = new Date().toString();
    String belgeBilgisi = "Bu belge " + belge_tarihi + " ,  " + belge_sahibi + " tarafindan olusturulmustur.";

    @Override
    public ResponseEntity<byte[]> generateDocument(String type, String ad_soyad, String adres, String dogumYeri, String tckn, String cinsiyet) {
        try {
            PdfPattern pdfPattern = CreatePdf.pdfPattern(type);
            return pdfPattern.generateKisiKarti(ad_soyad, adres, dogumYeri, tckn, cinsiyet);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<byte[]> generateKisiKarti(String ad_soyad, String adres, String dogumYeri, String tckn, String cinsiyet) {

        try{
            ClassPathResource pdftemplate = new ClassPathResource("templates/KisiKarti.pdf");
            InputStream inputStream = pdftemplate.getInputStream();
            PDDocument document = PDDocument.load(inputStream);
            PDPage page = document.getPage(0);


            PDPageContentStream  contentStream = new PDPageContentStream(document, page,PDPageContentStream.AppendMode.APPEND, true);
            File fontFie = new File("src/main/resources/templates/arial.ttf");
            PDType0Font font = PDType0Font.load(document,fontFie);
            contentStream.setFont(font, 13);
            contentStream.setNonStrokingColor(Color.BLACK);

            contentStream.beginText();
            contentStream.newLineAtOffset(480, 355);
            contentStream.showText(ad_soyad);
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(480, 300);
            contentStream.showText(dogumYeri);
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(480, 240);
            contentStream.showText(tckn);
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(480, 185);
            contentStream.showText(cinsiyet);
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(480, 130);
            contentStream.showText(adres);
            contentStream.endText();

            //QR KOD OLUŞTUR VE PDF'YE EKLE
            PDImageXObject qrImage = createQrCodeImage(document, belgeBilgisi, 100, 100); // 100x100 px QR kod
            PDPageContentStream qrContentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true);
            qrContentStream.drawImage(qrImage, 70, 70, 100, 100); // Sol alt köşeye yerleştir
            qrContentStream.close();

            contentStream.close();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            document.close();

            // PDF dosyasını indirme başlığıyla yanıt olarak gönder
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=output.pdf");
            headers.add("Content-Type", "application/pdf");

            System.out.printf("Kisi Karti : %s için %s tarihinde başarıyla oluşturulmuştur.",ad_soyad,new Date());
            return new ResponseEntity<>(baos.toByteArray(), headers, HttpStatus.OK);


        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private static PDImageXObject createQrCodeImage(PDDocument document, String data, int width, int height) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, width, height);
        BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();

        return PDImageXObject.createFromByteArray(document, byteArray, "QRCode");
    }



    @Override
    public ResponseEntity<byte[]> generateBasariBelgesi(String ad_soyad, String tarih) {
        return null;
    }
}
