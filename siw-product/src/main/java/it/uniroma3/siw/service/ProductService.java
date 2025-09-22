package it.uniroma3.siw.service;

import it.uniroma3.siw.model.Product;
import it.uniroma3.siw.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    public List<Product> findAll() {
        return (List<Product>) productRepository.findAll();
    }
    
    public Product findById(Long id) {
        return productRepository.findById(id).orElse(null);
    }
    
    public List<Product> findByCategory(String category) {
        return productRepository.findByCategory(category);
    }
    
    public List<Product> findByNameContaining(String name) {
        return productRepository.findByNameContainingIgnoreCase(name);
    }
    
    @Transactional
    public Product save(Product product) {
        return productRepository.save(product);
    }
    
    @Transactional
    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }
    
    @Transactional
    public void addSimilarProduct(Long productId, Long similarProductId) {
        Product product = productRepository.findById(productId).orElse(null);
        Product similarProduct = productRepository.findById(similarProductId).orElse(null);
        
        if (product != null && similarProduct != null) {
            product.getSimilarProducts().add(similarProduct);
            productRepository.save(product);
        }
    }
    
    @Transactional
    public void removeSimilarProduct(Long productId, Long similarProductId) {
        Product product = productRepository.findById(productId).orElse(null);
        Product similarProduct = productRepository.findById(similarProductId).orElse(null);
        
        if (product != null && similarProduct != null) {
            product.getSimilarProducts().remove(similarProduct);
            productRepository.save(product);
        }
    }
    
    public List<Product> findProductsNotInSimilar(Long productId) {
        Product product = productRepository.findById(productId).orElse(null);
        List<Product> allProducts = (List<Product>) productRepository.findAll();
        
        if (product != null) {
            return allProducts.stream()
                .filter(p -> !p.getId().equals(productId) && 
                           !product.getSimilarProducts().contains(p))
                .collect(Collectors.toList());
        }
        return allProducts;
    }
    
    public List<String> findAllCategories() {
        return productRepository.findDistinctCategories();
    }
}