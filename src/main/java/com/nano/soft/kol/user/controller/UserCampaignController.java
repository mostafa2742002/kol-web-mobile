package com.nano.soft.kol.user.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nano.soft.kol.bloger.entity.CampaignReq;
import com.nano.soft.kol.dto.ResponseDto;
import com.nano.soft.kol.user.service.UserCampaignService;

import jakarta.validation.constraints.NotNull;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@Validated
@RequestMapping(path = "/api", produces = { MediaType.APPLICATION_JSON_VALUE })
public class UserCampaignController {

    private final UserCampaignService userCampaignService;

    @PostMapping("/campaign/request/to-admin")
    public ResponseEntity<ResponseDto> requestCampaign(@RequestBody @NotNull CampaignReq campaignReq) {
        return ResponseEntity.ok(userCampaignService.requestCampaign(campaignReq));
    }

    @PostMapping("/campaign/request/to-bloger")
    public ResponseEntity<ResponseDto> requestCampaignToBloger(@RequestBody @NotNull CampaignReq campaignReq) {
        return ResponseEntity.ok(userCampaignService.requestCampaignToBloger(campaignReq));
    }
}
