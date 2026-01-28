package uz.testgenerator.web;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import uz.testgenerator.service.GenerationService;

import java.nio.file.Path;

@Controller
@RequestMapping("/generations/{genId}/variants")
public class FileDownloadController {

    private final GenerationService generationService;

    public FileDownloadController(GenerationService generationService) {
        this.generationService = generationService;
    }

    @GetMapping("/{variantId}/download")
    @ResponseBody
    public ResponseEntity<Resource> download(@PathVariable Long genId, @PathVariable Long variantId) {
        Path path = generationService.getVariantFile(genId, variantId);
        Resource resource = new FileSystemResource(path);

        String filename = path.getFileName().toString();

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .body(resource);
    }
}
