package it.uniroma3.siw.oauth2;

import java.io.IOException;
import java.util.Collection;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        
        System.out.println("=== OAuth2 Authentication Success ===");
        System.out.println("Principal type: " + authentication.getPrincipal().getClass());
        
        if (!(authentication.getPrincipal() instanceof CustomOAuth2User)) {
            System.out.println("Unexpected principal type - redirecting to error");
            response.sendRedirect("/login?error=oauth");
            return;
        }
        
        CustomOAuth2User customUser = (CustomOAuth2User) authentication.getPrincipal();
        
        System.out.println("User email: " + customUser.getEmail());
        System.out.println("Is existing user: " + customUser.isExistingUser());
        
        if (customUser.isExistingUser()) {
            // Controlla il ruolo per decidere dove reindirizzare
            Collection<? extends GrantedAuthority> authorities = customUser.getAuthorities();
            
            for (GrantedAuthority authority : authorities) {
                if ("ROLE_ADMIN".equals(authority.getAuthority())) {
                    System.out.println("OAuth2 Admin user - redirecting to admin panel");
                    response.sendRedirect("/admin");
                    return;
                }
            }
            
            System.out.println("OAuth2 Regular user - redirecting to home");
            response.sendRedirect("/");
        } else {
            System.out.println("New user, redirecting to step 2");
            request.getSession().setAttribute("credentials", customUser.getCredentials());
            response.sendRedirect("/register/step2");
        }
    }
}
