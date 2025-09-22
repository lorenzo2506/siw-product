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
        
        System.out.println("=== STEP 1 DEBUG ===");
        System.out.println("Email: " + credentials.getEmail());
        System.out.println("Password length: " + (credentials.getPassword() != null ? credentials.getPassword().length() : "null"));
        System.out.println("User object: " + credentials.getUser());
        if (credentials.getUser() != null) {
            System.out.println("User name: " + credentials.getUser().getName());
            System.out.println("User surname: " + credentials.getUser().getSurname());
        }
        
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
        
        System.out.println("Original password: " + originalPassword);
        System.out.println("Encoded password: " + credentials.getPassword());
        System.out.println("Role: " + credentials.getRole());
        
        return "redirect:/register/step2";
    }

    // Step 2: Mostra form per username (comune per registrazione normale e OAuth2)
    @GetMapping("/register/step2")
    public String showUsernameForm(@ModelAttribute("credentials") Credentials credentials, Model model) {
        
        System.out.println("=== STEP 2 GET DEBUG ===");
        System.out.println("Credentials from session: " + credentials);
        
        // Controlla se abbiamo le credenziali dalla sessione
        if (credentials == null || credentials.getEmail() == null) {
            System.out.println("Redirecting to register - missing credentials or email");
            return "redirect:/register";
        }
        
        // Verifica che l'oggetto User sia presente
        if (credentials.getUser() == null) {
            System.out.println("Redirecting to register - missing user object");
            return "redirect:/register";
        }
        
        System.out.println("Email from session: " + credentials.getEmail());
        System.out.println("User from session: " + credentials.getUser());
        if (credentials.getUser() != null) {
            System.out.println("User name from session: " + credentials.getUser().getName());
            System.out.println("User surname from session: " + credentials.getUser().getSurname());
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
        
        System.out.println("=== STEP 2 POST DEBUG - START ===");
        System.out.println("Credentials from session: " + credentials);
        
        // ========== VALIDAZIONI INIZIALI ==========
        
        // Verifica che abbiamo tutti i dati necessari dalla sessione
        if (credentials == null || credentials.getEmail() == null || credentials.getUser() == null) {
            System.out.println("ERROR: Missing required data from session - redirecting to register");
            return "redirect:/register";
        }
        
        // Determina il tipo di registrazione
        boolean isOAuth2Registration = credentials.getPassword() == null;
        
        System.out.println("=== REGISTRATION TYPE ANALYSIS ===");
        System.out.println("Email: " + credentials.getEmail());
        System.out.println("Username from form: " + credentials.getUsername());
        System.out.println("Is OAuth2: " + isOAuth2Registration);
        System.out.println("Credentials ID: " + credentials.getId());
        System.out.println("User ID: " + (credentials.getUser() != null ? credentials.getUser().getId() : "null"));
        System.out.println("Password present: " + (credentials.getPassword() != null));
        System.out.println("Role: " + credentials.getRole());
        
        // ========== CONTROLLO CRITICO PER OAUTH2 ==========
        
        if (isOAuth2Registration) {
            System.out.println("=== OAUTH2 DUPLICATE CHECK ===");
            
            // Verifica se esiste già un utente con questa email nel database
            Credentials existingByEmail = credentialsService.getByEmail(credentials.getEmail());
            
            if (existingByEmail != null) {
                System.out.println("CRITICAL ERROR: OAuth2 user " + credentials.getEmail() + " already exists in database!");
                System.out.println("Existing credentials ID: " + existingByEmail.getId());
                System.out.println("Existing user ID: " + (existingByEmail.getUser() != null ? existingByEmail.getUser().getId() : "null"));
                System.out.println("This should NOT happen - OAuth2 flow error!");
                
                // Pulisci la sessione
                sessionStatus.setComplete();
                
                // Auto-login con le credenziali esistenti e redirect
                authenticationService.loginUserAutomatically(existingByEmail, request);
                System.out.println("Auto-logged in existing OAuth2 user and redirecting to home");
                return "redirect:/?oauth_existing=true";
            }
            
            // Verifica che le credenziali dalla sessione non abbiano già un ID
            if (credentials.getId() != null) {
                System.out.println("WARNING: OAuth2 credentials from session already have ID: " + credentials.getId());
                System.out.println("This might indicate a session persistence issue");
            }
        }
        
        // ========== VALIDAZIONI USERNAME ==========
        
        // Controlla username vuoto o null
        if (credentials.getUsername() == null || credentials.getUsername().trim().isEmpty()) {
            System.out.println("ERROR: Username is null or empty");
            model.addAttribute("usernameExists", "Username non può essere vuoto");
            model.addAttribute("isOAuth2", isOAuth2Registration);
            return "register-step2";
        }
        
        // Controlla se username esiste già
        if (credentialsService.existsByUsername(credentials.getUsername())) {
            System.out.println("ERROR: Username already exists: " + credentials.getUsername());
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
        
        System.out.println("=== FINAL DATA BEFORE SAVE ===");
        System.out.println("Final email: " + credentials.getEmail());
        System.out.println("Final username: " + credentials.getUsername());
        System.out.println("Final role: " + credentials.getRole());
        System.out.println("Final credentials ID: " + credentials.getId());
        System.out.println("Final user ID: " + (credentials.getUser() != null ? credentials.getUser().getId() : "null"));
        System.out.println("Final user name: " + (credentials.getUser() != null ? credentials.getUser().getName() : "null"));
        System.out.println("Final user surname: " + (credentials.getUser() != null ? credentials.getUser().getSurname() : "null"));
        
        // ========== SALVATAGGIO ==========
        
        try {
            System.out.println("=== ATTEMPTING TO SAVE ===");
            
            // Salva le credenziali
            Credentials savedCredentials = credentialsService.saveCredentials(credentials);
            
            System.out.println("=== SAVE SUCCESSFUL ===");
            System.out.println("Saved credentials ID: " + savedCredentials.getId());
            System.out.println("Saved user ID: " + (savedCredentials.getUser() != null ? savedCredentials.getUser().getId() : "null"));
            
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
            
            System.out.println("=== PERFORMING AUTO-LOGIN ===");
            authenticationService.loginUserAutomatically(savedCredentials, request);
            
            // Pulisci la sessione
            sessionStatus.setComplete();
            
            // Redirect appropriato
            String redirectUrl = isOAuth2Registration ? "/?oauth_registered=true" : "/?registered=true";
            System.out.println("SUCCESS: Redirecting to: " + redirectUrl);
            
            return "redirect:" + redirectUrl;
            
        } catch (Exception e) {
            System.out.println("=== SAVE ERROR ===");
            System.out.println("ERROR saving credentials: " + e.getMessage());
            e.printStackTrace();
            
            // In caso di errore, controlla se è un problema di duplicati
            if (e.getMessage() != null && e.getMessage().contains("constraint") || 
                e.getMessage() != null && e.getMessage().contains("duplicate")) {
                
                System.out.println("Detected constraint violation - possible duplicate");
                
                if (isOAuth2Registration) {
                    // Per OAuth2, verifica se nel frattempo è stato creato un utente
                    Credentials existingByEmail = credentialsService.getByEmail(credentials.getEmail());
                    if (existingByEmail != null) {
                        System.out.println("Found existing user created during save attempt - auto-login");
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