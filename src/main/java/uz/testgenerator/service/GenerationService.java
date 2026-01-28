package uz.testgenerator.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import uz.testgenerator.domain.GenerationEntity;
import uz.testgenerator.domain.GeneratedVariantEntity;
import uz.testgenerator.domain.QuestionEntity;
import uz.testgenerator.domain.TestEntity;
import uz.testgenerator.repo.GenerationRepository;
import uz.testgenerator.repo.GeneratedVariantRepository;
import uz.testgenerator.repo.TestRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class GenerationService {

    private final TestRepository testRepository;
    private final GenerationRepository generationRepository;
    private final GeneratedVariantRepository variantRepository;
    private final StorageService storageService;
    private final PdfRenderService pdfRenderService;

    public GenerationService(TestRepository testRepository,
                             GenerationRepository generationRepository,
                             GeneratedVariantRepository variantRepository,
                             StorageService storageService,
                             PdfRenderService pdfRenderService) {
        this.testRepository = testRepository;
        this.generationRepository = generationRepository;
        this.variantRepository = variantRepository;
        this.storageService = storageService;
        this.pdfRenderService = pdfRenderService;
    }

    @Transactional
    public GenerationEntity generate(Long testId,
                                     int questionCount,
                                     int variantCount,
                                     boolean shuffleQuestions,
                                     boolean shuffleOptions,
                                     Long seed) throws IOException {

        TestEntity test = testRepository.findById(testId)
            .orElseThrow(() -> new IllegalArgumentException("Test topilmadi: " + testId));

        int available = test.getQuestions().size();
        if (available == 0) throw new IllegalArgumentException("Testda savollar yo'q.");
        if (questionCount <= 0) throw new IllegalArgumentException("Savollar soni noto'g'ri.");
        if (variantCount <= 0) throw new IllegalArgumentException("Variantlar soni noto'g'ri.");

        int qCount = Math.min(questionCount, available);

        Long usedSeed = (seed != null) ? seed : Instant.now().toEpochMilli();

        GenerationEntity gen = new GenerationEntity(test, qCount, variantCount, shuffleQuestions, shuffleOptions, usedSeed);
        gen = generationRepository.save(gen);

        Path genDir = storageService.resolve("gen-" + gen.getId());
        Files.createDirectories(genDir);

        for (int v = 1; v <= variantCount; v++) {
            Random random = new Random(usedSeed + v);

            List<QuestionEntity> qs = new ArrayList<>(test.getQuestions());
            if (shuffleQuestions) {
                for (int i = qs.size() - 1; i > 0; i--) {
                    int k = random.nextInt(i + 1);
                    QuestionEntity tmp = qs.get(i);
                    qs.set(i, qs.get(k));
                    qs.set(k, tmp);
                }
            }

            List<QuestionEntity> picked = qs.subList(0, qCount);

            var render = pdfRenderService.renderVariantPdf(test, v, picked, shuffleOptions, random);
            String fileName = "variant-" + v + ".pdf";
            Path out = genDir.resolve(fileName).normalize();
            Files.write(out, render.pdfBytes());

            GeneratedVariantEntity variant = new GeneratedVariantEntity(gen, v, out.toString(), render.answerKeyText());
            variantRepository.save(variant);
        }

        return gen;
    }

    public List<GeneratedVariantEntity> getVariants(Long generationId) {
        return variantRepository.findByGenerationIdOrderByVariantNoAsc(generationId);
    }

    public GenerationEntity getGeneration(Long id) {
        return generationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Generation topilmadi: " + id));
    }
    @Transactional(readOnly = true)
    public byte[] loadVariantPdf(Long genId, int variantNo) {
        GeneratedVariantEntity v = variantRepository
            .findByGenerationIdAndVariantNo(genId, variantNo)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Variant topilmadi: genId=" + genId + ", variantNo=" + variantNo
            ));

        String pdfPath = v.getPdfPath();   // sizda aynan shu saqlangan
        if (pdfPath == null || pdfPath.isBlank()) {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "PDF path saqlanmagan: genId=" + genId + ", variantNo=" + variantNo
            );
        }

        try {
            Path path = Path.of(pdfPath).normalize();
            if (!Files.exists(path)) {
                throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "PDF fayl topilmadi: " + pdfPath
                );
            }
            return Files.readAllBytes(path);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (IOException e) {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "PDF o‘qishda xatolik: " + e.getMessage()
            );
        }
    }


    public Path getVariantFile(Long generationId, Long variantId) {
        GeneratedVariantEntity v = variantRepository.findById(variantId)
            .orElseThrow(() -> new IllegalArgumentException("Variant topilmadi: " + variantId));
        if (!v.getGeneration().getId().equals(generationId)) {
            throw new IllegalArgumentException("Variant generation ga tegishli emas.");
        }
        return Path.of(v.getPdfPath());
    }
}
