package com.nano.soft.kol.bloger.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.nano.soft.kol.bloger.entity.Bloger;
import com.nano.soft.kol.bloger.entity.Category;

import java.util.*;

@Repository
public interface BlogerRepository extends MongoRepository<Bloger, String> {
    Bloger findByEmail(String email);

    Bloger findById(int id);

    Page<Bloger> findByInterests(Category interest, Pageable pageable);

    Optional<Bloger> findByName(String name);

    ArrayList<Bloger> findByInterests(Category categoryEnum);

}
