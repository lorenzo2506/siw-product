package it.uniroma3.siw.repository;

import it.uniroma3.siw.model.Comment;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CommentRepository extends CrudRepository<Comment, Long> {
    
    List<Comment> findByProductId(Long productId);
    
    List<Comment> findByUserId(Long userId);
}