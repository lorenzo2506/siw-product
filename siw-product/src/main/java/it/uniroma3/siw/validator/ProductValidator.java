package it.uniroma3.siw.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import it.uniroma3.siw.model.Product;
import it.uniroma3.siw.service.ProductService;




@Component
public class ProductValidator implements Validator{
	
	@Autowired private ProductService productService;
	
	@Override
	public void validate(Object o, Errors errors) {
		
		Product product= (Product) o;
		
		if(product.getName()==null || product.getCategory()==null || this.productService.existsByNameAndCategory(product.getName(), product.getCategory()) ) {
			
			errors.reject("product.duplicate");
		}
	}
	

	@Override
	public boolean supports(Class<?> aClass) {
		return Product.class.equals(aClass);
	}
	

}
