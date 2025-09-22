package it.uniroma3.siw.repository;

import it.uniroma3.siw.model.Product;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends CrudRepository<Product, Long> {
    
    List<Product> findByCategory(String category);
    
    List<Product> findByNameContainingIgnoreCase(String name);
    
    
    
    @Query("SELECT DISTINCT p.category FROM Product p")
    List<String> findDistinctCategories();
    
    @Query("SELECT sp FROM Product p JOIN p.similarProducts sp WHERE p.id = :productId")
    List<Product> findAllSimilarProductsByProductId(@Param("productId") Long productId);
    
    public boolean existsByNameAndCategory(String name, String category);
    
    public Product findByNameAndCategory(String name, String category);
}