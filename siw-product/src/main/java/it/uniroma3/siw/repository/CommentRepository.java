package it.uniroma3.siw.repository;

import it.uniroma3.siw.model.Comment;
import it.uniroma3.siw.model.Product;
import it.uniroma3.siw.model.User;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CommentRepository extends CrudRepository<Comment, Long> {
    
    List<Comment> findByProductId(Long productId);
    
    List<Comment> findByUserId(Long userId);
    
    public boolean existsByProductAndUser(Product product, User user);
    
    public Comment findByProductAndUser(Product product, User user);
    
    public List<Comment> findAllByProductAndUserNot(Product product, User user);
}