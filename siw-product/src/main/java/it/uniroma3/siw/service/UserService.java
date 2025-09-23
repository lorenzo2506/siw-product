package it.uniroma3.siw.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.uniroma3.siw.model.Comment;
import it.uniroma3.siw.model.User;
import it.uniroma3.siw.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
	
	@Autowired private UserRepository userRepo;
	
	public User getUser(Long id) {
		
		return userRepo.findById(id).get();
	}
	
	
	
	
	public void saveUser(User user) {
		
		userRepo.save(user);
	}
	
	
	@Transactional
	public void addCommentToUser(Comment comment, User user) {
	    // Ricarica l'utente dal database per avere la sessione attiva
	    User managedUser = userRepo.findById(user.getId())
	        .orElseThrow(() -> new RuntimeException("User not found"));
	    
	    managedUser.getComments().add(comment);
	    this.saveUser(managedUser);
	}
	
	
	@Transactional
	public void removeCommentToUser(Comment comment, User user) {
	    // Ricarica l'utente dal database
	    User managedUser = userRepo.findById(user.getId())
	        .orElseThrow(() -> new RuntimeException("User not found"));
	    
	    managedUser.getComments().remove(comment);
	    this.saveUser(managedUser);
	}
	
	
	
	

}
