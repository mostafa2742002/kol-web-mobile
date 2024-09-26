package com.nano.soft.kol.user.repo;

import java.util.ArrayList;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.nano.soft.kol.bloger.entity.CampaignReq;

@Repository
public interface CampaignRepository extends MongoRepository<CampaignReq, String> {

    ArrayList<CampaignReq> findByAdminApprovalClient(boolean b);

    ArrayList<CampaignReq> findByDoneFromBloger(boolean b);

}
