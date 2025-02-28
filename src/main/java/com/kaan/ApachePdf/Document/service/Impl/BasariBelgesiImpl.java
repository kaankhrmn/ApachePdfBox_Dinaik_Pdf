package com.kaan.ApachePdf.Document.service.Impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.kaan.ApachePdf.Document.model.PdfFactory;
import com.kaan.ApachePdf.Document.model.PdfGenerator;
import com.kaan.ApachePdf.Document.service.BasariBelgesiService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
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
public class BasariBelgesiImpl implements PdfGenerator, BasariBelgesiService {


    String belge_sahibi = "Kaan Kahraman";
    String belge_tarihi = new Date().toString();
    String belgeBilgisi = String.format("Bu belge %s tarihinde %s tarafindan üretilmistir", belge_tarihi, belge_sahibi);


    @Override
    public ResponseEntity<byte[]> generateDocument(String type, String ad_soyad, String tarih) {
        try {
            PdfGenerator pdfGenerator = PdfFactory.pdfPattern(type);
            return pdfGenerator.generateBasariBelgesi(ad_soyad, tarih);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<byte[]> generateBasariBelgesi(String ad_soyad, String tarih) {
        try {
            ClassPathResource pdftemplate = new ClassPathResource("templates/BasariBelgesi.pdf");
            InputStream inputStream = pdftemplate.getInputStream();
            PDDocument document = PDDocument.load(inputStream);
            PDPage page = document.getPage(0);

            PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true);
            File fontFile = new File("src/main/resources/templates/arial.ttf");
            PDType0Font font = PDType0Font.load(document, fontFile);
            contentStream.setFont(font, 40);
            contentStream.setNonStrokingColor(Color.BLACK);

            contentStream.beginText();
            contentStream.newLineAtOffset(370, 250);
            contentStream.showText(ad_soyad);
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(450, 450);
            contentStream.showText(tarih);
            contentStream.endText();

            contentStream.close();

            //QR KOD OLUŞTUR VE PDF'YE EKLE
            PDImageXObject qrImage = createQrCodeImage(document, belgeBilgisi, 100, 100); // 100x100 px QR kod
            PDPageContentStream qrContentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true);
            qrContentStream.drawImage(qrImage, 70, 70, 100, 100); // Sol alt köşeye yerleştir
            qrContentStream.close();


            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            document.close();

            // PDF dosyasını indirme başlığıyla yanıt olarak gönder
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=output.pdf");
            headers.add("Content-Type", "application/pdf");

            System.out.printf("Basari Belgesi : %s için %s tarihinde başarıyla oluşturulmuştur.%n", ad_soyad, new Date());
            return new ResponseEntity<>(baos.toByteArray(), headers, HttpStatus.OK);

        } catch (Exception e) {
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
    public ResponseEntity<byte[]> generateKisiKarti(String ad_soyad, String adres, String dogumYeri, String tckn, String cinsiyet) {
        return null;
    }
}
