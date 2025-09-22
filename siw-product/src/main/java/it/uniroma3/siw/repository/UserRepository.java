package it.uniroma3.siw.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import it.uniroma3.siw.model.User;

public interface UserRepository extends JpaRepository<User, Long>{

	
}
