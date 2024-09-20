package com.nano.soft.kol.user.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.nano.soft.kol.user.entity.RequestToBloger;

@Repository
public interface RequestToBlogerRepository extends MongoRepository<RequestToBloger, String> {

}
