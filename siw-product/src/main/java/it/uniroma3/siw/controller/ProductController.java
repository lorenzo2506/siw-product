package it.uniroma3.siw.controller;

import it.uniroma3.siw.model.Product;
import it.uniroma3.siw.model.Comment;
import it.uniroma3.siw.model.User;
import it.uniroma3.siw.service.ProductService;
import it.uniroma3.siw.service.CommentService;
import jakarta.validation.Valid;
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
    @PostMapping("/admin/product")
    public String saveProduct(@Valid @ModelAttribute("product") Product product, 
                             BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "admin/formNewProduct";
        }
        productService.save(product);
        return "redirect:/products";
    }
    
    // Form per modificare prodotto (ADMIN)
    @GetMapping("/admin/product/edit/{id}")
    public String showEditProductForm(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.findById(id));
        return "admin/formEditProduct";
    }
    
    // Aggiorna prodotto (ADMIN)
    @PostMapping("/admin/product/edit/{id}")
    public String updateProduct(@PathVariable Long id, 
                               @Valid @ModelAttribute("product") Product product,
                               BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "admin/formEditProduct";
        }
        product.setId(id);
        productService.save(product);
        return "redirect:/product/" + id;
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
        model.addAttribute("availableProducts", productService.findProductsNotInSimilar(id));
        return "admin/addSimilarProduct";
    }
    
    @PostMapping("/admin/product/{productId}/addSimilar/{similarId}")
    public String addSimilarProduct(@PathVariable Long productId, 
                                   @PathVariable Long similarId) {
    	
        productService.addSimilarProduct(productId, similarId);
        return "redirect:/product/" + productId;
    }
    
    @GetMapping("/admin/product/{productId}/removeSimilar/{similarId}")
    public String removeSimilarProduct(@PathVariable Long productId, 
                                      @PathVariable Long similarId) {
        productService.removeSimilarProduct(productId, similarId);
        return "redirect:/product/" + productId;
    }
    
    // Aggiungi commento (USER)
    @PostMapping("/product/{id}/comment")
    public String addComment(@PathVariable Long id, 
                            @Valid @ModelAttribute("comment") Comment comment,
                            BindingResult bindingResult,
                            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("product", productService.findById(id));
            return "product";
        }
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        
        comment.setProduct(productService.findById(id));
        comment.setUser(currentUser);
        commentService.save(comment);
        
        return "redirect:/product/" + id;
    }
    
    // Elimina commento (solo il proprio)
    @GetMapping("/comment/delete/{id}")
    public String deleteComment(@PathVariable Long id) {
        Comment comment = commentService.findById(id);
        Long productId = comment.getProduct().getId();
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        
        if (comment.getUser().getId().equals(currentUser.getId())) {
            commentService.deleteById(id);
        }
        
        return "redirect:/product/" + productId;
    }
}