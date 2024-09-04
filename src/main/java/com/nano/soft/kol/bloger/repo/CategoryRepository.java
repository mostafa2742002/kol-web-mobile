package com.nano.soft.kol.bloger.repo;

import com.nano.soft.kol.bloger.entity.Category;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends MongoRepository<Category, String> {

    Category findByName(String name);
}
