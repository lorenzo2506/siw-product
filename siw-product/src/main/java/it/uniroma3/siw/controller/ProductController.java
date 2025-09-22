package it.uniroma3.siw.controller;

import it.uniroma3.siw.model.Product;
import it.uniroma3.siw.model.Comment;
import it.uniroma3.siw.model.User;
import it.uniroma3.siw.service.ProductService;
import it.uniroma3.siw.service.CommentService;
import jakarta.validation.Valid;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class ProductController {
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private CommentService commentService;
    
    // Visualizza tutti i prodotti
    @GetMapping("/products")
    public String getAllProducts(Model model) {
        model.addAttribute("products", productService.findAll());
        return "products";
    }
    
    // Visualizza prodotti per categoria
    @GetMapping("/products/category/{category}")
    public String getProductsByCategory(@PathVariable String category, Model model) {
        model.addAttribute("products", productService.findByCategory(category));
        model.addAttribute("category", category);
        return "productsByCategory";
    }
    
    // Visualizza dettaglio prodotto
    @GetMapping("/product/{id}")
    public String getProduct(@PathVariable Long id, Model model) {
        Product product = productService.findById(id);
        model.addAttribute("product", product);
        model.addAttribute("comment", new Comment());
        return "product";
    }
    
    // Form per nuovo prodotto (ADMIN)
    @GetMapping("/admin/product/new")
    public String showNewProductForm(Model model) {
        model.addAttribute("product", new Product());
        return "admin/formNewProduct";
    }
    
    // Salva nuovo prodotto (ADMIN)
    @PostMapping("/admin/product/new")
    public String addProduct(@Valid @ModelAttribute("product") Product product, 
                             BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "admin/formNewProduct";
        }
        productService.save(product);
        return "redirect:/products";
    }
    
    
    // Elimina prodotto (ADMIN)
    @GetMapping("/admin/product/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteById(id);
        return "redirect:/products";
    }
    
    // Gestione prodotti simili (ADMIN)
    @GetMapping("/admin/product/{id}/addSimilar")
    public String showAddSimilarForm(@PathVariable Long id, Model model) {
        Product product = productService.findById(id);
        model.addAttribute("product", product);
        // NON serve più passare availableProducts!
        return "admin/addSimilarProduct";
    }
    
    @PostMapping("/admin/product/{productId}/addSimilar")
    public String addSimilarProduct(@PathVariable Long productId, 
                                   @RequestParam String name,
                                   @RequestParam String category,
                                   Model model) {
        
        Product product = productService.findById(productId);        
        Product similarProduct = productService.findByNameAndCategory(name, category);
        
        if (similarProduct == null) {
            model.addAttribute("error", "Prodotto non trovato con nome e categoria specificati");
            model.addAttribute("product", product);
            return "admin/addSimilarProduct";
        }
        
        if (similarProduct.getId().equals(productId)) {
            model.addAttribute("error", "Non puoi aggiungere il prodotto a se stesso");
            model.addAttribute("product", product);
            return "admin/addSimilarProduct";
        }
        
        if (product.getSimilarProducts().contains(similarProduct)) {
            model.addAttribute("error", "Questo prodotto è già nella lista dei simili");
            model.addAttribute("product", product);
            return "admin/addSimilarProduct";
        }
        
        productService.addSimilarProduct(productId, similarProduct.getId());
        
        return "redirect:/product/" + productId;
    }
    
    
    @PostMapping("/admin/product/{productId}/removeSimilar/{similarId}")
    public String removeSimilarProduct(@PathVariable Long productId, 
                                      @PathVariable Long similarId,
                                      Model model) {
        
        Product product = productService.findById(productId);
        Product similarProduct = productService.findById(similarId);
        
        // 1. Controlla se i prodotti esistono
        if (product == null || similarProduct == null) {
            return "redirect:/products";
        }
        
        // 2. Controlla se similarProduct è effettivamente nella lista
        if (!product.getSimilarProducts().contains(similarProduct)) {
            return "redirect:/product/" + productId;
        }
        
        // 3. Rimuovi (bidirezionale già gestito nel service)
        productService.removeSimilarProduct(productId, similarId);
        
        return "redirect:/product/" + productId;
    }
    
    
    @GetMapping("/admin/product/{productId}/edit")
    public String showEditForm(@PathVariable("productId") Long productId, Model model) {
    	
    	Product product = this.productService.findById(productId);
    	model.addAttribute("product", product);
    	return "admin/productEditForm";
    }
    
    
    @PostMapping("/admin/product/{productId}/edit")
    public String edit(@ModelAttribute("formProduct") Product formProduct, @PathVariable("productId") Long productId) {
    	
    	Product product = this.productService.findById(productId);
    	this.productService.edit(product, formProduct);
    	return "redirect:/product"+productId;
    }
    
 
}