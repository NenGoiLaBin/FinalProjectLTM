package com.finalproject.service;

import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FormattedTextStripper extends PDFTextStripper {

  private List<FormattedText> formattedTexts;

  public FormattedTextStripper() throws IOException {
    super();
    formattedTexts = new ArrayList<>();
  }

  @Override
  protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
    if (textPositions == null || textPositions.isEmpty()) {
      for (char c : text.toCharArray()) {
        formattedTexts.add(new FormattedText(String.valueOf(c), false, false, false));
      }
      return;
    }

    for (TextPosition textPosition : textPositions) {
      PDFont font = textPosition.getFont();
      String fontName = font != null ? font.getName() : "";

      boolean isBold = fontName.toLowerCase().contains("bold") ||
          fontName.toLowerCase().contains("black") ||
          fontName.toLowerCase().contains("heavy") ||
          fontName.toLowerCase().contains("semibold") ||
          fontName.toLowerCase().endsWith("-b") ||
          fontName.toLowerCase().endsWith("bold");

      boolean isItalic = fontName.toLowerCase().contains("italic") ||
          fontName.toLowerCase().contains("oblique") ||
          fontName.toLowerCase().endsWith("-i") ||
          fontName.toLowerCase().endsWith("italic");

      boolean isUnderline = false;

      String charText = textPosition.getUnicode();
      if (charText != null && !charText.isEmpty()) {
        formattedTexts.add(new FormattedText(
            charText,
            isBold,
            isItalic,
            isUnderline));
      }
    }
  }

  public List<FormattedText> getFormattedTexts() {
    return formattedTexts;
  }

  public void clear() {
    formattedTexts.clear();
  }

  public static class FormattedText {
    private String text;
    private boolean bold;
    private boolean italic;
    private boolean underline;

    public FormattedText(String text, boolean bold, boolean italic, boolean underline) {
      this.text = text;
      this.bold = bold;
      this.italic = italic;
      this.underline = underline;
    }

    public String getText() {
      return text;
    }

    public boolean isBold() {
      return bold;
    }

    public boolean isItalic() {
      return italic;
    }

    public boolean isUnderline() {
      return underline;
    }
  }
}
