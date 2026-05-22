//package com.client.utils;
//
//import com.common.dto.PassengerDto;
//import com.itextpdf.text.*;
//import com.itextpdf.text.pdf.BaseFont;
//import com.itextpdf.text.pdf.PdfPCell;
//import com.itextpdf.text.pdf.PdfPTable;
//import com.itextpdf.text.pdf.PdfWriter;
//
//import java.io.FileOutputStream;
//import java.util.List;
//
//public class PdfExportUtil {
//
//    public static void exportPassengerList(String fileName, String flightNumber, List<PassengerDto> passengers) {
//        Document document = new Document();
//        try {
//            PdfWriter.getInstance(document, new FileOutputStream(fileName));
//            document.open();
//
//            // Поддержка кириллицы
//            BaseFont bf = BaseFont.createFont("c:/Windows/Fonts/arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
//            Font fontTitle = new Font(bf, 18, Font.BOLD);
//            Font fontHeader = new Font(bf, 12, Font.BOLD);
//            Font fontNormal = new Font(bf, 12, Font.NORMAL);
//
//            Paragraph title = new Paragraph("Список пассажиров рейса: " + flightNumber, fontTitle);
//            title.setAlignment(Element.ALIGN_CENTER);
//            title.setSpacingAfter(20);
//            document.add(title);
//
//            PdfPTable table = new PdfPTable(3);
//            table.setWidthPercentage(100);
//            table.setSpacingBefore(10f);
//            table.setSpacingAfter(10f);
//
//            // Заголовки
//            addTableCell(table, "Имя", fontHeader);
//            addTableCell(table, "Фамилия", fontHeader);
//            addTableCell(table, "Паспорт", fontHeader);
//
//            // Данные
//            for (PassengerDto p : passengers) {
//                addTableCell(table, p.getFirstName(), fontNormal);
//                addTableCell(table, p.getLastName(), fontNormal);
//                addTableCell(table, p.getPassportNumber(), fontNormal);
//            }
//
//            document.add(table);
//            document.close();
//            System.out.println("PDF отчет успешно создан: " + fileName);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private static void addTableCell(PdfPTable table, String text, Font font) {
//        PdfPCell cell = new PdfPCell(new Phrase(text, font));
//        cell.setPadding(5);
//        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
//        table.addCell(cell);
//    }
//}
