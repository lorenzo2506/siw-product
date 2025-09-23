package it.uniroma3.siw.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;

@Service
public class ImageStorageService {
    
	@PostConstruct
	public void init() {
	    Path uploadPath = Paths.get(uploadDir);
	    System.out.println("=== IMAGE STORAGE CONFIG ===");
	    System.out.println("Upload dir: " + uploadDir);
	    System.out.println("Absolute path: " + uploadPath.toAbsolutePath());
	    System.out.println("Exists: " + Files.exists(uploadPath));
	}
	
    @Value("${app.image.upload.dir:uploads/images}")
    private String uploadDir;
    
    // Salva singola immagine (per Author)
    public String saveImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("File vuoto");
        }
        
        // Crea la directory se non esiste
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Genera nome file unico
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueFilename = UUID.randomUUID().toString() + extension;
        
        // Salva il file
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        return uniqueFilename;
    }
    
    // Salva multiple immagini (per Book - max 5)
    public List<String> saveImages(MultipartFile[] files) throws IOException {
        List<String> savedPaths = new ArrayList<>();
        
        if (files == null || files.length == 0) {
            throw new IOException("Nessun file fornito");
        }
        
        int count = 0;
        for (MultipartFile file : files) {
            if (!file.isEmpty() && count < 5) { // Massimo 5 immagini
                String savedPath = saveImage(file);
                savedPaths.add(savedPath);
                count++;
            }
        }
        
        if (savedPaths.isEmpty()) {
            throw new IOException("Tutti i file erano vuoti");
        }
        
        return savedPaths;
    }
    
    // Elimina singola immagine
    public void deleteImage(String filename) {
        try {
            if (filename != null && !filename.isEmpty()) {
                Path filePath = Paths.get(uploadDir).resolve(filename);
                Files.deleteIfExists(filePath);
            }
        } catch (IOException e) {
            System.err.println("Errore nella cancellazione dell'immagine: " + filename);
        }
    }
    
    // Elimina multiple immagini
    public void deleteImages(List<String> filenames) {
        if (filenames != null) {
            for (String filename : filenames) {
                deleteImage(filename);
            }
        }
    }
    
    // Validazione tipo file
    public boolean isValidImageType(String contentType) {
        return contentType != null && (
            contentType.equals("image/jpeg") ||
            contentType.equals("image/jpg") ||
            contentType.equals("image/png") ||
            contentType.equals("image/gif") ||
            contentType.equals("image/webp")
        );
    }
    
    // Validazione multiple immagini
    public boolean areValidImageTypes(MultipartFile[] files) {
        if (files == null) return false;
        
        for (MultipartFile file : files) {
            if (!file.isEmpty() && !isValidImageType(file.getContentType())) {
                return false;
            }
        }
        return true;
    }
    
    // Conta file non vuoti
    public int countNonEmptyFiles(MultipartFile[] files) {
        if (files == null) return 0;
        
        int count = 0;
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                count++;
            }
        }
        return count;
    }
    
    // Controlla se tutti i file sono vuoti
    public boolean areAllFilesEmpty(MultipartFile[] files) {
        if (files == null) return true;
        
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                return false;
            }
        }
        return true;
    }
    
    // Validazione dimensione totale
    public boolean isValidTotalSize(MultipartFile[] files, long maxTotalSize) {
        if (files == null) return true;
        
        long totalSize = 0;
        for (MultipartFile file : files) {
            totalSize += file.getSize();
        }
        
        return totalSize <= maxTotalSize;
    }
}