package com.nano.soft.kol.bloger.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nano.soft.kol.bloger.entity.Category;
import com.nano.soft.kol.bloger.repo.BlogerRepository;
import com.nano.soft.kol.bloger.repo.CategoryRepository;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private BlogerRepository blogerRepository;

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Optional<Category> getCategoryById(String id) {
        return categoryRepository.findById(id);
    }

    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }

    public Category updateCategory(String id, Category category) {
        return categoryRepository.findById(id)
                .map(existingCategory -> {
                    existingCategory.setName(category.getName());
                    return categoryRepository.save(existingCategory);
                })
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }

    public void deleteCategory(String id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        blogerRepository.findAll().forEach(bloger -> {
            bloger.getInterests().remove(category.getId());
            blogerRepository.save(bloger);
        });

        categoryRepository.deleteById(id);
    }
}
