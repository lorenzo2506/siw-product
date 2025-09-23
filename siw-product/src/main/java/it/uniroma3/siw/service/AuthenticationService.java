package it.uniroma3.siw.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.Role;
import it.uniroma3.siw.model.User;
import it.uniroma3.siw.oauth2.CustomOAuth2User;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class AuthenticationService {
    
    @Autowired 
    private CredentialsService credentialsService;
    
    // ========== METODI ESISTENTI (NON MODIFICARE) ==========
    
    public UserDetails getCurrentUserDetails() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            Object principal = auth.getPrincipal();
            
            if (principal instanceof Credentials credentials) {
                // Login tradizionale - funziona direttamente
                return credentials;
            } else if (principal instanceof CustomOAuth2User customUser) {
                // Login OAuth2 con il nostro wrapper
                return customUser.getCredentials();
            } else if (principal instanceof OAuth2User oAuth2User) {
                // Login OAuth2 senza wrapper - dovremmo cercare nel database
                String email = (String) oAuth2User.getAttributes().get("email");
                if (email != null) {
                    Credentials credentials = credentialsService.getByEmail(email);
                    if (credentials != null) {
                        return credentials;
                    } else {
                        System.out.println("No credentials found for OAuth2 user: " + email);
                    }
                }
            } else {
                System.out.println("Unknown principal type: " + principal.getClass());
            }
        }
        
        return null;
    }
    
    public User getCurrentUser() {
        UserDetails userDetails = getCurrentUserDetails();
        if (userDetails instanceof Credentials credentials) {
            return credentials.getUser();
        }
        return null;
    }
    
    public boolean isAdmin() {
        UserDetails userDetails = getCurrentUserDetails();
        if (userDetails instanceof Credentials credentials) {
            return credentials.getRole() == Role.ADMIN;
        }
        return false;
    }
    
    public boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
    }
    
    public String getCurrentUsername() {
        UserDetails userDetails = getCurrentUserDetails();
        if (userDetails instanceof Credentials credentials) {
            return credentials.getUsername();
        }
        return null;
    }
    
    public String getCurrentEmail() {
        UserDetails userDetails = getCurrentUserDetails();
        if (userDetails instanceof Credentials credentials) {
            return credentials.getEmail();
        }
        return null;
    }
    
    // ========== NUOVI METODI PER AUTO-LOGIN ==========
    
    /**
     * Effettua il login automatico per un utente appena registrato
     */
    public void loginUserAutomatically(Credentials credentials, HttpServletRequest request) {
        
        try {
            // Crea il token di autenticazione
            UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(
                    credentials,                    // Principal (le credenziali complete)
                    null,                          // Credentials (password non necessaria per auto-login)
                    credentials.getAuthorities()   // Authorities (ruoli)
                );
            
            // Aggiungi dettagli della richiesta web
            authToken.setDetails(new WebAuthenticationDetails(request));
            
            // Imposta l'autenticazione nel SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authToken);
            
            // Salva il SecurityContext nella sessione HTTP
            request.getSession().setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext()
            );
            
             
        } catch (Exception e) {
            System.err.println("Error during automatic login: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Ottiene le credenziali dell'utente corrente (se autenticato)
     */
    public Credentials getCurrentUserCredentials() {
        UserDetails userDetails = getCurrentUserDetails();
        if (userDetails instanceof Credentials credentials) {
            return credentials;
        }
        return null;
    }
}