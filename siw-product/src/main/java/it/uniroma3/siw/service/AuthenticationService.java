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
        
        System.out.println("=== GET CURRENT USER DETAILS ===");
        System.out.println("Authentication: " + auth);
        System.out.println("Is authenticated: " + (auth != null ? auth.isAuthenticated() : "null"));
        System.out.println("Principal type: " + (auth != null && auth.getPrincipal() != null ? auth.getPrincipal().getClass() : "null"));
        
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            Object principal = auth.getPrincipal();
            
            if (principal instanceof Credentials credentials) {
                // Login tradizionale - funziona direttamente
                System.out.println("Traditional login - returning Credentials directly");
                return credentials;
            } else if (principal instanceof CustomOAuth2User customUser) {
                // Login OAuth2 con il nostro wrapper
                System.out.println("OAuth2 login with CustomOAuth2User wrapper");
                return customUser.getCredentials();
            } else if (principal instanceof OAuth2User oAuth2User) {
                // Login OAuth2 senza wrapper - dovremmo cercare nel database
                System.out.println("OAuth2 login without wrapper - looking up in database");
                String email = (String) oAuth2User.getAttributes().get("email");
                if (email != null) {
                    Credentials credentials = credentialsService.getByEmail(email);
                    if (credentials != null) {
                        System.out.println("Found credentials for OAuth2 user: " + email);
                        return credentials;
                    } else {
                        System.out.println("No credentials found for OAuth2 user: " + email);
                    }
                }
            } else {
                System.out.println("Unknown principal type: " + principal.getClass());
            }
        }
        
        System.out.println("Returning null - no valid authentication found");
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
        System.out.println("=== AUTOMATIC LOGIN ===");
        System.out.println("Logging in user: " + credentials.getUsername());
        System.out.println("Email: " + credentials.getEmail());
        System.out.println("Role: " + credentials.getRole());
        
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
            
            System.out.println("User successfully logged in automatically!");
            System.out.println("Authentication: " + SecurityContextHolder.getContext().getAuthentication());
            
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