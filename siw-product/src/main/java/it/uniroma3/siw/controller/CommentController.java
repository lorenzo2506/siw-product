package it.uniroma3.siw.controller;

import it.uniroma3.siw.model.Comment;
import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.service.CommentService;
import it.uniroma3.siw.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class CommentController {
    
    @Autowired
    private CommentService commentService;
    
    @Autowired
    private ProductService productService;
    
    // Aggiungi commento
    @PostMapping("/product/{productId}/comment")
    public String addComment(@PathVariable Long productId, 
                            @Valid @ModelAttribute("comment") Comment comment,
                            BindingResult bindingResult,
                            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("product", productService.findById(productId));
            return "product";
        }
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Credentials credentials = (Credentials) auth.getPrincipal();
        
        comment.setProduct(productService.findById(productId));
        comment.setUser(credentials.getUser());
        commentService.save(comment);
        
        return "redirect:/product/" + productId;
    }
    
    // Form per modificare commento
    @GetMapping("/comment/edit/{id}")
    public String showEditCommentForm(@PathVariable Long id, Model model) {
        Comment comment = commentService.findById(id);
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Credentials credentials = (Credentials) auth.getPrincipal();
        
        // Verifica che l'utente sia il proprietario del commento
        if (!comment.getUser().getId().equals(credentials.getUser().getId())) {
            return "redirect:/product/" + comment.getProduct().getId();
        }
        
        model.addAttribute("comment", comment);
        return "editComment";
    }
    
    // Aggiorna commento
    @PostMapping("/comment/edit/{id}")
    public String updateComment(@PathVariable Long id, 
                               @Valid @ModelAttribute("comment") Comment comment,
                               BindingResult bindingResult,
                               Model model) {
        Comment existingComment = commentService.findById(id);
        Long productId = existingComment.getProduct().getId();
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Credentials credentials = (Credentials) auth.getPrincipal();
        
        // Verifica che l'utente sia il proprietario del commento
        if (!existingComment.getUser().getId().equals(credentials.getUser().getId())) {
            return "redirect:/product/" + productId;
        }
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("comment", existingComment);
            return "editComment";
        }
        
        commentService.update(id, comment.getText());
        return "redirect:/product/" + productId;
    }
    
    // Elimina commento
    @GetMapping("/comment/delete/{id}")
    public String deleteComment(@PathVariable Long id) {
        Comment comment = commentService.findById(id);
        Long productId = comment.getProduct().getId();
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Credentials credentials = (Credentials) auth.getPrincipal();
        
        // Verifica che l'utente sia il proprietario del commento
        if (comment.getUser().getId().equals(credentials.getUser().getId())) {
            commentService.deleteById(id);
        }
        
        return "redirect:/product/" + productId;
    }
}