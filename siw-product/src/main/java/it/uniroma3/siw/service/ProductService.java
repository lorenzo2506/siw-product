package it.uniroma3.siw.service;

import it.uniroma3.siw.model.Comment;
import it.uniroma3.siw.model.Product;
import it.uniroma3.siw.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
        return productRepository.findByCategory(category.toLowerCase().trim());
    }
    
    public List<Product> findAllSimilarProducts(Long id) {
    	return productRepository.findAllSimilarProductsByProductId(id);
    }
    
    public boolean existsByNameAndCategory(String name, String category) {
    	return productRepository.existsByNameAndCategory(name.toLowerCase().trim(), category.toLowerCase().trim());
    }
    
    public List<Product> findByNameContaining(String name) {
        return productRepository.findByNameContainingIgnoreCase(name);
    }
    
    @Transactional
    public Product save(Product product) {
        // Normalizza la categoria in minuscolo
    	product.setName( product.getName().toLowerCase().trim() );
        product.setCategory( product.getCategory().toLowerCase().trim() );
        return productRepository.save(product);
    }
    
    @Transactional
    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }
    
    public void delete(Long id) {
    	
    	Product product = this.findById(id);
    	List<Product> similar = this.findAllSimilarProducts(id);
    	for(Product p: similar) {
    		p.getSimilarProducts().remove(product);
    		this.save(p);
    	}
    	
    	this.deleteById(id);
    }
    
    @Transactional
    public void addSimilarProduct(Long productId, Long similarProductId) {
        Product product = productRepository.findById(productId).orElse(null);
        Product similarProduct = productRepository.findById(similarProductId).orElse(null);
        
        if (product != null && similarProduct != null) {
            similarProduct.getSimilarProducts().add(product);
            productRepository.save(product);
            productRepository.save(similarProduct);

        }
    }
    
    @Transactional
    public void removeSimilarProduct(Long productId, Long similarProductId) {
        Product product = productRepository.findById(productId).orElse(null);
        Product similarProduct = productRepository.findById(similarProductId).orElse(null);
        
        if (product != null && similarProduct != null) {
            product.getSimilarProducts().remove(similarProduct);
            similarProduct.getSimilarProducts().remove(product);
            productRepository.save(product);
            productRepository.save(similarProduct);
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
    
    
    
    public void edit(Product product, Product formProduct) {
    	
    	if(product==null || formProduct==null)
    		throw new IllegalArgumentException("errore");
    	
    	product.setName( formProduct.getName() );
    	product.setPrice( formProduct.getPrice() );
    	product.setDescription( formProduct.getDescription());
    	product.setCategory( formProduct.getCategory() );
    	product.setImagePath( formProduct.getImagePath() );
    	this.save(product);
    	
    }
    
    
    public Product findByNameAndCategory(String name, String category) {
    	return this.productRepository.findByNameAndCategory(name.toLowerCase().trim(), category.toLowerCase().trim());
    }
    
    
    @Transactional
    public void addCommentToProduct(Comment comment, Product product) {
    	
    	product.getComments().add(comment);
    	this.save(product);
    }
    
    @Transactional
    public void deleteCommentToProduct(Comment comment, Product product) {
    	
    	product.getComments().remove(comment);
    	this.save(product);
    }
    
    @Transactional
    public void updateSimilarProducts(Long productId, List<Long> newSimilarIds) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return;
        
        // Rimuovi tutti i vecchi collegamenti
        List<Product> oldSimilars = new ArrayList<>(product.getSimilarProducts());
        for (Product old : oldSimilars) {
            old.getSimilarProducts().remove(product);
            product.getSimilarProducts().remove(old);
        }
        
        // Aggiungi i nuovi
        if (newSimilarIds != null) {
            for (Long similarId : newSimilarIds) {
                Product similar = productRepository.findById(similarId).orElse(null);
                if (similar != null) {
                    product.getSimilarProducts().add(similar);
                    similar.getSimilarProducts().add(product);
                }
            }
        }
        
        productRepository.save(product);
    }
    
    
    
    
    public List<Product> searchProducts(String query, Double minPrice, Double maxPrice) {
        List<Product> products;
        
        if(query==null || query.isEmpty())
        	return this.findAll();
        
        return this.productRepository.findBySearchQuery(query, maxPrice, minPrice);
    }
}