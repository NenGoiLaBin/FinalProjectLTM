package com.finalproject.service;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

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
      System.out.println("[PDFToDOCConverter] Reading PDF file: " + pdfFilePath);
      PDDocument document = PDDocument.load(pdfFile);
      int totalPages = document.getNumberOfPages();
      System.out.println("[PDFToDOCConverter] Total PDF pages: " + totalPages);

      XWPFDocument docxDocument = new XWPFDocument();

      boolean isSlideFormat = totalPages > 1 && totalPages <= 50;

      for (int pageNum = 1; pageNum <= totalPages; pageNum++) {
        System.out.println("[PDFToDOCConverter] Đang xử lý trang " + pageNum + "/" + totalPages);

        if (pageNum > 1 && isSlideFormat) {
          XWPFParagraph pageBreakPara = docxDocument.createParagraph();
          XWPFRun pageBreakRun = pageBreakPara.createRun();
          pageBreakRun.addBreak(org.apache.poi.xwpf.usermodel.BreakType.PAGE);
        }

        PDPage page = document.getPage(pageNum - 1);

        FormattedTextStripper formattedStripper = null;
        List<FormattedTextStripper.FormattedText> formattedTexts = null;

        try {
          formattedStripper = new FormattedTextStripper();
          formattedStripper.setStartPage(pageNum);
          formattedStripper.setEndPage(pageNum);
          formattedStripper.clear();

          formattedStripper.getText(document);
          formattedTexts = formattedStripper.getFormattedTexts();

          System.out.println("[PDFToDOCConverter] Page " + pageNum + ": Extracted " +
              (formattedTexts != null ? formattedTexts.size() : 0) + " formatted characters");
        } catch (Exception e) {
          System.out.println("[PDFToDOCConverter] Cannot extract format, using plain text: " + e.getMessage());
          formattedTexts = null;
        }

        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setStartPage(pageNum);
        stripper.setEndPage(pageNum);
        String pageText = null;
        try {
          pageText = stripper.getText(document);
        } catch (Exception e) {
          System.out
              .println("[PDFToDOCConverter] Không thể trích xuất text từ trang " + pageNum + ": " + e.getMessage());
          pageText = "";
        }

        List<PDImageXObject> images = extractImagesFromPage(page);
        System.out.println("[PDFToDOCConverter] Trang " + pageNum + ": Tìm thấy " + images.size() + " hình ảnh");

        boolean hasText = pageText != null && !pageText.trim().isEmpty();
        boolean isScannedPDF = !hasText && images.isEmpty();

        if (isScannedPDF) {
          System.out.println("[PDFToDOCConverter] Phát hiện PDF scan - render toàn bộ trang thành hình ảnh");
          try {
            BufferedImage pageImage = renderPageAsImage(document, pageNum - 1);
            if (pageImage != null) {
              insertBufferedImageToDocument(docxDocument, pageImage, "Trang " + pageNum);
            }
          } catch (Exception e) {
            System.err
                .println("[PDFToDOCConverter] Lỗi khi render trang " + pageNum + " thành hình ảnh: " + e.getMessage());
            XWPFParagraph paragraph = docxDocument.createParagraph();
            XWPFRun run = paragraph.createRun();
            run.setText("(Không thể xử lý trang " + pageNum + " - có thể là PDF scan)");
            run.setItalic(true);
          }
        } else {
          if (isSlideFormat && hasText) {
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

          if (!images.isEmpty()) {
            insertImagesToDocument(docxDocument, images);
          }

          if (hasText) {
            if (formattedTexts != null && !formattedTexts.isEmpty()) {
              System.out.println("[PDFToDOCConverter] Using format from PDF");
              processFormattedTextWithLines(docxDocument, formattedTexts, pageText);
            } else {
              System.out.println("[PDFToDOCConverter] Using plain text (no format)");
              processPlainText(docxDocument, pageText);
            }
          } else if (!images.isEmpty()) {
            XWPFParagraph paragraph = docxDocument.createParagraph();
            XWPFRun run = paragraph.createRun();
            run.setText("(Trang " + pageNum + " chỉ có hình ảnh, không có text)");
            run.setItalic(true);
          }
          }
        }
      }

      document.close();

      if (docxDocument.getParagraphs().isEmpty()) {
        System.err.println("[PDFToDOCConverter] Warning: PDF has no text or cannot extract text!");
        XWPFParagraph paragraph = docxDocument.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText(
            "This PDF does not contain extractable text. This PDF may only contain images or scanned text.");
      }

      System.out.println("[PDFToDOCConverter] Number of paragraphs created: " + docxDocument.getParagraphs().size());
      System.out.println(
          "[PDFToDOCConverter] Format: " + (isSlideFormat ? "Slide (separate pages)" : "Document (continuous)"));

      FileOutputStream out = new FileOutputStream(docFilePath);
      docxDocument.write(out);
      out.flush();
      out.close();
      docxDocument.close();

      System.out.println("[PDFToDOCConverter] Successfully created DOCX file: " + docFilePath);

      return docFilePath;

    } catch (Exception e) {
      System.err.println("[PDFToDOCConverter] Error during conversion: " + e.getMessage());
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
    boolean inTable = false;
    java.util.List<String> tableRows = new ArrayList<>();

    for (int lineIdx = 0; lineIdx < lines.length; lineIdx++) {
      String originalLine = lines[lineIdx];
      String line = originalLine.trim();

      if (line.isEmpty()) {
        if (inTable && !tableRows.isEmpty()) {
          createTableFromRows(docxDocument, tableRows);
          tableRows.clear();
          inTable = false;
        }

        if (lineIdx > 0 && lineIdx < lines.length - 1) {
          boolean hasContentBefore = false;
          boolean hasContentAfter = false;
          for (int j = lineIdx - 1; j >= 0 && j >= lineIdx - 3; j--) {
            if (!lines[j].trim().isEmpty()) {
              hasContentBefore = true;
              break;
            }
          }
          for (int j = lineIdx + 1; j < lines.length && j <= lineIdx + 3; j++) {
            if (!lines[j].trim().isEmpty()) {
              hasContentAfter = true;
              break;
            }
          }
          if (hasContentBefore && hasContentAfter) {
            XWPFParagraph emptyPara = docxDocument.createParagraph();
            emptyPara.setSpacingAfter(120);
            XWPFRun emptyRun = emptyPara.createRun();
            emptyRun.setText("");
          }
        }
        globalCharIndex += originalLine.length() + 1;
        continue;
      }

      if (isTableRow(line)) {
        if (!inTable) {
          inTable = true;
          tableRows.clear();
        }
        tableRows.add(line);
        globalCharIndex += originalLine.length() + 1;
        continue;
      } else if (inTable && !tableRows.isEmpty()) {
        createTableFromRows(docxDocument, tableRows);
        tableRows.clear();
        inTable = false;
      }

      XWPFParagraph paragraph = docxDocument.createParagraph();

      HeadingInfo headingInfo = detectHeading(line);
      if (headingInfo != null && headingInfo.alignment != null) {
        paragraph.setAlignment(headingInfo.alignment);
      }

      XWPFRun currentRun = null;
      boolean currentBold = false;
      boolean currentItalic = false;
      boolean currentUnderline = false;
      StringBuilder currentTextBuffer = new StringBuilder();

      int fontSize = headingInfo != null ? headingInfo.fontSize : 11;

      paragraph.setSpacingBefore(0);
      paragraph.setSpacingAfter(0);

      for (int i = 0; i < line.length(); i++) {
        int charPos = globalCharIndex + i;
        FormattedTextStripper.FormattedText format = formatMap.get(charPos);

        boolean isBold = format != null ? format.isBold() : false;
        boolean isItalic = format != null ? format.isItalic() : false;
        boolean isUnderline = format != null ? format.isUnderline() : false;

        if (headingInfo != null) {
          isBold = true;
        }

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
          currentRun.setFontSize(fontSize);
          if (currentUnderline) {
            currentRun.setUnderline(org.apache.poi.xwpf.usermodel.UnderlinePatterns.SINGLE);
          }
        }

        currentTextBuffer.append(c);
      }

      if (currentRun != null && currentTextBuffer.length() > 0) {
        currentRun.setText(currentTextBuffer.toString());
      }

      if (headingInfo != null && headingInfo.addSpacingAfter) {
        paragraph.setSpacingAfter(240);
        paragraph.setSpacingBefore(120);
      } else {
        paragraph.setSpacingAfter(60);
      }

      globalCharIndex += originalLine.length() + 1;
    }

    if (inTable && !tableRows.isEmpty()) {
      createTableFromRows(docxDocument, tableRows);
    }
  }

  private void processPlainText(XWPFDocument docxDocument, String pageText) {
    String[] lines = pageText.split("\n", -1);
    boolean inTable = false;
    java.util.List<String> tableRows = new ArrayList<>();

    for (int i = 0; i < lines.length; i++) {
      String line = lines[i].trim();

      if (line.isEmpty()) {
        if (inTable && !tableRows.isEmpty()) {
          createTableFromRows(docxDocument, tableRows);
          tableRows.clear();
          inTable = false;
        }

        if (i > 0 && i < lines.length - 1) {
          boolean hasContentBefore = false;
          boolean hasContentAfter = false;
          for (int j = i - 1; j >= 0 && j >= i - 3; j--) {
            if (!lines[j].trim().isEmpty()) {
              hasContentBefore = true;
              break;
            }
          }
          for (int j = i + 1; j < lines.length && j <= i + 3; j++) {
            if (!lines[j].trim().isEmpty()) {
              hasContentAfter = true;
              break;
            }
          }
          if (hasContentBefore && hasContentAfter) {
            XWPFParagraph emptyPara = docxDocument.createParagraph();
            emptyPara.setSpacingAfter(120);
            XWPFRun emptyRun = emptyPara.createRun();
            emptyRun.setText("");
          }
        }
        continue;
      }

      if (isTableRow(line)) {
        if (!inTable) {
          inTable = true;
          tableRows.clear();
        }
        tableRows.add(line);
        continue;
      } else if (inTable && !tableRows.isEmpty()) {
        createTableFromRows(docxDocument, tableRows);
        tableRows.clear();
        inTable = false;
      }

      XWPFParagraph paragraph = docxDocument.createParagraph();

      paragraph.setSpacingBefore(0);
      paragraph.setSpacingAfter(0);

      HeadingInfo headingInfo = detectHeading(line);
      if (headingInfo != null && headingInfo.alignment != null) {
        paragraph.setAlignment(headingInfo.alignment);
      }

      XWPFRun run = paragraph.createRun();
      String cleanText = line;

      if (headingInfo == null) {
        cleanText = cleanText.replaceAll("\\s+", " ");
      }

      int fontSize = headingInfo != null ? headingInfo.fontSize : 11;
      run.setFontSize(fontSize);
      if (headingInfo != null) {
        run.setBold(true);
      }
      run.setText(cleanText);

      if (headingInfo != null && headingInfo.addSpacingAfter) {
        paragraph.setSpacingAfter(240);
        paragraph.setSpacingBefore(120);
      } else {
        paragraph.setSpacingAfter(60);
      }
    }

    if (inTable && !tableRows.isEmpty()) {
      createTableFromRows(docxDocument, tableRows);
    }
  }

  private List<PDImageXObject> extractImagesFromPage(PDPage page) {
    List<PDImageXObject> images = new ArrayList<>();
    try {
      PDResources resources = page.getResources();
      if (resources == null) {
        return images;
      }

      Iterable<COSName> xObjectNames = resources.getXObjectNames();
      if (xObjectNames == null) {
        return images;
      }

      for (COSName xObjectName : xObjectNames) {
        PDXObject xObject = resources.getXObject(xObjectName);
        if (xObject instanceof PDImageXObject) {
          PDImageXObject image = (PDImageXObject) xObject;
          images.add(image);
        }
      }
    } catch (IOException e) {
      System.err.println("[PDFToDOCConverter] Lỗi khi trích xuất hình ảnh: " + e.getMessage());
    }
    return images;
  }

  private BufferedImage renderPageAsImage(PDDocument document, int pageIndex) {
    try {
      PDFRenderer renderer = new PDFRenderer(document);
      BufferedImage image = renderer.renderImageWithDPI(pageIndex, 150);
      System.out.println("[PDFToDOCConverter] Đã render trang " + (pageIndex + 1) + " thành hình ảnh: " +
          image.getWidth() + "x" + image.getHeight());
      return image;
    } catch (Exception e) {
      System.err.println("[PDFToDOCConverter] Lỗi khi render trang thành hình ảnh: " + e.getMessage());
      return null;
    }
  }

  private void insertBufferedImageToDocument(XWPFDocument docxDocument, BufferedImage image, String caption) {
    try {
      XWPFParagraph imagePara = docxDocument.createParagraph();
      imagePara.setAlignment(org.apache.poi.xwpf.usermodel.ParagraphAlignment.CENTER);
      XWPFRun imageRun = imagePara.createRun();

      int width = image.getWidth();
      int height = image.getHeight();

      int maxWidth = 600;
      int maxHeight = 800;

      if (width > maxWidth || height > maxHeight) {
        double scale = Math.min((double) maxWidth / width, (double) maxHeight / height);
        width = (int) (width * scale);
        height = (int) (height * scale);
      }

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ImageIO.write(image, "png", baos);
      byte[] imageBytes = baos.toByteArray();

      imageRun.addPicture(
          new ByteArrayInputStream(imageBytes),
          org.apache.poi.xwpf.usermodel.XWPFDocument.PICTURE_TYPE_PNG,
          caption,
          Units.toEMU(width),
          Units.toEMU(height));

      System.out.println("[PDFToDOCConverter] Đã chèn hình ảnh render: " + width + "x" + height);

    } catch (Exception e) {
      System.err.println("[PDFToDOCConverter] Lỗi khi chèn hình ảnh render vào DOCX: " + e.getMessage());
    }
  }

  private void insertImagesToDocument(XWPFDocument docxDocument, List<PDImageXObject> images) {
    for (PDImageXObject image : images) {
      try {
        XWPFParagraph imagePara = docxDocument.createParagraph();
        imagePara.setAlignment(org.apache.poi.xwpf.usermodel.ParagraphAlignment.CENTER);
        XWPFRun imageRun = imagePara.createRun();

        int width = image.getWidth();
        int height = image.getHeight();

        int maxWidth = 500;
        int maxHeight = 400;

        if (width > maxWidth || height > maxHeight) {
          double scale = Math.min((double) maxWidth / width, (double) maxHeight / height);
          width = (int) (width * scale);
          height = (int) (height * scale);
        }

        BufferedImage bufferedImage = image.getImage();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        String imageType = image.getSuffix();
        if (imageType == null || imageType.isEmpty()) {
          imageType = "png";
        }

        String formatName = "png";
        int pictureType = org.apache.poi.xwpf.usermodel.XWPFDocument.PICTURE_TYPE_PNG;

        if ("jpg".equalsIgnoreCase(imageType) || "jpeg".equalsIgnoreCase(imageType)) {
          formatName = "jpg";
          pictureType = org.apache.poi.xwpf.usermodel.XWPFDocument.PICTURE_TYPE_JPEG;
        } else if ("png".equalsIgnoreCase(imageType)) {
          formatName = "png";
          pictureType = org.apache.poi.xwpf.usermodel.XWPFDocument.PICTURE_TYPE_PNG;
        }

        ImageIO.write(bufferedImage, formatName, baos);
        byte[] imageBytes = baos.toByteArray();

        imageRun.addPicture(
            new ByteArrayInputStream(imageBytes),
            pictureType,
            "image",
            Units.toEMU(width),
            Units.toEMU(height));

        System.out.println("[PDFToDOCConverter] Đã chèn hình ảnh: " + width + "x" + height + " (" + imageType + ")");

      } catch (Exception e) {
        System.err.println("[PDFToDOCConverter] Lỗi khi chèn hình ảnh vào DOCX: " + e.getMessage());
      }
    }
  }

  private boolean isTableRow(String line) {
    if (line == null || line.trim().isEmpty()) {
      return false;
    }

    int pipeCount = 0;
    int tabCount = 0;
    for (char c : line.toCharArray()) {
      if (c == '|') {
        pipeCount++;
      } else if (c == '\t') {
        tabCount++;
      }
    }

    return (pipeCount >= 2) || (tabCount >= 2);
  }

  private void createTableFromRows(XWPFDocument docxDocument, List<String> rows) {
    if (rows.isEmpty()) {
      return;
    }

    try {
      List<List<String>> tableData = new ArrayList<>();
      int maxCols = 0;

      for (String row : rows) {
        List<String> cells = new ArrayList<>();

        if (row.contains("|")) {
          String[] parts = row.split("\\|", -1);
          for (String part : parts) {
            cells.add(part.trim());
          }
        } else if (row.contains("\t")) {
          String[] parts = row.split("\t", -1);
          for (String part : parts) {
            cells.add(part.trim());
          }
        } else {
          cells.add(row.trim());
        }

        tableData.add(cells);
        maxCols = Math.max(maxCols, cells.size());
      }

      if (maxCols == 0) {
        return;
      }

      org.apache.poi.xwpf.usermodel.XWPFTable table = docxDocument.createTable(rows.size(), maxCols);
      table.setWidth("100%");

      for (int i = 0; i < tableData.size() && i < rows.size(); i++) {
        List<String> cells = tableData.get(i);
        org.apache.poi.xwpf.usermodel.XWPFTableRow tableRow = table.getRow(i);

        for (int j = 0; j < maxCols; j++) {
          org.apache.poi.xwpf.usermodel.XWPFTableCell cell = tableRow.getCell(j);
          if (cell == null) {
            cell = tableRow.createCell();
          }

          String cellText = (j < cells.size()) ? cells.get(j) : "";
          cell.setText(cellText);

          if (i == 0) {
            org.apache.poi.xwpf.usermodel.XWPFParagraph cellPara = cell.getParagraphs().get(0);
            if (!cellPara.getRuns().isEmpty()) {
              cellPara.getRuns().get(0).setBold(true);
            }
          }
        }
      }

      System.out.println("[PDFToDOCConverter] Đã tạo bảng với " + rows.size() + " hàng và " + maxCols + " cột");
    } catch (Exception e) {
      System.err.println("[PDFToDOCConverter] Lỗi khi tạo bảng: " + e.getMessage());
    }
  }

  private HeadingInfo detectHeading(String line) {
    if (line == null || line.trim().isEmpty()) {
      return null;
    }

    String trimmed = line.trim();

    if (trimmed.matches("^CHƯƠNG\\s+\\d+.*")) {
      return new HeadingInfo(18,
          org.apache.poi.xwpf.usermodel.ParagraphAlignment.CENTER, true);
    }

    if (trimmed.matches("^\\d+\\.\\s+.*")) {
      String[] parts = trimmed.split("\\.", 2);
      if (parts.length > 0) {
        String numPart = parts[0].trim();
        int level = numPart.split("\\.").length;
        if (level == 1) {
          return new HeadingInfo(16, null, true);
        } else if (level == 2) {
          return new HeadingInfo(14, null, true);
        } else if (level >= 3) {
          return new HeadingInfo(12, null, true);
        }
      }
    }

    if (trimmed.matches("^[A-Z][A-Z\\s]+$") && trimmed.length() < 50 &&
        !trimmed.contains(":") && !trimmed.contains(".")) {
      return new HeadingInfo(14,
          org.apache.poi.xwpf.usermodel.ParagraphAlignment.CENTER, true);
    }

    if (trimmed.startsWith("#") || trimmed.startsWith("*") ||
        trimmed.matches("^[-•]\\s+.*")) {
      return null;
    }

    return null;
  }

  private static class HeadingInfo {
    int fontSize;
    org.apache.poi.xwpf.usermodel.ParagraphAlignment alignment;
    boolean addSpacingAfter;

    HeadingInfo(int fontSize,
        org.apache.poi.xwpf.usermodel.ParagraphAlignment alignment,
        boolean addSpacingAfter) {
      this.fontSize = fontSize;
      this.alignment = alignment;
      this.addSpacingAfter = addSpacingAfter;
    }
  }
}
