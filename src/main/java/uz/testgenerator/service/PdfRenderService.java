package uz.testgenerator.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import uz.testgenerator.domain.OptionEntity;
import uz.testgenerator.domain.QuestionEntity;
import uz.testgenerator.domain.TestEntity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class PdfRenderService {

    public RenderResult renderVariantPdf(TestEntity test,
        int variantNo,
        List<QuestionEntity> questions,
        boolean shuffleOptions,
        Random random) {

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            document.open();

            Fonts fonts = loadFonts();

            StringBuilder answerKey = new StringBuilder();

            PdfContentByte cb = writer.getDirectContent();
            ColumnText flow = new ColumnText(cb);

            float leading = 14f;

            for (int i = 0; i < questions.size(); i++) {
                QuestionEntity q = questions.get(i);

                Paragraph qP = new Paragraph((i + 1) + ". " + safe(q.getText()), fonts.normal);
                qP.setLeading(leading);
                qP.setSpacingAfter(2f);
                flow.addElement(qP);

                List<OptionEntity> opts = new ArrayList<>(q.getOptions());
                if (shuffleOptions) {
                    for (int j = opts.size() - 1; j > 0; j--) {
                        int k = random.nextInt(j + 1);
                        OptionEntity tmp = opts.get(j);
                        opts.set(j, opts.get(k));
                        opts.set(k, tmp);
                    }
                }

                int newCorrectIndex = -1;
                for (int pos = 0; pos < opts.size(); pos++) {
                    if (opts.get(pos).getOriginalIndex() == q.getCorrectIndex()) {
                        newCorrectIndex = pos;
                        break;
                    }
                }
                if (newCorrectIndex < 0) newCorrectIndex = 0;
                char correctLetter = (char) ('A' + newCorrectIndex);

                for (int pos = 0; pos < opts.size(); pos++) {
                    char letter = (char) ('A' + pos);
                    Paragraph optP = new Paragraph(letter + ") " + safe(opts.get(pos).getText()), fonts.normal);
                    optP.setLeading(leading);
                    optP.setIndentationLeft(18f);
                    optP.setSpacingAfter(1f);
                    flow.addElement(optP);
                }

                flow.addElement(new Paragraph(" ", fonts.normal));

                if (i > 0) answerKey.append(", ");
                answerKey.append(i + 1).append("-").append(correctLetter);
            }

            boolean firstPage = true;
            float gutter = 18f;

            while (true) {

                float top;
                if (firstPage) {
                     float contentTopY = drawHeader(writer, document, fonts, test.getTitle(), variantNo);
                    top = contentTopY - 6f;
                    firstPage = false;
                } else {
                        top = document.top();
                }

                float bottom = document.bottom();

                float leftX = document.left();
                float rightX = document.right();
                float fullWidth = rightX - leftX;
                float colWidth = (fullWidth - gutter) / 2f;

                flow.setSimpleColumn(
                    leftX,
                    bottom,
                    leftX + colWidth,
                    top
                );
                int status = flow.go();

                if (ColumnText.hasMoreText(status)) {
                    flow.setSimpleColumn(
                        leftX + colWidth + gutter,
                        bottom,
                        rightX,
                        top
                    );
                    status = flow.go();

                    if (ColumnText.hasMoreText(status)) {
                        document.newPage();
                        continue;
                    }
                }

                break;
            }

            document.newPage();
            Paragraph akTitle = new Paragraph("Answer Key (To'g'ri javoblar)", fonts.hBold);
            akTitle.setSpacingAfter(10f);
            document.add(akTitle);

            Paragraph ak = new Paragraph(answerKey.toString(), fonts.normal);
            ak.setLeading(14f);
            document.add(ak);

            document.close();
            return new RenderResult(baos.toByteArray(), answerKey.toString());

        } catch (Exception e) {
            throw new RuntimeException("PDF yaratishda xatolik: " + e.getMessage(), e);
        }
    }

    private float drawHeader(PdfWriter writer,
        Document document,
        Fonts fonts,
        String title,
        int variantNo) {

        PdfContentByte cb = writer.getDirectContent();

        float left = document.left();
        float right = document.right();
        float centerX = (left + right) / 2f;

        float y = document.top() + 10f;

        ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
            new Phrase(safe(title), fonts.titleBold),
            centerX, y, 0);

        y -= 20f;

        ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
            new Phrase("Variant: " + variantNo, fonts.hBold),
            left, y, 0);

        ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
            new Phrase("Sana: ____/____/______", fonts.normal),
            right, y, 0);

        y -= 16f;

        ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
            new Phrase("Ism: ___________________________    Guruh: ____________", fonts.normal),
            left, y, 0);

        return y - 18f;
    }


    private Fonts loadFonts() {
        try {
            BaseFont calibri = loadTtfFromResources("fonts/calibri.ttf");
            BaseFont calibriBold = null;

            try {
                calibriBold = loadTtfFromResources("fonts/calibri-bold.ttf");
            } catch (Exception ignored) {
            }

            Font normal = new Font(calibri, 11, Font.NORMAL);
            Font hBold = new Font(calibriBold != null ? calibriBold : calibri, 11, Font.BOLD);
            Font titleBold = new Font(calibriBold != null ? calibriBold : calibri, 16, Font.BOLD);

            return new Fonts(normal, hBold, titleBold);

        } catch (Exception e) {
            Font normal = new Font(Font.HELVETICA, 11, Font.NORMAL);
            Font hBold = new Font(Font.HELVETICA, 11, Font.BOLD);
            Font titleBold = new Font(Font.HELVETICA, 16, Font.BOLD);
            return new Fonts(normal, hBold, titleBold);
        }
    }

    private BaseFont loadTtfFromResources(String classpath) throws IOException {
        ClassPathResource res = new ClassPathResource(classpath);
        try (InputStream is = res.getInputStream()) {
            byte[] bytes = is.readAllBytes();
            return BaseFont.createFont("Calibri", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, bytes, null);
        }
    }

    private String safe(String s) {
        return (s == null) ? "" : s.trim();
    }

    private static class Fonts {
        final Font normal;
        final Font hBold;
        final Font titleBold;

        Fonts(Font normal, Font hBold, Font titleBold) {
            this.normal = normal;
            this.hBold = hBold;
            this.titleBold = titleBold;
        }
    }

    public record RenderResult(byte[] pdfBytes, String answerKeyText) {}
}
