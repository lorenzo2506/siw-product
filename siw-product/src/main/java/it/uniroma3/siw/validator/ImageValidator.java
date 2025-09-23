package it.uniroma3.siw.validator;

import it.uniroma3.siw.service.ImageStorageService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;


@Component
public class ImageValidator {
    
    @Autowired
    private ImageStorageService imageStorageService;
    
    public void validateSingleImage(MultipartFile file, BindingResult bindingResult, String fieldName) {
        if (file == null || file.isEmpty()) {
            return; // Immagine opzionale
        }
        
        // Tipo file
        if (!imageStorageService.isValidImageType(file.getContentType())) {
            bindingResult.rejectValue(fieldName, "error.image.type", 
                "Tipo file non valido. Usa solo JPEG, PNG, GIF o WebP");
        }
        
        // Dimensione
        if (file.getSize() > 5 * 1024 * 1024) {
            bindingResult.rejectValue(fieldName, "error.image.size", 
                "File troppo grande. Dimensione massima: 5MB");
        }
    }
    
    public void validateMultipleImages(MultipartFile[] files, BindingResult bindingResult, String fieldName) {
        if (files == null || imageStorageService.areAllFilesEmpty(files)) {
            bindingResult.rejectValue(fieldName, "error.images.required", 
                "Almeno un'immagine è obbligatoria");
            return;
        }
        
        if (!imageStorageService.areValidImageTypes(files)) {
            bindingResult.rejectValue(fieldName, "error.images.type", 
                "Tutti i file devono essere immagini valide");
        }
        
        if (imageStorageService.countNonEmptyFiles(files) > 5) {
            bindingResult.rejectValue(fieldName, "error.images.count", 
                "Massimo 5 immagini consentite");
        }
        
        if (!imageStorageService.isValidTotalSize(files, 20 * 1024 * 1024)) {
            bindingResult.rejectValue(fieldName, "error.images.totalSize", 
                "Dimensione totale massima: 20MB");
        }
    }
}