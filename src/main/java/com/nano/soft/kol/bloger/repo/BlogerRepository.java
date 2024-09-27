package com.nano.soft.kol.bloger.repo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.nano.soft.kol.bloger.entity.Bloger;
import com.nano.soft.kol.bloger.entity.Category;

@Repository
public interface BlogerRepository extends MongoRepository<Bloger, String> {
    Bloger findByEmail(String email);

    Bloger findById(int id);

    Page<Bloger> findByInterests(String interest_id, Pageable pageable);

    Optional<Bloger> findByName(String name);

    ArrayList<Bloger> findByInterests(Category categoryEnum);

}
