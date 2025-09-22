package it.uniroma3.siw.service;

import it.uniroma3.siw.model.Comment;
import it.uniroma3.siw.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentService {
    
    @Autowired
    private CommentRepository commentRepository;
    
    public List<Comment> findAll() {
        return (List<Comment>) commentRepository.findAll();
    }
    
    public Comment findById(Long id) {
        return commentRepository.findById(id).orElse(null);
    }
    
    public List<Comment> findByProductId(Long productId) {
        return commentRepository.findByProductId(productId);
    }
    
    public List<Comment> findByUserId(Long userId) {
        return commentRepository.findByUserId(userId);
    }
    
    @Transactional
    public Comment save(Comment comment) {
        return commentRepository.save(comment);
    }
    
    @Transactional
    public Comment update(Long id, String newText) {
        Comment comment = commentRepository.findById(id).orElse(null);
        if (comment != null) {
            comment.setText(newText);
            return commentRepository.save(comment);
        }
        return null;
    }
    
    @Transactional
    public void deleteById(Long id) {
        commentRepository.deleteById(id);
    }
}