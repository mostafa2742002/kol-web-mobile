package com.nano.soft.kol.bloger.entity;

import org.springframework.data.mongodb.core.mapping.Document;

import com.nano.soft.kol.user.entity.AuditableBase;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import nonapi.io.github.classgraph.json.Id;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "campaigns")
public class CampaignReq extends AuditableBase {

    @Id
    @Schema(hidden = true)
    private String id;

    private String campaignDescription;

    private String campaignType;

    private String from;

    private String to;

    private String blogerStatus = "pending";

    private String clientStatus = "pending";

    private String blogerId;

    private String clientId;

    private Boolean adminApprovalClient = false;

    private String content;

    private String campaignUrl;

    private Boolean adminApprovalBloger = false;
}
