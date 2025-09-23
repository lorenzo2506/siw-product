package it.uniroma3.siw.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.Objects;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Data
@Entity
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @NotBlank
    private String name;
    
    @NotNull
    @Positive
    private Double price;
    
    @NotBlank
    @Column(length = 1000)
    private String description;
    
    @NotBlank
    private String category; // tipologia: libri, articoli per giardino, etc.
    
    @ManyToMany
    @JoinTable(
        name = "similar_products",
        joinColumns = @JoinColumn(name = "product_id"),
        inverseJoinColumns = @JoinColumn(name = "similar_product_id")
    )
    private List<Product> similarProducts = new ArrayList<>();
    
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();
    
    
    private String imagePath;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(name, product.name) && 
               Objects.equals(category, product.category);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, category);
    }
}