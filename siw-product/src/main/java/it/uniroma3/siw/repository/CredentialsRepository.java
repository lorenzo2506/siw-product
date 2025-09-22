package it.uniroma3.siw.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.User;

public interface CredentialsRepository extends JpaRepository<Credentials, Long>{

	 	Optional<Credentials> findByUsername(String username);
	    
	    Optional<Credentials> findByEmail(String email);
	    
	    @Query("SELECT c FROM Credentials c WHERE c.username = :value OR c.email = :value")
	    Optional<Credentials> findByUsernameOrEmail(@Param("value") String value);
	    
	    boolean existsByUsername(String username);
	    
	    boolean existsByEmail(String email);
	    
	    Optional<Credentials> findByUser(User user);
	
}
