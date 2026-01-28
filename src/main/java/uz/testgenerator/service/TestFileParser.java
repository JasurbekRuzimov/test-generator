package uz.testgenerator.service;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class TestFileParser {

    public ParsedTest parse(MultipartFile file) throws IOException {
        String name = Optional.ofNullable(file.getOriginalFilename()).orElse("").toLowerCase(Locale.ROOT);
        if (name.endsWith(".docx")) {
            return parseDocx(file);
        }
        return parseTxt(file);
    }

    public ParsedTest parseTxt(MultipartFile file) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        }
        return parseLines(lines);
    }

    public ParsedTest parseDocx(MultipartFile file) throws IOException {
        List<String> lines = new ArrayList<>();
        try (XWPFDocument doc = new XWPFDocument(file.getInputStream())) {
            lines = doc.getParagraphs().stream()
                .map(p -> p.getText() == null ? "" : p.getText())
                .collect(Collectors.toList());
        }
        return parseLines(lines);
    }

    private ParsedTest parseLines(List<String> rawLines) {

        List<String> lines = rawLines.stream()
            .map(l -> l == null ? "" : l.stripTrailing())
            .toList();

        List<List<String>> blocks = new ArrayList<>();
        List<String> current = new ArrayList<>();
        for (String line : lines) {
            if (line.trim().equals("---")) {
                if (!current.isEmpty()) {
                    blocks.add(current);
                    current = new ArrayList<>();
                }
                continue;
            }
            if (!line.isBlank()) current.add(line);
        }
        if (!current.isEmpty()) blocks.add(current);

        List<ParsedQuestion> questions = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < blocks.size(); i++) {
            List<String> b = blocks.get(i);
            try {
                questions.add(parseQuestionBlock(b));
            } catch (IllegalArgumentException ex) {
                errors.add("Savol #" + (i + 1) + ": " + ex.getMessage());
            }
        }

        return new ParsedTest(questions, errors);
    }

    private ParsedQuestion parseQuestionBlock(List<String> block) {
        String qText = null;
        Map<Character, String> opts = new LinkedHashMap<>();
        Character ans = null;

        for (String raw : block) {
            String line = raw.trim();
            if (line.toUpperCase(Locale.ROOT).startsWith("Q:")) {
                qText = line.substring(2).trim();
            } else if (line.matches("^[A-Da-d]\\).*")) {
                char key = Character.toUpperCase(line.charAt(0));
                String text = line.substring(2).trim();
                opts.put(key, text);
            } else if (line.toUpperCase(Locale.ROOT).startsWith("ANS:")) {
                String v = line.substring(4).trim();
                if (v.isEmpty()) throw new IllegalArgumentException("ANS bo'sh.");
                ans = Character.toUpperCase(v.charAt(0));
            }
        }

        if (qText == null || qText.isBlank()) throw new IllegalArgumentException("Q: topilmadi yoki bo'sh.");
        for (char c : new char[]{'A','B','C','D'}) {
            if (!opts.containsKey(c) || opts.get(c).isBlank()) {
                throw new IllegalArgumentException(c + ") varianti yo'q yoki bo'sh.");
            }
        }
        if (ans == null || (ans < 'A' || ans > 'D')) throw new IllegalArgumentException("ANS noto'g'ri. A/B/C/D bo'lishi kerak.");

        List<String> options = List.of(opts.get('A'), opts.get('B'), opts.get('C'), opts.get('D'));
        int correctIndex = ans - 'A';
        return new ParsedQuestion(qText, options, correctIndex);
    }

    public record ParsedTest(List<ParsedQuestion> questions, List<String> errors) {}
    public record ParsedQuestion(String text, List<String> options, int correctIndex) {}
}
