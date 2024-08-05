package com.nano.soft.kol.user.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.nano.soft.kol.bloger.entity.CampaignReq;

@Repository
public interface CampaignRepository extends MongoRepository<CampaignReq, String> {

}
