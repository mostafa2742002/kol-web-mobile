package com.nano.soft.kol.user.service;

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
        
        campaignRepository.save(campaignReq);
        return new ResponseDto("201", "Campaign request sent successfully");
    }

    public ResponseDto requestCampaignToBloger(@NotNull CampaignReq campaignReq) {
        campaignReq.setAdminApprovalClient(true);
        if(!blogerRepository.findById(campaignReq.getBlogerId()).isPresent()) {
            throw new  ResourceNotFoundException("Bloger Id", "Id", campaignReq.getBlogerId());
        }
        Bloger bloger = blogerRepository.findById(campaignReq.getBlogerId()).get();
        bloger.getRequestedCampaign().add(campaignReq.getId());
        blogerRepository.save(bloger);

        if(userRepository.findById(campaignReq.getClientId()).isPresent()) {
            throw new  ResourceNotFoundException("User Id", "Id", campaignReq.getClientId());
        }
        User user = userRepository.findById(campaignReq.getClientId()).get();
        user.getRequestedCampaign().add(campaignReq.getId());
        userRepository.save(user);

        campaignRepository.save(campaignReq);
        return new ResponseDto("201", "Campaign request sent successfully");
    }

}
