package it.uniroma3.siw.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.uniroma3.siw.model.User;
import it.uniroma3.siw.repository.UserRepository;

@Service
public class UserService {
	
	@Autowired private UserRepository userRepo;
	
	public User getUser(Long id) {
		
		return userRepo.findById(id).get();
	}
	
	
	
	public void saveUser(User user) {
		
		userRepo.save(user);
	}
	
	
	
	

}
