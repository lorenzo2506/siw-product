package it.uniroma3.siw.repository;

import it.uniroma3.siw.model.Product;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends CrudRepository<Product, Long> {
    
    public List<Product> findByCategory(String category);
    
    public List<Product> findByNameContainingIgnoreCase(String name);
    
    
    
    @Query("SELECT DISTINCT p.category FROM Product p")
    public List<String> findDistinctCategories();
    
    @Query("SELECT sp FROM Product p JOIN p.similarProducts sp WHERE p.id = :productId")
    public List<Product> findAllSimilarProductsByProductId(@Param("productId") Long productId);
    
    public boolean existsByNameAndCategory(String name, String category);
    
    public Product findByNameAndCategory(String name, String category);
    
    @Query("SELECT p FROM Product p WHERE "
    		+ "(LOWER(p.name) LIKE LOWER(CONCAT(:query, '%'))) "
    		+ "AND (:maxPrice IS NULL OR p.price < :maxPrice)"
    		+ " AND (:minPrice IS NULL OR p.price > :minPrice)")
    public List<Product> findBySearchQuery(@Param("query") String query, @Param("maxPrice") Double maxPrice, @Param("minPrice") Double minPrice);
}