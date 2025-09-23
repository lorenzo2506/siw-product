package it.uniroma3.siw.controller;

import it.uniroma3.siw.model.Product;
import it.uniroma3.siw.model.Comment;
import it.uniroma3.siw.service.ProductService;
import it.uniroma3.siw.service.CommentService;
import it.uniroma3.siw.service.ImageStorageService;
import it.uniroma3.siw.validator.ImageValidator;
import jakarta.validation.Valid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;

@Controller
@SessionAttributes({"product", "editProduct", "mostraCampoNuovo"})
public class ProductController {
    
    @Autowired private ProductService productService;
    @Autowired private CommentService commentService;
    @Autowired private ImageStorageService imageStorageService;
    @Autowired private ImageValidator imageValidator;
    
    @GetMapping("/products")
    public String getAllProducts(Model model) {
        model.addAttribute("products", productService.findAll());
        model.addAttribute("categories", productService.findAllCategories());
        return "products";
    }
    
    @GetMapping("/products/category/{category}")
    public String getProductsByCategory(@PathVariable String category, Model model) {
        model.addAttribute("products", productService.findByCategory(category));
        model.addAttribute("categories", productService.findAllCategories());
        return "/products";
    }
    
    @GetMapping("/products/search")
    public String searchProducts(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "false") boolean showFilters,
            Model model) {
        
        List<Product> products = productService.searchProducts(query, minPrice, maxPrice);
        
        model.addAttribute("products", products);
        model.addAttribute("categories", productService.findAllCategories());
        model.addAttribute("searchQuery", query);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("showFilters", showFilters);
        
        return "products";
    }
    
    
    @GetMapping("/product/{id}")
    public String getProduct(@PathVariable Long id, Model model) {
        Product product = productService.findById(id);
        model.addAttribute("product", product);
        return "product";
    }
    
    @GetMapping("/admin/product/new")
    public String showNewProductForm(Model model) {
        Product product = new Product();
        product.setSimilarProducts(new ArrayList<>());
        model.addAttribute("product", product);
        model.addAttribute("mostraCampoNuovo", false);
        return "admin/formNewProduct";
    }
    
    @PostMapping("/admin/product/new")
    public String addProduct(@Valid @ModelAttribute("product") Product product, 
                             BindingResult bindingResult,
                             @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                             @RequestParam(required = false) String aggiungiSimilarProduct,
                             @RequestParam(required = false) String rimuoviSimilarProduct,
                             @RequestParam(required = false) String chiudiCampoNuovo,
                             @RequestParam(required = false) String newSimilarName,
                             @RequestParam(required = false) String newSimilarCategory,
                             SessionStatus sessionStatus,
                             Model model) {
        
        if (product.getSimilarProducts() == null) {
            product.setSimilarProducts(new ArrayList<>());
        }
        
        // Gestione chiusura campo nuovo (cestino)
        if (chiudiCampoNuovo != null) {
            model.addAttribute("mostraCampoNuovo", false);
            return "admin/formNewProduct";
        }
        
        // Gestione aggiunta prodotto simile
        if (aggiungiSimilarProduct != null) {
            // Se i campi sono entrambi vuoti, mostra solo il form
            if ((newSimilarName == null || newSimilarName.trim().isEmpty()) && 
                (newSimilarCategory == null || newSimilarCategory.trim().isEmpty())) {
                model.addAttribute("mostraCampoNuovo", true);
                return "admin/formNewProduct";
            }
            
            // Se i campi sono parzialmente compilati, errore
            if ((newSimilarName == null || newSimilarName.trim().isEmpty()) || 
                (newSimilarCategory == null || newSimilarCategory.trim().isEmpty())) {
                model.addAttribute("error", "Compila tutti i campi del prodotto simile");
                model.addAttribute("mostraCampoNuovo", true);
                return "admin/formNewProduct";
            }
            
            // Cerca il prodotto nel database
            Product similar = productService.findByNameAndCategory(newSimilarName, newSimilarCategory);
            
            if (similar == null) {
                model.addAttribute("error", "Prodotto non trovato nel database");
                model.addAttribute("mostraCampoNuovo", true);
            } else if (product.getSimilarProducts().contains(similar)) {
                model.addAttribute("error", "Prodotto già aggiunto alla lista");
                model.addAttribute("mostraCampoNuovo", true);
            } else {
                // Aggiunta riuscita
                product.getSimilarProducts().add(similar);
                model.addAttribute("mostraCampoNuovo", false);
            }
            return "admin/formNewProduct";
        }
        
        // Gestione rimozione prodotto simile
        if (rimuoviSimilarProduct != null) {
            int index = Integer.parseInt(rimuoviSimilarProduct);
            if (index >= 0 && index < product.getSimilarProducts().size()) {
                product.getSimilarProducts().remove(index);
            }
            model.addAttribute("mostraCampoNuovo", false);
            return "admin/formNewProduct";
        }
        
        // Validazione immagine
        imageValidator.validateSingleImage(imageFile, bindingResult, "name");
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("mostraCampoNuovo", false);
            return "admin/formNewProduct";
        }
        
        // Salvataggio prodotto
        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                String imagePath = imageStorageService.saveImage(imageFile);
                product.setImagePath(imagePath);
            }
            
            Product saved = productService.save(product);
            
            // Aggiungi le relazioni bidirezionali per i prodotti simili
            List<Product> similarsToAdd = new ArrayList<>(product.getSimilarProducts());
            for (Product similar : similarsToAdd) {
                productService.addSimilarProduct(saved.getId(), similar.getId());
            }
            
            sessionStatus.setComplete();
            return "redirect:/products";
            
        } catch (IOException e) {
            bindingResult.rejectValue("name", "error.product.image", 
                "Errore nel caricamento immagine: " + e.getMessage());
            model.addAttribute("mostraCampoNuovo", false);
            return "admin/formNewProduct";
        }
    }
    
    @GetMapping("/admin/product/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        Product product = productService.findById(id);
        
        if (product != null && product.getImagePath() != null) {
            imageStorageService.deleteImage(product.getImagePath());
        }
        
        productService.delete(id);
        return "redirect:/products";
    }
    
    @GetMapping("/admin/product/{productId}/edit")
    public String showEditForm(@PathVariable("productId") Long productId, Model model) {
        Product product = this.productService.findById(productId);
        model.addAttribute("editProduct", product);
        model.addAttribute("mostraCampoNuovo", false);
        return "admin/formEditProduct";
    }

    @PostMapping("/admin/product/{productId}/edit")
    public String edit(@PathVariable("productId") Long productId,
                      @Valid @ModelAttribute("editProduct") Product editProduct,
                      BindingResult bindingResult,
                      @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                      @RequestParam(required = false) String aggiungiSimilarProduct,
                      @RequestParam(required = false) String rimuoviSimilarProduct,
                      @RequestParam(required = false) String chiudiCampoNuovo,
                      @RequestParam(required = false) String newSimilarName,
                      @RequestParam(required = false) String newSimilarCategory,
                      SessionStatus sessionStatus,
                      Model model) {
        
        Product product = this.productService.findById(productId);
        
        if (product == null) {
            throw new IllegalArgumentException("Prodotto non trovato");
        }
        
        // Gestione chiusura campo nuovo (cestino)
        if (chiudiCampoNuovo != null) {
            model.addAttribute("editProduct", product);
            model.addAttribute("mostraCampoNuovo", false);
            return "admin/formEditProduct";
        }
        
        // Gestione aggiunta prodotto simile
        if (aggiungiSimilarProduct != null) {
            // Se i campi sono entrambi vuoti, mostra solo il form
            if ((newSimilarName == null || newSimilarName.trim().isEmpty()) && 
                (newSimilarCategory == null || newSimilarCategory.trim().isEmpty())) {
                model.addAttribute("editProduct", product);
                model.addAttribute("mostraCampoNuovo", true);
                return "admin/formEditProduct";
            }
            
            // Se i campi sono parzialmente compilati, errore
            if ((newSimilarName == null || newSimilarName.trim().isEmpty()) || 
                (newSimilarCategory == null || newSimilarCategory.trim().isEmpty())) {
                model.addAttribute("error", "Compila tutti i campi del prodotto simile");
                model.addAttribute("editProduct", product);
                model.addAttribute("mostraCampoNuovo", true);
                return "admin/formEditProduct";
            }
            
            // Cerca il prodotto nel database
            Product similar = productService.findByNameAndCategory(newSimilarName, newSimilarCategory);
            
            if (similar == null) {
                model.addAttribute("error", "Prodotto non trovato nel database");
                model.addAttribute("editProduct", product);
                model.addAttribute("mostraCampoNuovo", true);
            } else if (similar.getId().equals(productId)) {
                model.addAttribute("error", "Non puoi aggiungere il prodotto a se stesso");
                model.addAttribute("editProduct", product);
                model.addAttribute("mostraCampoNuovo", true);
            } else if (product.getSimilarProducts().contains(similar)) {
                model.addAttribute("error", "Prodotto già presente nella lista");
                model.addAttribute("editProduct", product);
                model.addAttribute("mostraCampoNuovo", true);
            } else {
                // Aggiunta riuscita - salva subito la relazione bidirezionale
                product.getSimilarProducts().add(similar);
                similar.getSimilarProducts().add(product);
                productService.save(product);
                productService.save(similar);
                model.addAttribute("editProduct", product);
                model.addAttribute("mostraCampoNuovo", false);
            }
            return "admin/formEditProduct";
        }
        
        // Gestione rimozione prodotto simile
        if (rimuoviSimilarProduct != null) {
            int index = Integer.parseInt(rimuoviSimilarProduct);
            List<Product> similars = new ArrayList<>(product.getSimilarProducts());
            if (index >= 0 && index < similars.size()) {
                Product toRemove = similars.get(index);
                product.getSimilarProducts().remove(toRemove);
                toRemove.getSimilarProducts().remove(product);
                productService.save(product);
                productService.save(toRemove);
            }
            model.addAttribute("editProduct", product);
            model.addAttribute("mostraCampoNuovo", false);
            return "admin/formEditProduct";
        }
        
        // Validazione immagine
        imageValidator.validateSingleImage(imageFile, bindingResult, "name");
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("editProduct", product);
            model.addAttribute("mostraCampoNuovo", false);
            return "admin/formEditProduct";
        }
        
        // Salvataggio modifiche prodotto
        try {
            product.setName(editProduct.getName());
            product.setPrice(editProduct.getPrice());
            product.setCategory(editProduct.getCategory());
            product.setDescription(editProduct.getDescription());
            
            if (imageFile != null && !imageFile.isEmpty()) {
                if (product.getImagePath() != null) {
                    imageStorageService.deleteImage(product.getImagePath());
                }
                String imagePath = imageStorageService.saveImage(imageFile);
                product.setImagePath(imagePath);
            }
            
            productService.save(product);
            sessionStatus.setComplete();
            return "redirect:/product/" + productId;
            
        } catch (IOException e) {
            model.addAttribute("error", "Errore nel caricamento immagine: " + e.getMessage());
            model.addAttribute("editProduct", product);
            model.addAttribute("mostraCampoNuovo", false);
            return "admin/formEditProduct";
        }
    }
}