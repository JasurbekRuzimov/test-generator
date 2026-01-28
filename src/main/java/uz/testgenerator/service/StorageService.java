package uz.testgenerator.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class StorageService {

    private final Path root;

    public StorageService(@Value("${app.storage.dir:storage}") String dir) {
        this.root = Paths.get(dir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.root);
        } catch (IOException e) {
            throw new RuntimeException("Storage folder yaratib bo'lmadi: " + this.root, e);
        }
    }

    public Path getRoot() {
        return root;
    }

    public Path resolve(String relative) {
        return root.resolve(relative).normalize();
    }
}
