package com.nano.soft.kol.bloger.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.nano.soft.kol.bloger.dto.BlogerDTO;
import com.nano.soft.kol.bloger.entity.Bloger;
import com.nano.soft.kol.bloger.entity.CampaignReq;
import com.nano.soft.kol.bloger.entity.Category;
import com.nano.soft.kol.bloger.entity.CategoryNumber;
import com.nano.soft.kol.bloger.entity.PageResponse;
import com.nano.soft.kol.bloger.repo.BlogerRepository;
import com.nano.soft.kol.dto.ResponseDto;
import com.nano.soft.kol.email.EmailService;
import com.nano.soft.kol.exception.ResourceNotFoundException;
import com.nano.soft.kol.jwt.JwtService;
import com.nano.soft.kol.user.entity.User;
import com.nano.soft.kol.user.repo.CampaignRepository;
import com.nano.soft.kol.user.repo.UserRepository;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class BlogerService {

    private final JwtService jwtService;
    private final BlogerRepository blogerRepository;
    private final EmailService emailService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final CampaignRepository campaignRepository;
    private final UserRepository userRepository;

    public String registerBloger(@Valid @NotNull BlogerDTO blogerDTO) throws MessagingException, InterruptedException {
        if (blogerRepository.findByEmail(blogerDTO.getEmail()) != null) {
            throw new IllegalArgumentException("Bloger already exists");
        }

        blogerDTO.setEmail(blogerDTO.getEmail().toLowerCase());
        Bloger bloger = new Bloger(blogerDTO);
        ArrayList<Category> category = new ArrayList<>();
        for (int i = 0; i < blogerDTO.getInterests().size(); i++) {
            String name = blogerDTO.getInterests().get(i).toLowerCase();
            // make the first letter of the category name capital
            name = name.substring(0, 1).toUpperCase() + name.substring(1);
            category.add(Category.valueOf(name));
        }
        bloger.setInterests(category);

        String verificationToken = jwtService.generateToken(bloger);

        bloger.setEmailVerified(false);
        bloger.setVerificationToken(verificationToken);
        bloger.setPassword(bCryptPasswordEncoder.encode(bloger.getPassword()));

        Bloger savedBloger = blogerRepository.save(bloger);

        blogerRepository.save(savedBloger);

        String subject = "Verify Your Email";

        // if we use render site then use this
        // String body = "Click the link to verify your
        // email:https://courses-website-q0gf.onrender.com/api/verifyemail?token="
        // + verificationToken;

        // if we use localhost then use this
        String body = "Click the link to verify your email:http://localhost:8080/api/verifyemail?token="
                + verificationToken;
        emailService.sendEmail(savedBloger.getEmail(), subject, body);

        return "the Bloger added successfully go to your email to verify your email";
    }

    public Bloger getBloger(String id) {
        // validate then get the bloger
        if (blogerRepository.findById(id).isPresent()) {
            return blogerRepository.findById(id).get();
        } else {
            throw new IllegalArgumentException("Bloger not found");
        }
    }

    public PageResponse<Bloger> getAllBlogers(int page, int size) {
        Page<Bloger> blogerPage = blogerRepository.findAll(PageRequest.of(page, size));

        PageResponse<Bloger> response = new PageResponse<>();
        response.setContent(blogerPage.getContent());
        response.setNumber(blogerPage.getNumber());
        response.setSize(blogerPage.getSize());
        response.setTotalElements(blogerPage.getTotalElements());
        response.setTotalPages(blogerPage.getTotalPages());
        response.setFirst(blogerPage.isFirst());
        response.setLast(blogerPage.isLast());

        return response;
    }

    public ArrayList<CategoryNumber> getCategories() {
        ArrayList<CategoryNumber> categories = new ArrayList<>();
        ArrayList<Bloger> blogers = (ArrayList<Bloger>) blogerRepository.findAll();
        HashMap<Category, Integer> categoryMap = new HashMap<>();
        for (Bloger bloger : blogers) {
            for (Category category : bloger.getInterests()) {
                categoryMap.put(category, categoryMap.getOrDefault(category, 0) + 1);
            }
        }
        return categories;
    }

    public PageResponse<Bloger> getBlogersByCategory(String category, int page, int size) {
        // Convert the category string to the Category enum
        Category categoryEnum;
        try {
            categoryEnum = Category.valueOf(category);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid category: " + category);
        }

        // Fetch the paginated list of bloggers by category
        Page<Bloger> blogerPage = blogerRepository.findByInterests(categoryEnum, PageRequest.of(page, size));

        // Map the Page<Bloger> to PageResponse<Bloger>
        PageResponse<Bloger> response = new PageResponse<>();
        response.setContent(blogerPage.getContent());
        response.setNumber(blogerPage.getNumber());
        response.setSize(blogerPage.getSize());
        response.setTotalElements(blogerPage.getTotalElements());
        response.setTotalPages(blogerPage.getTotalPages());
        response.setFirst(blogerPage.isFirst());
        response.setLast(blogerPage.isLast());

        return response;
    }

    public ResponseDto responseToClient(String campaignId, Boolean blogerResponse, String content) {
        if (!campaignRepository.findById(campaignId).isPresent()) {
            throw new ResourceNotFoundException("Campaign", "Id", campaignId);
        }
        CampaignReq campaignReq = campaignRepository.findById(campaignId).get();
        campaignReq.setBlogerStatus((blogerResponse) ? "Accepted" : "Rejected");
        campaignReq.setContent(content);

        if (!userRepository.findById(campaignReq.getClientId()).isPresent()) {
            throw new ResourceNotFoundException("User", "Id", campaignReq.getClientId());
        }

        User user = userRepository.findById(campaignReq.getClientId()).get();
        // delete the campaign from the user requested campaigns
        user.getRequestedCampaign().remove(campaignId);
        // add the campaign to the user accepted or rejected campaigns
        if (blogerResponse) {
            user.getAcceptedCampaign().add(campaignId);
        } else {
            user.getRejectedCampaign().add(campaignId);
        }

        userRepository.save(user);
        campaignRepository.save(campaignReq);

        return new ResponseDto("200", "Response sent successfully");
    }

    public ResponseDto completeToAdmin(@Valid @NotNull CampaignReq campaignComplete) {
        if (campaignComplete.getCampaignUrl() == null) {
            throw new IllegalArgumentException("Campaign URL is required");
        }
        if (!campaignRepository.findById(campaignComplete.getId()).isPresent()) {
            throw new ResourceNotFoundException("Campaign", "Id", campaignComplete.getId());
        }

        CampaignReq campaignReq = campaignRepository.findById(campaignComplete.getId()).get();
        campaignRepository.save(campaignReq);

        return new ResponseDto("200", "Campaign sent to admin successfully");
    }

    public ResponseDto completeToClient(@Valid @NotNull CampaignReq campaignComplete) {
        if (campaignComplete.getCampaignUrl() == null) {
            throw new IllegalArgumentException("Content is required");
        }
        if (!campaignRepository.findById(campaignComplete.getId()).isPresent()) {
            throw new ResourceNotFoundException("Campaign", "Id", campaignComplete.getId());
        }

        CampaignReq campaignReq = campaignRepository.findById(campaignComplete.getId()).get();
        campaignReq.setAdminApprovalBloger(true);
        campaignRepository.save(campaignReq);

        User user = userRepository.findById(campaignReq.getClientId()).get();
        user.getDoneCampaign().add(campaignReq.getId());
        user.getAcceptedCampaign().remove(campaignReq.getId());
        userRepository.save(user);

        return new ResponseDto("200", "Campaign sent to client successfully");
    }

    public Bloger getProfileBloger(String email) {
        if (blogerRepository.findByEmail(email) == null) {
            throw new ResourceNotFoundException("the bloger", "Email", email);
        }
        return blogerRepository.findByEmail(email);
    }

    public List<Bloger> getBlogerByFilter(String category, String country, String type, Integer age) {
        List<Bloger> blogers = blogerRepository.findAll();
        List<Bloger> filteredBlogers = new ArrayList<>();
        for (Bloger bloger : blogers) {
            if (category != null && !bloger.getInterests().contains(Category.valueOf(category))) {
                continue;
            }
            if (country != null && !bloger.getCountryOfResidence().equals(country)) {
                continue;
            }
            if (type != null && !bloger.getGender().equals(type)) {
                continue;
            }
            Integer lowerAge = age;
            Integer upperAge = age+9;
            LocalDate birthDate = bloger.getDateOfBirth().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate currentDate = LocalDate.now();
            int blogerAge = currentDate.getYear() - birthDate.getYear();
            if (age != null && (blogerAge < lowerAge || blogerAge > upperAge)) {
                continue;
            }

            filteredBlogers.add(bloger);
        }
        return filteredBlogers;
    }

}
