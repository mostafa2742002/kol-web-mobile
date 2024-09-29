package com.nano.soft.kol.user.service;

import java.util.ArrayList;
import java.util.Optional;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

import com.nano.soft.kol.bloger.entity.Bloger;
import com.nano.soft.kol.bloger.entity.CampaignReq;
import com.nano.soft.kol.bloger.repo.BlogerRepository;
import com.nano.soft.kol.dto.ResponseDto;
import com.nano.soft.kol.exception.ResourceNotFoundException;
import com.nano.soft.kol.user.entity.User;
import com.nano.soft.kol.user.repo.CampaignRepository;
import com.nano.soft.kol.user.repo.UserRepository;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserCampaignService {

    private final CampaignRepository campaignRepository;
    private final BlogerRepository blogerRepository;
    private final UserRepository userRepository;

    public ResponseDto requestCampaign(@NotNull CampaignReq campaignReq) {

        if (!userRepository.findById(campaignReq.getClientId()).isPresent()) {
            throw new ResourceNotFoundException("User Id", "Id", campaignReq.getClientId());
        }

        if (!blogerRepository.findById(campaignReq.getBlogerId()).isPresent()) {
            throw new ResourceNotFoundException("Bloger Id", "Id", campaignReq.getBlogerId());
        }

        Bloger bloger = blogerRepository.findById(campaignReq.getBlogerId()).get();
        campaignReq.setBlogerName(bloger.getName());
        campaignReq.setBlogerImage(bloger.getImage());

        campaignRepository.save(campaignReq);

        Optional<User> user = userRepository.findById(campaignReq.getClientId());
        if (!user.isPresent()) {
            throw new ResourceNotFoundException("User Id", "Id", campaignReq.getClientId());
        }

        user.get().getRequestedCampaign().add(campaignReq.getId());
        userRepository.save(user.get());

        return new ResponseDto("201", "Campaign request sent successfully");
    }

    public ResponseDto requestCampaignToBloger(@NotNull CampaignReq campaignReq) {
        if (!blogerRepository.findById(campaignReq.getBlogerId()).isPresent()) {
            throw new ResourceNotFoundException("Bloger Id", "Id", campaignReq.getBlogerId());
        }
        if (!campaignReq.getAdminApprovalClient()) {

            if (userRepository.findById(campaignReq.getClientId()).isPresent()) {
                throw new ResourceNotFoundException("User Id", "Id", campaignReq.getClientId());
            }
            User user = userRepository.findById(campaignReq.getClientId()).get();
            user.getRejectedCampaign().add(campaignReq.getId());
            user.getRequestedCampaign().remove(campaignReq.getId());
            userRepository.save(user);
            return new ResponseDto("201", "Campaign request sent successfully");
        }

        if (!blogerRepository.findById(campaignReq.getBlogerId()).isPresent()) {
            throw new ResourceNotFoundException("Bloger Id", "Id", campaignReq.getBlogerId());
        }

        Bloger bloger = blogerRepository.findById(campaignReq.getBlogerId()).get();

        bloger.getRequestedCampaign().add(campaignReq.getId());
        blogerRepository.save(bloger);
        CampaignReq campaign = campaignRepository.findById(campaignReq.getId()).get();
        campaign.setAdminApprovalClient(true);

        campaignRepository.save(campaign);
        return new ResponseDto("201", "Campaign request sent successfully");
    }

    public ArrayList<CampaignReq> getRequestedCampaign(@NotNull String userId) {
        if (!userRepository.findById(userId).isPresent()) {
            throw new ResourceNotFoundException("User Id", "Id", userId);
        }
        User user = userRepository.findById(userId).get();
        ArrayList<CampaignReq> requestedCampaign = new ArrayList<>();
        for (String campaignId : user.getRequestedCampaign()) {
            if (campaignRepository.findById(campaignId).isPresent())
                requestedCampaign.add(campaignRepository.findById(campaignId).get());
        }
        return requestedCampaign;
    }

    public ArrayList<CampaignReq> getAcceptedCampaign(@NotNull String userId) {
        if (!userRepository.findById(userId).isPresent()) {
            throw new ResourceNotFoundException("User Id", "Id", userId);
        }
        User user = userRepository.findById(userId).get();
        ArrayList<CampaignReq> acceptedCampaign = new ArrayList<>();
        for (String campaignId : user.getAcceptedCampaign()) {
            if (campaignRepository.findById(campaignId).isPresent())
                acceptedCampaign.add(campaignRepository.findById(campaignId).get());
        }
        return acceptedCampaign;
    }

    public ArrayList<CampaignReq> getRejectedCampaign(@NotNull String userId) {
        if (!userRepository.findById(userId).isPresent()) {
            throw new ResourceNotFoundException("User Id", "Id", userId);
        }
        User user = userRepository.findById(userId).get();

        ArrayList<CampaignReq> rejectedCampaign = new ArrayList<>();
        for (String campaignId : user.getRejectedCampaign()) {
            if (campaignRepository.findById(campaignId).isPresent())
                rejectedCampaign.add(campaignRepository.findById(campaignId).get());
        }
        return rejectedCampaign;
    }

    public ArrayList<CampaignReq> getDoneCampaign(@NotNull String userId) {
        if (!userRepository.findById(userId).isPresent()) {
            throw new ResourceNotFoundException("User Id", "Id", userId);
        }
        User user = userRepository.findById(userId).get();

        ArrayList<CampaignReq> doneCampaign = new ArrayList<>();
        for (String campaignId : user.getDoneCampaign()) {
            if (campaignRepository.findById(campaignId).isPresent())
                doneCampaign.add(campaignRepository.findById(campaignId).get());
        }

        return doneCampaign;
    }

    public ArrayList<CampaignReq> getAdminRequestedCampaign() {
        // we will get all campaign that the adminapprovalclient is false
        return campaignRepository.findByAdminApprovalClient(false);
    }

    public ArrayList<CampaignReq> getLiveCampaign(@NotNull String userId) {
        if (!userRepository.findById(userId).isPresent()) {
            throw new ResourceNotFoundException("User Id", "Id", userId);
        }
        User user = userRepository.findById(userId).get();
        ArrayList<CampaignReq> liveCampaigns = new ArrayList<>();
        for (String campaignId : user.getLiveCampaign()) {

            if (!campaignRepository.findById(campaignId).isPresent()) {
                continue;
            }
            CampaignReq campaign = campaignRepository.findById(campaignId).get();
            // this is the (to) date in the database : 2024-10-03T06:46:00.000Z. it saved as
            // a string
            String campaignDate = campaign.getTo();
            // this is the current date
            String currentDate = LocalDate.now().toString();
            // if the campaign date is before the current date we will delete it from live
            // campaign to done campaign
            if (campaignDate.compareTo(currentDate) < 0) {
                user.getLiveCampaign().remove(campaignId);
                user.getDoneCampaign().add(campaignId);
                userRepository.save(user);
                continue;
            }

            liveCampaigns.add(campaignRepository.findById(campaignId).get());
        }
        return liveCampaigns;
    }
}
