package com.nano.soft.kol.user.entity;

import org.springframework.data.mongodb.core.mapping.Document;

import com.nano.soft.kol.bloger.entity.CampaignReq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nonapi.io.github.classgraph.json.Id;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "request_to_bloger")
public class RequestToBloger {

    @Id
    private String id;

    private CampaignReq campaignReq;
}
