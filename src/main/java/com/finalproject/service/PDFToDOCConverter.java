package com.finalproject.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class PDFToDOCConverter {

  public String convert(String pdfFilePath) {
    File pdfFile = new File(pdfFilePath);
    if (!pdfFile.exists()) {
      return null;
    }

    String docFilePath = pdfFilePath.replace(".pdf", ".docx");
    if (docFilePath.equals(pdfFilePath)) {
      docFilePath = pdfFilePath + ".docx";
    }

    try {
      System.out.println("[PDFToDOCConverter] Đang đọc file PDF: " + pdfFilePath);
      PDDocument document = PDDocument.load(pdfFile);
      int totalPages = document.getNumberOfPages();
      System.out.println("[PDFToDOCConverter] Số trang PDF: " + totalPages);

      XWPFDocument docxDocument = new XWPFDocument();

      boolean isSlideFormat = totalPages > 1 && totalPages <= 50;

      for (int pageNum = 1; pageNum <= totalPages; pageNum++) {
        FormattedTextStripper formattedStripper = null;
        List<FormattedTextStripper.FormattedText> formattedTexts = null;

        try {
          formattedStripper = new FormattedTextStripper();
          formattedStripper.setStartPage(pageNum);
          formattedStripper.setEndPage(pageNum);
          formattedStripper.clear();

          formattedStripper.getText(document);
          formattedTexts = formattedStripper.getFormattedTexts();

          System.out.println("[PDFToDOCConverter] Trang " + pageNum + ": Trích xuất được " +
              (formattedTexts != null ? formattedTexts.size() : 0) + " ký tự có format");
        } catch (Exception e) {
          System.out.println("[PDFToDOCConverter] Không thể trích xuất format, sử dụng text thường: " + e.getMessage());
          formattedTexts = null;
        }

        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setStartPage(pageNum);
        stripper.setEndPage(pageNum);
        String pageText = stripper.getText(document);

        if (pageText != null && !pageText.trim().isEmpty()) {
          if (pageNum > 1 && isSlideFormat) {
            XWPFParagraph pageBreakPara = docxDocument.createParagraph();
            XWPFRun pageBreakRun = pageBreakPara.createRun();
            pageBreakRun.addBreak(org.apache.poi.xwpf.usermodel.BreakType.PAGE);

            String firstLine = pageText.split("\n")[0].trim();
            if (firstLine.length() > 0 && firstLine.length() < 100) {
              XWPFParagraph titlePara = docxDocument.createParagraph();
              XWPFRun titleRun = titlePara.createRun();
              titleRun.setBold(true);
              titleRun.setFontSize(16);
              titleRun.setText("Slide " + pageNum + ": " + firstLine);
              titleRun.addBreak();
            }
          }

          if (formattedTexts != null && !formattedTexts.isEmpty()) {
            System.out.println("[PDFToDOCConverter] Sử dụng format từ PDF");
            processFormattedTextWithLines(docxDocument, formattedTexts, pageText);
          } else {
            System.out.println("[PDFToDOCConverter] Sử dụng text thường (không có format)");
            processPlainText(docxDocument, pageText);
          }
        }
      }

      document.close();

      if (docxDocument.getParagraphs().isEmpty()) {
        System.err.println("[PDFToDOCConverter] Cảnh báo: PDF không có text hoặc không thể trích xuất text!");
        XWPFParagraph paragraph = docxDocument.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText(
            "PDF này không chứa text có thể trích xuất được. Có thể PDF này chỉ chứa hình ảnh hoặc text đã được scan.");
      }

      System.out.println("[PDFToDOCConverter] Số paragraph đã tạo: " + docxDocument.getParagraphs().size());
      System.out.println(
          "[PDFToDOCConverter] Format: " + (isSlideFormat ? "Slide (từng trang riêng biệt)" : "Document (liên tục)"));

      FileOutputStream out = new FileOutputStream(docFilePath);
      docxDocument.write(out);
      out.flush();
      out.close();
      docxDocument.close();

      System.out.println("[PDFToDOCConverter] Đã tạo file DOCX thành công: " + docFilePath);

      return docFilePath;

    } catch (Exception e) {
      System.err.println("[PDFToDOCConverter] Lỗi khi chuyển đổi: " + e.getMessage());
      e.printStackTrace();
      return null;
    }
  }

  private void processFormattedTextWithLines(XWPFDocument docxDocument,
      List<FormattedTextStripper.FormattedText> formattedTexts, String plainText) {
    String[] lines = plainText.split("\n", -1);

    int charIndex = 0;
    java.util.Map<Integer, FormattedTextStripper.FormattedText> formatMap = new java.util.HashMap<>();

    for (FormattedTextStripper.FormattedText formattedText : formattedTexts) {
      String text = formattedText.getText();
      if (text != null && !text.isEmpty()) {
        for (char c : text.toCharArray()) {
          if (c != '\n' && c != '\r') {
            formatMap.put(charIndex, formattedText);
            charIndex++;
          }
        }
      }
    }

    int globalCharIndex = 0;

    for (int lineIdx = 0; lineIdx < lines.length; lineIdx++) {
      String line = lines[lineIdx].trim();

      if (line.isEmpty()) {
        if (lineIdx > 0 && lineIdx < lines.length - 1) {
          boolean hasContentBefore = false;
          boolean hasContentAfter = false;
          for (int j = lineIdx - 1; j >= 0 && j >= lineIdx - 2; j--) {
            if (!lines[j].trim().isEmpty()) {
              hasContentBefore = true;
              break;
            }
          }
          for (int j = lineIdx + 1; j < lines.length && j <= lineIdx + 2; j++) {
            if (!lines[j].trim().isEmpty()) {
              hasContentAfter = true;
              break;
            }
          }
          if (hasContentBefore && hasContentAfter) {
            XWPFParagraph emptyPara = docxDocument.createParagraph();
            XWPFRun emptyRun = emptyPara.createRun();
            emptyRun.setText("");
          }
        }
        globalCharIndex += line.length() + 1;
        continue;
      }

      XWPFParagraph paragraph = docxDocument.createParagraph();
      XWPFRun currentRun = null;
      boolean currentBold = false;
      boolean currentItalic = false;
      boolean currentUnderline = false;
      StringBuilder currentTextBuffer = new StringBuilder();

      for (int i = 0; i < line.length(); i++) {
        int charPos = globalCharIndex + i;
        FormattedTextStripper.FormattedText format = formatMap.get(charPos);

        boolean isBold = format != null ? format.isBold() : false;
        boolean isItalic = format != null ? format.isItalic() : false;
        boolean isUnderline = format != null ? format.isUnderline() : false;

        char c = line.charAt(i);

        if (currentRun == null ||
            isBold != currentBold ||
            isItalic != currentItalic ||
            isUnderline != currentUnderline) {

          if (currentRun != null && currentTextBuffer.length() > 0) {
            currentRun.setText(currentTextBuffer.toString());
            currentTextBuffer.setLength(0);
          }

          currentRun = paragraph.createRun();
          currentBold = isBold;
          currentItalic = isItalic;
          currentUnderline = isUnderline;

          currentRun.setBold(currentBold);
          currentRun.setItalic(currentItalic);
          if (currentUnderline) {
            currentRun.setUnderline(org.apache.poi.xwpf.usermodel.UnderlinePatterns.SINGLE);
          }
        }

        currentTextBuffer.append(c);
      }

      if (currentRun != null && currentTextBuffer.length() > 0) {
        currentRun.setText(currentTextBuffer.toString());
      }

      globalCharIndex += line.length() + 1;
    }
  }

  private void processFormattedText(XWPFDocument docxDocument,
      List<FormattedTextStripper.FormattedText> formattedTexts) {
    if (formattedTexts == null || formattedTexts.isEmpty()) {
      return;
    }

    XWPFParagraph currentParagraph = docxDocument.createParagraph();
    XWPFRun currentRun = null;
    boolean currentBold = false;
    boolean currentItalic = false;
    boolean currentUnderline = false;
    StringBuilder currentTextBuffer = new StringBuilder();

    for (int i = 0; i < formattedTexts.size(); i++) {
      FormattedTextStripper.FormattedText formattedText = formattedTexts.get(i);
      String text = formattedText.getText();

      if (text == null) {
        continue;
      }

      boolean hasNewline = text.contains("\n") || text.contains("\r\n") || text.contains("\r");

      if (hasNewline) {
        if (currentRun != null && currentTextBuffer.length() > 0) {
          currentRun.setText(currentTextBuffer.toString());
          currentTextBuffer.setLength(0);
        }

        String[] parts = text.split("[\r\n]+", -1);
        for (int j = 0; j < parts.length; j++) {
          String part = parts[j];

          if (!part.isEmpty()) {
            if (currentRun == null ||
                formattedText.isBold() != currentBold ||
                formattedText.isItalic() != currentItalic ||
                formattedText.isUnderline() != currentUnderline) {

              if (currentRun != null && currentTextBuffer.length() > 0) {
                currentRun.setText(currentTextBuffer.toString());
                currentTextBuffer.setLength(0);
              }

              currentRun = currentParagraph.createRun();
              currentBold = formattedText.isBold();
              currentItalic = formattedText.isItalic();
              currentUnderline = formattedText.isUnderline();

              currentRun.setBold(currentBold);
              currentRun.setItalic(currentItalic);
              if (currentUnderline) {
                currentRun.setUnderline(org.apache.poi.xwpf.usermodel.UnderlinePatterns.SINGLE);
              }
            }

            currentTextBuffer.append(part);
          }

          if (j < parts.length - 1 || (i < formattedTexts.size() - 1)) {
            if (currentRun != null && currentTextBuffer.length() > 0) {
              currentRun.setText(currentTextBuffer.toString());
              currentTextBuffer.setLength(0);
            }

            currentParagraph = docxDocument.createParagraph();
            currentRun = null;
          }
        }
        continue;
      }

      if (currentRun == null ||
          formattedText.isBold() != currentBold ||
          formattedText.isItalic() != currentItalic ||
          formattedText.isUnderline() != currentUnderline) {

        if (currentRun != null && currentTextBuffer.length() > 0) {
          currentRun.setText(currentTextBuffer.toString());
          currentTextBuffer.setLength(0);
        }

        currentRun = currentParagraph.createRun();
        currentBold = formattedText.isBold();
        currentItalic = formattedText.isItalic();
        currentUnderline = formattedText.isUnderline();

        currentRun.setBold(currentBold);
        currentRun.setItalic(currentItalic);
        if (currentUnderline) {
          currentRun.setUnderline(org.apache.poi.xwpf.usermodel.UnderlinePatterns.SINGLE);
        }
      }

      if (currentRun != null && !text.isEmpty()) {
        currentTextBuffer.append(text);
      }
    }

    if (currentRun != null && currentTextBuffer.length() > 0) {
      currentRun.setText(currentTextBuffer.toString());
    }
  }

  private void processPlainText(XWPFDocument docxDocument, String pageText) {
    String[] lines = pageText.split("\n");

    for (int i = 0; i < lines.length; i++) {
      String line = lines[i].trim();

      if (line.isEmpty()) {
        if (i > 0 && i < lines.length - 1) {
          boolean hasContentBefore = false;
          boolean hasContentAfter = false;
          for (int j = i - 1; j >= 0 && j >= i - 2; j--) {
            if (!lines[j].trim().isEmpty()) {
              hasContentBefore = true;
              break;
            }
          }
          for (int j = i + 1; j < lines.length && j <= i + 2; j++) {
            if (!lines[j].trim().isEmpty()) {
              hasContentAfter = true;
              break;
            }
          }
          if (hasContentBefore && hasContentAfter) {
            XWPFParagraph emptyPara = docxDocument.createParagraph();
            XWPFRun emptyRun = emptyPara.createRun();
            emptyRun.setText("");
          }
        }
        continue;
      }

      XWPFParagraph paragraph = docxDocument.createParagraph();
      XWPFRun run = paragraph.createRun();

      String cleanText = line.replaceAll("\\s+", " ");
      run.setText(cleanText);
    }
  }
}
