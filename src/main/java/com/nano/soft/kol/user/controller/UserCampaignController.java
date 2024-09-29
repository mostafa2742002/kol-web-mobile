package com.nano.soft.kol.user.controller;

import java.util.ArrayList;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nano.soft.kol.bloger.entity.CampaignReq;
import com.nano.soft.kol.dto.ResponseDto;
import com.nano.soft.kol.user.service.UserCampaignService;

import jakarta.validation.constraints.NotNull;
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

    @GetMapping("/admin/campaign/to-bloger")
    public ResponseEntity<ArrayList<CampaignReq>> getAdminRequestedCampaign() {
        return ResponseEntity.ok(userCampaignService.getAdminRequestedCampaign());
    }

    @GetMapping("/user/requested-campaign")
    public ResponseEntity<ArrayList<CampaignReq>> getRequestedCampaign(@RequestParam @NotNull String userId) {
        return ResponseEntity.ok(userCampaignService.getRequestedCampaign(userId));
    }

    @GetMapping("/user/Accepted-campaign")
    public ResponseEntity<ArrayList<CampaignReq>> getAcceptedCampaign(@RequestParam @NotNull String userId) {
        return ResponseEntity.ok(userCampaignService.getAcceptedCampaign(userId));
    }

    @GetMapping("/user/rejected-campaign")
    public ResponseEntity<ArrayList<CampaignReq>> getRejectedCampaign(@RequestParam @NotNull String userId) {
        return ResponseEntity.ok(userCampaignService.getRejectedCampaign(userId));
    }

    @GetMapping("/user/done-campaign")
    public ResponseEntity<ArrayList<CampaignReq>> getDoneCampaign(@RequestParam @NotNull String userId) {
        return ResponseEntity.ok(userCampaignService.getDoneCampaign(userId));
    }

    @GetMapping("/user/live-campaign")
    public ResponseEntity<ArrayList<CampaignReq>> getLiveCampaign(@RequestParam @NotNull String userId) {
        return ResponseEntity.ok(userCampaignService.getLiveCampaign(userId));
    }
}
