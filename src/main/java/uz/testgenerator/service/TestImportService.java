package uz.testgenerator.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import uz.testgenerator.domain.OptionEntity;
import uz.testgenerator.domain.QuestionEntity;
import uz.testgenerator.domain.TestEntity;
import uz.testgenerator.repo.TestRepository;

import java.io.IOException;

@Service
public class TestImportService {

    private final TestFileParser parser;
    private final TestRepository testRepository;

    public TestImportService(TestFileParser parser, TestRepository testRepository) {
        this.parser = parser;
        this.testRepository = testRepository;
    }

    public TestFileParser.ParsedTest preview(MultipartFile file) throws IOException {
        return parser.parse(file);
    }

    @Transactional
    public TestEntity importAndSave(String title, MultipartFile file) throws IOException {
        var parsed = parser.parse(file);
        if (!parsed.errors().isEmpty()) {
            throw new IllegalArgumentException("Importda xatolar bor: " + String.join("; ", parsed.errors()));
        }
        if (parsed.questions().isEmpty()) {
            throw new IllegalArgumentException("Savollar topilmadi.");
        }

        TestEntity test = new TestEntity(title == null || title.isBlank() ? "Untitled Test" : title.trim());
        for (var pq : parsed.questions()) {
            QuestionEntity q = new QuestionEntity(pq.text(), pq.correctIndex());
            for (int i = 0; i < pq.options().size(); i++) {
                q.addOption(new OptionEntity(pq.options().get(i), i));
            }
            test.addQuestion(q);
        }
        return testRepository.save(test);
    }
}
