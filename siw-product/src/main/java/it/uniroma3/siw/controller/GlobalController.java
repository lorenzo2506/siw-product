package it.uniroma3.siw.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import it.uniroma3.siw.model.User;
import it.uniroma3.siw.service.AuthenticationService;

@ControllerAdvice
public class GlobalController {

    @Autowired private AuthenticationService authenticationService;
    
    @ModelAttribute("userDetails")
    public UserDetails getUserDetails() {
        return authenticationService.getCurrentUserDetails();
    }

    @ModelAttribute("currentUser")
    public User getCurrentUser() {
        return authenticationService.getCurrentUser();
    }

    @ModelAttribute("isAuthenticated")
    public boolean isAuthenticated() {
        return authenticationService.getCurrentUser() != null;
    }

    @ModelAttribute("isAdmin")
    public boolean isAdmin() {
        return authenticationService.isAdmin();
    }

    @ModelAttribute("isUser")
    public boolean isUser() {
        return !authenticationService.isAdmin() && authenticationService.getCurrentUser() != null;
    }
}