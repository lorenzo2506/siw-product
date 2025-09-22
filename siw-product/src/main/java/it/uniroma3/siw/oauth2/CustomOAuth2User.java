// =================== 1. CUSTOM USER (UNICO) ===================
package it.uniroma3.siw.oauth2;

import java.util.Collection;
import java.util.Map;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import it.uniroma3.siw.model.Credentials;

public class CustomOAuth2User implements OAuth2User {
    
    private final OAuth2User oAuth2User;
    private final Credentials credentials;
    private final boolean isExistingUser;
    
    public CustomOAuth2User(OAuth2User oAuth2User, Credentials credentials, boolean isExistingUser) {
        this.oAuth2User = oAuth2User;
        this.credentials = credentials;
        this.isExistingUser = isExistingUser;
    }
    
    @Override
    public Map<String, Object> getAttributes() {
        return oAuth2User.getAttributes();
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return credentials.getAuthorities();
    }
    
    @Override
    public String getName() {
        return credentials.getEmail();
    }
    
    public Credentials getCredentials() { 
        return credentials; 
    }
    
    public boolean isExistingUser() { 
        return isExistingUser; 
    }
    
    public String getEmail() { 
        return credentials.getEmail(); 
    }
}