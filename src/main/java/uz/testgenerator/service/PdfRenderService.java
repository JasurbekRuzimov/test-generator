package uz.testgenerator.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import uz.testgenerator.domain.OptionEntity;
import uz.testgenerator.domain.QuestionEntity;
import uz.testgenerator.domain.TestEntity;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class PdfRenderService {

    public RenderResult renderVariantPdf(TestEntity test, int variantNo, List<QuestionEntity> questions, boolean shuffleOptions, Random random) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD);
            Font hFont = new Font(Font.HELVETICA, 11, Font.BOLD);
            Font normal = new Font(Font.HELVETICA, 11);

            Paragraph title = new Paragraph(test.getTitle(), titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph("Variant: " + variantNo, hFont));
            document.add(new Paragraph("Ism: ___________________________    Guruh: ____________      Sana: ____/____/______", normal));
            document.add(Chunk.NEWLINE);

            StringBuilder answerKey = new StringBuilder();

            for (int i = 0; i < questions.size(); i++) {
                QuestionEntity q = questions.get(i);

                document.add(new Paragraph((i + 1) + ". " + q.getText(), normal));
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
                char correctLetter = (char) ('A' + newCorrectIndex);

                for (int pos = 0; pos < opts.size(); pos++) {
                    char letter = (char) ('A' + pos);
                    document.add(new Paragraph("    " + letter + ") " + opts.get(pos).getText(), normal));
                }
                document.add(Chunk.NEWLINE);

                if (i > 0) answerKey.append(", ");
                answerKey.append(i + 1).append("-").append(correctLetter);
            }

            document.newPage();
            document.add(new Paragraph("Answer Key (To'g'ri javoblar)", hFont));
            document.add(Chunk.NEWLINE);
            document.add(new Paragraph(answerKey.toString(), normal));

            document.close();
            return new RenderResult(baos.toByteArray(), answerKey.toString());
        } catch (Exception e) {
            throw new RuntimeException("PDF yaratishda xatolik: " + e.getMessage(), e);
        }
    }

    public record RenderResult(byte[] pdfBytes, String answerKeyText) {}
}
