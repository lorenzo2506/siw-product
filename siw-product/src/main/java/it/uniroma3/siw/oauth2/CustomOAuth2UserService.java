package it.uniroma3.siw.oauth2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.Role;
import it.uniroma3.siw.model.User;
import it.uniroma3.siw.service.CredentialsService;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private CredentialsService credentialsService;

    public CustomOAuth2UserService() {
        System.out.println("=== CustomOAuth2UserService CONSTRUCTOR CALLED ===");
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        System.out.println("=== CustomOAuth2UserService.loadUser() CALLED ===");
        
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String email = oAuth2User.getAttribute("email");
        
        System.out.println("Processing OAuth2 user with email: " + email);
        
        // Controlla se utente esiste già
        Credentials existingCredentials = credentialsService.getByEmail(email);
        if (existingCredentials != null) {
            System.out.println("User already exists with email: " + email);
            return new CustomOAuth2User(oAuth2User, existingCredentials, true);
        }
        
        // Nuovo utente - crea credenziali temporanee
        System.out.println("Creating new user with email: " + email);
        Credentials tempCredentials = createTempCredentials(oAuth2User, email);
        return new CustomOAuth2User(oAuth2User, tempCredentials, false);
    }
    
    private Credentials createTempCredentials(OAuth2User oAuth2User, String email) {
        // Crea nuovo User
        User newUser = new User();
        newUser.setName(oAuth2User.getAttribute("given_name"));
        newUser.setSurname(oAuth2User.getAttribute("family_name"));
        
        // Crea nuove Credentials
        Credentials credentials = new Credentials();
        credentials.setEmail(email);
        credentials.setRole(Role.USER);
        credentials.setUser(newUser);
        // password rimane null per OAuth2
        
        return credentials;
    }
}