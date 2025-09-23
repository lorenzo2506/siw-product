package it.uniroma3.siw.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.Role;
import it.uniroma3.siw.model.User;
import it.uniroma3.siw.service.AuthenticationService;
import it.uniroma3.siw.service.CredentialsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@SessionAttributes("credentials")
public class AuthenticationController {

    @Autowired
    private CredentialsService credentialsService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    // 🔥 NUOVO: Servizio per auto-login
    @Autowired
    private AuthenticationService authenticationService;

    // Step 1: Mostra form per email e password
    @GetMapping("/register")
    public String showEmailPasswordForm(Model model) {
        Credentials credentials = new Credentials();
        credentials.setUser(new User());
        model.addAttribute("credentials", credentials);
        return "register-step1";
    }

    // Step 1: Processa email e password
    @PostMapping("/register/step1")
    public String processEmailPassword(@ModelAttribute("credentials") Credentials credentials,
                                     Model model, HttpSession session) {
        
        
        
        // Controlla se l'email esiste già
        if (credentialsService.existsByEmail(credentials.getEmail())) {
            model.addAttribute("emailExists", "Email già registrata");
            return "register-step1";
        }
        
        // IMPORTANTE: Assicurati che l'oggetto User sia inizializzato
        if (credentials.getUser() == null) {
            credentials.setUser(new User());
        }
        
        // Cripta la password e imposta il ruolo
        String originalPassword = credentials.getPassword();
        credentials.setPassword(passwordEncoder.encode(credentials.getPassword()));
        credentials.setRole(Role.USER);
        
       
        
        return "redirect:/register/step2";
    }

    // Step 2: Mostra form per username (comune per registrazione normale e OAuth2)
    @GetMapping("/register/step2")
    public String showUsernameForm(@ModelAttribute("credentials") Credentials credentials, Model model) {
        
        
        // Controlla se abbiamo le credenziali dalla sessione
        if (credentials == null || credentials.getEmail() == null) {
            return "redirect:/register";
        }
        
        // Verifica che l'oggetto User sia presente
        if (credentials.getUser() == null) {
            return "redirect:/register";
        }
        
       
        
        // Determina se è una registrazione OAuth2 o normale
        boolean isOAuth2Registration = credentials.getPassword() == null;
        model.addAttribute("isOAuth2", isOAuth2Registration);
        
        if (isOAuth2Registration) {
            model.addAttribute("pageTitle", "Completa la registrazione");
            model.addAttribute("welcomeMessage", "Benvenuto " + credentials.getUser().getName() + "!");
            model.addAttribute("instructions", "Per completare la registrazione con Google, scegli un username:");
        } else {
            model.addAttribute("pageTitle", "Scegli Username");
            model.addAttribute("instructions", "Scegli un username per completare la registrazione:");
        }
        
        return "register-step2";
    }

    // Step 2: Processa username e completa registrazione (comune per entrambi i flussi)
    @PostMapping("/register/step2")
    public String processUsername(@ModelAttribute("credentials") Credentials credentials,
                                Model model, SessionStatus sessionStatus,
                                RedirectAttributes redirectAttributes,
                                HttpServletRequest request) {
        
        
        // ========== VALIDAZIONI INIZIALI ==========
        
        // Verifica che abbiamo tutti i dati necessari dalla sessione
        if (credentials == null || credentials.getEmail() == null || credentials.getUser() == null) {
            return "redirect:/register";
        }
        
        // Determina il tipo di registrazione
        boolean isOAuth2Registration = credentials.getPassword() == null;
        
        // ========== CONTROLLO CRITICO PER OAUTH2 ==========
        
        if (isOAuth2Registration) {
            System.out.println("=== OAUTH2 DUPLICATE CHECK ===");
            
            // Verifica se esiste già un utente con questa email nel database
            Credentials existingByEmail = credentialsService.getByEmail(credentials.getEmail());
            
            if (existingByEmail != null) {
                
                // Pulisci la sessione
                sessionStatus.setComplete();
                
                // Auto-login con le credenziali esistenti e redirect
                authenticationService.loginUserAutomatically(existingByEmail, request);
                return "redirect:/?oauth_existing=true";
            }
            
            // Verifica che le credenziali dalla sessione non abbiano già un ID
            
        }
        
        // ========== VALIDAZIONI USERNAME ==========
        
        // Controlla username vuoto o null
        if (credentials.getUsername() == null || credentials.getUsername().trim().isEmpty()) {
            model.addAttribute("usernameExists", "Username non può essere vuoto");
            model.addAttribute("isOAuth2", isOAuth2Registration);
            return "register-step2";
        }
        
        // Controlla se username esiste già
        if (credentialsService.existsByUsername(credentials.getUsername())) {
            model.addAttribute("usernameExists", "Username già in uso");
            model.addAttribute("isOAuth2", isOAuth2Registration);
            return "register-step2";
        }
        
        // ========== PREPARAZIONE PER SALVATAGGIO ==========
        
        // Per OAuth2, assicurati che password sia null e ruolo sia corretto
        if (isOAuth2Registration) {
            credentials.setPassword(null); // Mantieni password null per OAuth2
            credentials.setRole(Role.USER);
            
            // IMPORTANTE: Assicurati che l'oggetto User non abbia ID (deve essere nuovo)
            if (credentials.getUser().getId() != null) {
                System.out.println("WARNING: OAuth2 User object has ID " + credentials.getUser().getId() + " - creating new User");
                // Crea un nuovo oggetto User per evitare conflitti
                User newUser = new User();
                newUser.setName(credentials.getUser().getName());
                newUser.setSurname(credentials.getUser().getSurname());
                credentials.setUser(newUser);
            }
            
            // Assicurati che anche le credenziali non abbiano ID
            credentials.setId(null);
        }
        
        
        // ========== SALVATAGGIO ==========
        
        try {
            
            // Salva le credenziali
            Credentials savedCredentials = credentialsService.saveCredentials(credentials);
            
            
            // Verifica che sia stato salvato correttamente
            Credentials retrievedCredentials = credentialsService.getByUsername(credentials.getUsername());
            if (retrievedCredentials != null) {
                System.out.println("VERIFICATION: User successfully found by username after save");
                System.out.println("Retrieved email: " + retrievedCredentials.getEmail());
                System.out.println("Retrieved role: " + retrievedCredentials.getRole());
                System.out.println("Retrieved user ID: " + (retrievedCredentials.getUser() != null ? retrievedCredentials.getUser().getId() : "null"));
            } else {
                System.out.println("CRITICAL ERROR: User NOT found by username after save!");
            }
            
            // ========== AUTO-LOGIN ==========
            
            authenticationService.loginUserAutomatically(savedCredentials, request);
            
            // Pulisci la sessione
            sessionStatus.setComplete();
            
            // Redirect appropriato
            String redirectUrl = isOAuth2Registration ? "/?oauth_registered=true" : "/?registered=true";
            
            return "redirect:" + redirectUrl;
            
        } catch (Exception e) {
             e.printStackTrace();
            
            // In caso di errore, controlla se è un problema di duplicati
            if (e.getMessage() != null && e.getMessage().contains("constraint") || 
                e.getMessage() != null && e.getMessage().contains("duplicate")) {
                
                
                if (isOAuth2Registration) {
                    // Per OAuth2, verifica se nel frattempo è stato creato un utente
                    Credentials existingByEmail = credentialsService.getByEmail(credentials.getEmail());
                    if (existingByEmail != null) {
                        sessionStatus.setComplete();
                        authenticationService.loginUserAutomatically(existingByEmail, request);
                        return "redirect:/?oauth_recovered=true";
                    }
                }
            }
            
            // Errore generico
            model.addAttribute("errorMessage", "Errore durante il salvataggio: " + e.getMessage());
            model.addAttribute("isOAuth2", isOAuth2Registration);
            return "register-step2";
        }
    }
    
    

    // Metodo per annullare la registrazione
    @GetMapping("/register/cancel")
    public String cancelRegistration(SessionStatus sessionStatus) {
        sessionStatus.setComplete();
        return "redirect:/";
    }

    // Login page
    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "error", required = false) String error,
                               @RequestParam(value = "logout", required = false) String logout,
                               Model model) {
        
        if (error != null) {
            if ("oauth".equals(error)) {
                model.addAttribute("errorMessage", "Errore durante l'autenticazione con Google");
            } else {
                model.addAttribute("errorMessage", "Username o password non validi");
            }
            System.out.println("Login error occurred: " + error);
        }
        
        if (logout != null) {
            model.addAttribute("logoutMessage", "Sei stato disconnesso con successo");
        }
        
        return "login";
    }
}