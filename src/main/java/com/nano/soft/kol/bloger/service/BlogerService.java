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
import com.nano.soft.kol.bloger.dto.SearchDTO;
import com.nano.soft.kol.bloger.entity.Bloger;
import com.nano.soft.kol.bloger.entity.CampaignReq;
import com.nano.soft.kol.bloger.entity.Category;
import com.nano.soft.kol.bloger.entity.CategoryNumber;
import com.nano.soft.kol.bloger.entity.PageResponse;
import com.nano.soft.kol.bloger.repo.BlogerRepository;
import com.nano.soft.kol.bloger.repo.CategoryRepository;
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
    private final CategoryRepository categoryRepository;

    public String registerBloger(@Valid @NotNull BlogerDTO blogerDTO) throws MessagingException, InterruptedException {
        if (blogerRepository.findByEmail(blogerDTO.getEmail()) != null) {
            throw new IllegalArgumentException("Bloger already exists");
        }

        if (userRepository.findByEmail(blogerDTO.getEmail()) != null) {
            throw new IllegalArgumentException("You are already registered as a user");
        }

        blogerDTO.setEmail(blogerDTO.getEmail().toLowerCase());
        Bloger bloger = new Bloger(blogerDTO);

        List<Category> categories = categoryRepository.findAll();
        for (String interest : blogerDTO.getInterests()) {
            if (categories.stream().noneMatch(c -> c.getName().equals(interest))) {
                throw new IllegalArgumentException("Category not found");
            }
        }
        blogerDTO.getInterests().add("General");
        bloger.setInterests(blogerDTO.getInterests());

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
        HashMap<String, Integer> categoryMap = new HashMap<>();
        for (Bloger bloger : blogers) {
            for (String category : bloger.getInterests()) {
                categoryMap.put(category, categoryMap.getOrDefault(category, 0) + 1);
            }
        }
        for (String category : categoryMap.keySet()) {
            List<Category> categoriesList = categoryRepository.findAll();
            String image = categoriesList.stream().filter(c -> c.getName().equals(category)).findFirst().get()
                    .getImage();
            categories.add(new CategoryNumber(category, categoryMap.get(category), image));
        }

        return categories;
    }

    public PageResponse<Bloger> getBlogersByCategory(String category, int page, int size) {

        // Validate the category

        List<Category> categories = categoryRepository.findAll();
        if (categories.stream().noneMatch(c -> c.getName().equals(category))) {
            throw new IllegalArgumentException("Category not found");
        }

        // Get the category id
        String category_id = categories.stream().filter(c -> c.getName().equals(category)).findFirst().get().getId();

        // Fetch the paginated list of bloggers by category
        Page<Bloger> blogerPage = blogerRepository.findByInterests(category_id, PageRequest.of(page, size));

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

    public ResponseDto responseToClient(CampaignReq campaignReq) {
        if (!userRepository.findById(campaignReq.getClientId()).isPresent()) {
            throw new ResourceNotFoundException("User", "Id", campaignReq.getClientId());
        }
        campaignReq.setAdminApprovalBlogerResponse(true);
        User user = userRepository.findById(campaignReq.getClientId()).get();
        String campaignId = campaignReq.getId();
        boolean blogerResponse = campaignReq.getBlogerStatus().equals("Accepted");

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

    public ResponseDto responseToAdmin(String campaignId, Boolean blogerResponse, String content) {
        if (!campaignRepository.findById(campaignId).isPresent()) {
            throw new ResourceNotFoundException("Campaign", "Id", campaignId);
        }

        CampaignReq campaignReq = campaignRepository.findById(campaignId).get();
        campaignReq.setBlogerStatus((blogerResponse) ? "Accepted" : "Rejected");
        campaignReq.setContent(content);
        campaignRepository.save(campaignReq);

        Bloger bloger = blogerRepository.findById(campaignReq.getBlogerId()).get();
        // we will remove the campaign from the bloger's requested campaign
        bloger.getRequestedCampaign().remove(campaignId);
        blogerRepository.save(bloger);

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

        Bloger bloger = blogerRepository.findById(campaignReq.getBlogerId()).get();
        bloger.getDoneCampaign().add(campaignReq.getId());
        bloger.getPaidCampaign().remove(campaignReq.getId());
        blogerRepository.save(bloger);

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
            if (category != null && !bloger.getInterests().contains(category)) {
                continue;
            }
            if (country != null && !bloger.getCountryOfResidence().equals(country)) {
                continue;
            }
            if (type != null && !bloger.getGender().equals(type)) {
                continue;
            }
            if (age != null) {

                Integer lowerAge = age;
                Integer upperAge = age + 9;
                LocalDate birthDate = bloger.getDateOfBirth().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                LocalDate currentDate = LocalDate.now();
                int blogerAge = currentDate.getYear() - birthDate.getYear();
                if (blogerAge < lowerAge || blogerAge > upperAge) {
                    continue;
                }
            }

            filteredBlogers.add(bloger);
        }
        return filteredBlogers;
    }

    public ArrayList<CampaignReq> getRequestedCampaign(@NotNull String blogerId) {
        if (!blogerRepository.findById(blogerId).isPresent()) {
            throw new ResourceNotFoundException("Bloger Id", "Id", blogerId);
        }

        Bloger bloger = blogerRepository.findById(blogerId).get();
        ArrayList<CampaignReq> campaignReqs = new ArrayList<>();
        for (String campaignId : bloger.getRequestedCampaign()) {
            if (!campaignRepository.findById(campaignId).isPresent()) {
                continue;
            }
            campaignReqs.add(campaignRepository.findById(campaignId).get());
        }
        return campaignReqs;
    }

    public ArrayList<String> getPaidCampaign(@NotNull String blogerId) {
        if (!blogerRepository.findById(blogerId).isPresent()) {
            throw new ResourceNotFoundException("Bloger Id", "Id", blogerId);
        }

        Bloger bloger = blogerRepository.findById(blogerId).get();
        return bloger.getPaidCampaign();
    }

    public void rejectedToBloger(@Valid @NotNull CampaignReq campaignRejected) {
        if (!campaignRepository.findById(campaignRejected.getId()).isPresent()) {
            throw new ResourceNotFoundException("Campaign", "Id", campaignRejected.getId());
        }

        CampaignReq campaignReq = campaignRepository.findById(campaignRejected.getId()).get();
        campaignReq.setAdminApprovalBloger(false);
        campaignRepository.save(campaignReq);
    }

    public ArrayList<CampaignReq> getRejectedCampaign(@NotNull String blogerId) {
        if (!blogerRepository.findById(blogerId).isPresent()) {
            throw new ResourceNotFoundException("Bloger Id", "Id", blogerId);
        }

        Bloger bloger = blogerRepository.findById(blogerId).get();
        ArrayList<CampaignReq> rejectedCampaigns = new ArrayList<>();
        for (String campaignId : bloger.getRejectedCampaign()) {
            if (!campaignRepository.findById(campaignId).isPresent()) {
                throw new ResourceNotFoundException("Campaign", "Id", campaignId);
            }
            rejectedCampaigns.add(campaignRepository.findById(campaignId).get());
        }
        return rejectedCampaigns;
    }

    public ArrayList<CampaignReq> getCampaignsAdminResponse() {
        ArrayList<CampaignReq> campaigns = campaignRepository.findByAdminApprovalClient(true);
        ArrayList<CampaignReq> campaignsAdminResponse = new ArrayList<>();
        for (CampaignReq campaign : campaigns) {
            if (campaign.getBlogerStatus().equals("Accepted") || campaign.getBlogerStatus().equals("Rejected")) {
                if(campaign.getAdminApprovalBlogerResponse()) {
                campaignsAdminResponse.add(campaign);
                }
            }
        }

        return campaignsAdminResponse;
    }

    public CampaignReq addPaidCampaign(@NotNull String blogerId, @NotNull String campaignId) {
        if (!campaignRepository.findById(campaignId).isPresent()) {
            throw new ResourceNotFoundException("Campaign", "Id", campaignId);
        }

        if (!blogerRepository.findById(blogerId).isPresent()) {
            throw new ResourceNotFoundException("Bloger Id", "Id", blogerId);
        }

        CampaignReq campaignReq = campaignRepository.findById(campaignId).get();
        Bloger bloger = blogerRepository.findById(blogerId).get();
        bloger.getPaidCampaign().add(campaignId);
        blogerRepository.save(bloger);
        return campaignReq;
    }

    public SearchDTO searchBloger(String keyword) {
        ArrayList<Bloger> blogers = (ArrayList<Bloger>) blogerRepository.findAll();
        ArrayList<Bloger> filteredBlogers = new ArrayList<>();
        for (Bloger bloger : blogers) {
            if (bloger.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                    bloger.getFirst_name().toLowerCase().contains(keyword.toLowerCase()) ||
                    bloger.getLast_name().toLowerCase().contains(keyword.toLowerCase()) ||
                    bloger.getInstagramUrl().toLowerCase().contains(keyword.toLowerCase()) ||
                    bloger.getSnapchatUrl().toLowerCase().contains(keyword.toLowerCase()) ||
                    bloger.getTiktokUrl().toLowerCase().contains(keyword.toLowerCase()) ||
                    bloger.getYoutubeUrl().toLowerCase().contains(keyword.toLowerCase()))
                filteredBlogers.add(bloger);
        }

        ArrayList<Category> categories = (ArrayList<Category>) categoryRepository.findAll();
        ArrayList<Category> filteredCategories = new ArrayList<>();
        for (Category category : categories) {
            if (category.getName().toLowerCase().contains(keyword.toLowerCase())) {
                filteredCategories.add(category);
            }
        }

        SearchDTO searchDTO = new SearchDTO();
        searchDTO.setBlogers(filteredBlogers);
        searchDTO.setCategories(getCategoryNumber(filteredCategories));

        return searchDTO;
    }

    public ArrayList<CategoryNumber> getCategoryNumber(ArrayList<Category> categories) {
        ArrayList<CategoryNumber> categoryNumbers = getCategories();
        ArrayList<CategoryNumber> filteredCategoryNumbers = new ArrayList<>();

        // now we want only the categories that are in the categories list
        for (CategoryNumber categoryNumber : categoryNumbers) {
            for (Category category : categories) {
                if (categoryNumber.getName().equals(category.getName())) {
                    filteredCategoryNumbers.add(categoryNumber);
                }
            }

        }
        return filteredCategoryNumbers;
    }

}
