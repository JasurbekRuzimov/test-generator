package uz.testgenerator.web;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uz.testgenerator.repo.GenerationRepository;
import uz.testgenerator.repo.TestRepository;
import uz.testgenerator.service.GenerationService;
import uz.testgenerator.service.TestImportService;

import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Controller
public class AdminPageController {

    private final TestRepository testRepository;
    private final GenerationRepository generationRepository;
    private final TestImportService importService;
    private final GenerationService generationService;

    public AdminPageController(TestRepository testRepository,
                               GenerationRepository generationRepository,
                               TestImportService importService,
                               GenerationService generationService) {
        this.testRepository = testRepository;
        this.generationRepository = generationRepository;
        this.importService = importService;
        this.generationService = generationService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("pageTitle", "Dashboard");
        model.addAttribute("pageSubtitle", "Test Generator admin panel");

        long tests = testRepository.count();
        long generations = generationRepository.count();

        model.addAttribute("stats", Map.of("tests", tests, "generations", generations));

        var recent = generationRepository.findTop10ByOrderByCreatedAtDesc().stream().map(g -> Map.of(
            "id", g.getId(),
            "testTitle", g.getTest().getTitle(),
            "variants", g.getVariantCount(),
            "status", g.getStatus(),
            "createdAt", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                .withZone(ZoneId.systemDefault()).format(g.getCreatedAt())
        )).toList();

        model.addAttribute("recentGenerations", recent);
        return "dashboard";
    }

    @GetMapping("/tests")
    public String tests(Model model) {
        model.addAttribute("pageTitle", "Tests");
        model.addAttribute("pageSubtitle", "Import qilingan testlar ro'yxati");

        var list = testRepository.findAllWithQuestionCount().stream().map(row -> {
            Long id = (Long) row[0];
            String title = (String) row[1];
            var createdAt = (java.time.Instant) row[2];   // sizda Instant bo‘lsa
            Long questionCount = (Long) row[3];

            return Map.of(
                "id", id,
                "title", title,
                "questionCount", questionCount,
                "createdAt", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                    .withZone(ZoneId.systemDefault()).format(createdAt)
            );
        }).toList();

        model.addAttribute("tests", list);
        return "tests";
    }


    @GetMapping("/tests/import")
    public String importTest(Model model) {
        model.addAttribute("pageTitle", "Import Test");
        model.addAttribute("pageSubtitle", "TXT yoki DOCX fayl yuklang");
        return "import-test";
    }

    @PostMapping("/tests/import")
    public String doImport(@RequestParam("title") String title,
                           @RequestParam("file") MultipartFile file,
                           Model model,
                           RedirectAttributes ra) throws IOException {

        model.addAttribute("pageTitle", "Import Test");
        model.addAttribute("pageSubtitle", "TXT yoki DOCX fayl yuklang");

        if (file == null || file.isEmpty()) {
            model.addAttribute("errorMessage", "Fayl tanlanmadi.");
            return "import-test";
        }

        var preview = importService.preview(file);
        model.addAttribute("preview", preview);

        if (!preview.errors().isEmpty()) {
            model.addAttribute("errorMessage", "Importda xatolar bor. Pastdagi ro'yxatni tekshiring.");
            return "import-test";
        }

        var saved = importService.importAndSave(title, file);
        ra.addFlashAttribute("toastSuccess", "Test saqlandi. ID: " + saved.getId());
        return "redirect:/tests";
    }

    @GetMapping("/generate")
    public String generate(Model model) {
        model.addAttribute("pageTitle", "Generate PDF");
        model.addAttribute("pageSubtitle", "Test tanlab, variantlar yarating");

        var tests = testRepository.findAllWithQuestionCount().stream().map(row -> Map.of(
            "id", (Long) row[0],
            "title", (String) row[1],
            "questionCount", (Long) row[3]
        )).toList();

        model.addAttribute("tests", tests);
        return "generate";
    }


    @PostMapping("/generate")
    public String doGenerate(@RequestParam("testId") Long testId,
                             @RequestParam("questionCount") int questionCount,
                             @RequestParam("variantCount") int variantCount,
                             @RequestParam(value = "shuffleQuestions", defaultValue = "false") boolean shuffleQuestions,
                             @RequestParam(value = "shuffleOptions", defaultValue = "false") boolean shuffleOptions,
                             @RequestParam(value = "seed", required = false) String seed,
                             RedirectAttributes ra) throws IOException {

        Long parsedSeed = null;
        if (seed != null && !seed.isBlank()) {
            try {
                parsedSeed = Long.parseLong(seed.trim());
            } catch (NumberFormatException ignored) {
                // keep null
            }
        }

        var gen = generationService.generate(testId, questionCount, variantCount, shuffleQuestions, shuffleOptions, parsedSeed);
        ra.addFlashAttribute("toastSuccess", "Generation tayyor. ID: " + gen.getId());
        return "redirect:/generations/" + gen.getId();
    }

    @GetMapping("/generations/{id}")
    public String generationDetails(@PathVariable("id") Long id, Model model) {
        var gen = generationService.getGeneration(id);
        var variants = generationService.getVariants(id).stream().map(v -> Map.of(
            "id", v.getId(),
            "no", v.getVariantNo(),
            "status", gen.getStatus(),
            "answerKey", v.getAnswerKeyText()
        )).toList();

        model.addAttribute("pageTitle", "Generation #" + id);
        model.addAttribute("pageSubtitle", gen.getTest().getTitle());

        model.addAttribute("gen", gen);
        model.addAttribute("variants", variants);
        return "generation-details";
    }

    @GetMapping("/generations/{genId}/download/{variantNo}")
    public ResponseEntity<byte[]> downloadVariantPdf(@PathVariable Long genId,
        @PathVariable int variantNo) {

        byte[] pdf = generationService.loadVariantPdf(genId, variantNo);

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"generation-" + genId + "-variant-" + variantNo + ".pdf\"")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdf);
    }
}
