package it.uniroma3.siw.controller;

import it.uniroma3.siw.model.Comment;
import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.Product;
import it.uniroma3.siw.service.AuthenticationService;
import it.uniroma3.siw.service.CommentService;
import it.uniroma3.siw.service.ProductService;
import it.uniroma3.siw.service.UserService;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class CommentController {
    
    @Autowired private CommentService commentService;
    @Autowired private ProductService productService;
    @Autowired private UserService userService;
    @Autowired private AuthenticationService authenticationService;
    
    // Aggiungi commento
    @PostMapping("/product/{productId}/comment/add")
    public String addComment(@PathVariable Long productId, 
                            @Valid @ModelAttribute("comment") Comment comment,
                            BindingResult bindingResult,
                            Model model) {
    	
    	Product product = this.productService.findById(productId);
        if (bindingResult.hasErrors()) {
            model.addAttribute("product", product);
            return "product";
        }
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Credentials credentials = (Credentials) auth.getPrincipal();
        
        comment.setProduct(product);
        comment.setUser(credentials.getUser());
        comment.setCreatedAt(LocalDateTime.now() );
        commentService.save(comment);
        this.userService.addCommentToUser(comment, credentials.getUser());
        this.productService.addCommentToProduct(comment, product);
        
        
        return "redirect:/product/" + productId;
    }
    
    
    // Form per modificare commento
    @GetMapping("product/{productId}/comment/{commentId}/edit")
    public String showEditCommentForm(@PathVariable("commentId") Long commentId, Model model) {
    	
    	Comment comment = commentService.findById(commentId);
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Credentials credentials = (Credentials) auth.getPrincipal();
        
        if (!comment.getUser().getId().equals(credentials.getUser().getId())) {
            return "redirect:/product/" + comment.getProduct().getId();
        }
        
        model.addAttribute("comment", comment);
        return "editComment";
    }
    
    
    
    @PostMapping("/product/{productId}/comment/{commentId}/edit")
    public String edit(@PathVariable("commentId") Long commentId, 
                               @Valid @ModelAttribute("formComment") Comment formComment,
                               BindingResult bindingResult,
                               Model model) {
    	
        Comment existingComment = commentService.findById(commentId);
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
        
        commentService.update(commentId, formComment.getText());
        return "redirect:/product/" + productId;
    }
    
    // Elimina commento
    @PostMapping("product/{productId}/comment/{commentId}/delete")
    public String deleteComment(@PathVariable("commentId") Long commentId) {
    	
        Comment comment = commentService.findById(commentId);
        Long productId = comment.getProduct().getId();
        Product product = this.productService.findById(productId);
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Credentials credentials = (Credentials) auth.getPrincipal();
        
        // Verifica che l'utente sia il proprietario del commento
        if (comment.getUser().getId().equals(credentials.getUser().getId())) {
        	
        	this.productService.deleteCommentToProduct(comment, product);
        	this.userService.removeCommentToUser(comment, credentials.getUser());
            commentService.deleteById(commentId);
        }
        
        return "redirect:/product/" + productId;
    }
    
    
    @PostMapping("admin/product/{productId}/comment/{commentId}/delete")
    public String adminDeleteComment(@PathVariable("commentId") Long commentId) {
    	
        Comment comment = commentService.findById(commentId);
        Long productId = comment.getProduct().getId();
        
        
        if(authenticationService.isAdmin())
        	return "redirect:/product/" + productId;
        
        this.productService.deleteCommentToProduct(comment, comment.getProduct());
    	this.userService.removeCommentToUser(comment, comment.getUser());
        commentService.deleteById(commentId);

        
        return "redirect:/product/" + productId;
    }
    
    
    @GetMapping("/product/{productId}/comments")
    public String showAllComments(@PathVariable("productId") Long productId,Model model) {
    	
    	Product product = this.productService.findById(productId);
    	model.addAttribute("product", product);
    	model.addAttribute("comment", new Comment());
    	return "comments";
    }
    
    
    
}