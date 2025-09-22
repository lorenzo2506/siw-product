package it.uniroma3.siw.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;


import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.service.AuthenticationService;





@Component
public class AdminValidator implements Validator{
	
	@Autowired private AuthenticationService authenticationService;
	
	@Override
	public void validate(Object o, Errors errors) {
		
		Credentials credentials = (Credentials) o;
		
		if(credentials==null || !(authenticationService.isAdmin()))
			errors.reject("admin.notAdmin");
	}
	

	@Override
	public boolean supports(Class<?> aClass) {
		return Credentials.class.equals(aClass);
	}
	

}
