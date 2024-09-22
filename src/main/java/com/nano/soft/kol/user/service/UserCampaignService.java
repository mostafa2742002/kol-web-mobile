package com.nano.soft.kol.user.service;

import java.util.ArrayList;
import java.util.Optional;

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
        System.out.println(2);
        if (!campaignReq.getAdminApprovalClient()) {
            System.out.println(1);
            if (userRepository.findById(campaignReq.getClientId()).isPresent()) {
                throw new ResourceNotFoundException("User Id", "Id", campaignReq.getClientId());
            }
            User user = userRepository.findById(campaignReq.getClientId()).get();
            user.getRejectedCampaign().add(campaignReq.getId());
            userRepository.save(user);
            return new ResponseDto("201", "Campaign request sent successfully");
        }

        if (!blogerRepository.findById(campaignReq.getBlogerId()).isPresent()) {
            throw new ResourceNotFoundException("Bloger Id", "Id", campaignReq.getBlogerId());
        }

        Bloger bloger = blogerRepository.findById(campaignReq.getBlogerId()).get();
        System.out.println(campaignReq.getBlogerId());
        System.out.println(campaignReq.getId());
        
        bloger.getRequestedCampaign().add(campaignReq.getId());
        blogerRepository.save(bloger);
        if (!userRepository.findById(campaignReq.getClientId()).isPresent()) {
            throw new ResourceNotFoundException("User Id", "Id", campaignReq.getClientId());
        }


        User user = userRepository.findById(campaignReq.getClientId()).get();
        user.getRequestedCampaign().add(campaignReq.getId());
        userRepository.save(user);

        campaignRepository.save(campaignReq);
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

}
