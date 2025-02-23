package com.kaan.ApachePdf.Document.model;


import com.kaan.ApachePdf.Document.service.Impl.BasariBelgesiImpl;
import com.kaan.ApachePdf.Document.service.Impl.KisiKartiImpl;

public class CreatePdf {
    public static PdfPattern pdfPattern(String type) {
        if (type.equalsIgnoreCase("BasariBelgesi")) {
            return new BasariBelgesiImpl();
        } else if (type.equalsIgnoreCase("KisiKarti")) {
            return new KisiKartiImpl();
        } else {
            throw new IllegalArgumentException("Geçersiz belge türü" + type);
        }
    }
}
