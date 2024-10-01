package com.nano.soft.kol.bloger.service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

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
import com.nano.soft.kol.bloger.entity.Wallet;
import com.nano.soft.kol.bloger.repo.BlogerRepository;
import com.nano.soft.kol.bloger.repo.CategoryRepository;
import com.nano.soft.kol.constants.BlogerCache;
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
    private final BlogerCache blogerCache;

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

        // Handle null return from repositories
        ArrayList<Bloger> blogers = (ArrayList<Bloger>) Optional.ofNullable(blogerRepository.findAll())
                .orElseGet(ArrayList::new);
        List<Category> categoriesList = Optional.ofNullable(categoryRepository.findAll())
                .orElseGet(ArrayList::new);

        // Map to store category counts
        HashMap<String, Integer> categoryMap = new HashMap<>();

        // Iterate over blogers' interests and check against the categoriesList
        for (Bloger bloger : blogers) {
            List<String> interests = new ArrayList<>(bloger.getInterests()); // Copy to avoid modifying while iterating
            for (String category : interests) {
                boolean categoryExists = categoriesList.stream()
                        .anyMatch(c -> c.getName().equals(category));

                // If category is not found in categoriesList, remove from bloger interests
                if (!categoryExists && !category.equals("General")) {
                    bloger.getInterests().remove(category); // Remove interest from Bloger
                    blogerRepository.save(bloger); // Update bloger in DB
                    continue;
                }
                // Add category to the map
                categoryMap.put(category, categoryMap.getOrDefault(category, 0) + 1);
            }
        }

        // Add CategoryNumber objects based on the category map
        for (String category : categoryMap.keySet()) {
            // Find the category in categoriesList and handle the Optional properly
            Optional<Category> matchedCategoryOpt = categoriesList.stream()
                    .filter(c -> c.getName().equals(category))
                    .findFirst();

            if (matchedCategoryOpt.isPresent()) {
                String image = matchedCategoryOpt.get().getImage();
                categories.add(new CategoryNumber(category, categoryMap.get(category), image));
            } else {
                // Log or handle the case where the category isn't found, although this case
                // should be rare
                System.out.println("Category not found for: " + category);
            }
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
        CampaignReq campaign = campaignRepository.findById(campaignId).get();
        campaign.setAdminApprovalBlogerResponse(true);

        campaignRepository.save(campaign);
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

        campaignReq.setCampaignUrl(campaignComplete.getCampaignUrl());
        campaignReq.setDoneFromBloger(true);
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
        campaignReq.setDoneFromBloger(true);
        campaignReq.setClientStatus("Done");
        campaignReq.setBlogerStatus("Done");
        campaignRepository.save(campaignReq);

        User user = userRepository.findById(campaignReq.getClientId()).get();
        user.getLiveCampaign().add(campaignReq.getId());
        user.getAcceptedCampaign().remove(campaignReq.getId());
        userRepository.save(user);

        Bloger bloger = blogerRepository.findById(campaignReq.getBlogerId()).get();
        bloger.getLiveCampaign();
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

    public List<Bloger> getBlogerByFilter(String category, String country, String type, Integer age,
            Integer priceFrom, Integer priceTo) {
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

            if (priceFrom != null && priceTo != null) {
                if (bloger.getPrice() < priceFrom || bloger.getPrice() > priceTo) {
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

    public ArrayList<CampaignReq> getPaidCampaign(@NotNull String blogerId) {
        if (!blogerRepository.findById(blogerId).isPresent()) {
            throw new ResourceNotFoundException("Bloger Id", "Id", blogerId);
        }

        Bloger bloger = blogerRepository.findById(blogerId).get();
        ArrayList<CampaignReq> paidCampaigns = new ArrayList<>();
        for (String campaignId : bloger.getPaidCampaign()) {
            if (!campaignRepository.findById(campaignId).isPresent()) {
                continue;
            }
            paidCampaigns.add(campaignRepository.findById(campaignId).get());
        }
        return paidCampaigns;

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
                if (campaign.getAdminApprovalBlogerResponse()) {
                    continue;
                }
                campaignsAdminResponse.add(campaign);
            }
        }

        return campaignsAdminResponse;
    }

    public CampaignReq addPaidCampaign(@NotNull String campaignId) {
        if (!campaignRepository.findById(campaignId).isPresent()) {
            throw new ResourceNotFoundException("Campaign", "Id", campaignId);
        }

        CampaignReq campaignReq = campaignRepository.findById(campaignId).get();
        campaignReq.setClientStatus("Paid");
        if (!blogerRepository.findById(campaignReq.getBlogerId()).isPresent()) {
            throw new ResourceNotFoundException("Bloger Id", "Id", campaignReq.getBlogerId());
        }

        Bloger bloger = blogerRepository.findById(campaignReq.getBlogerId()).get();
        bloger.getPaidCampaign().add(campaignId);
        bloger.getWallet().add(campaignId);
        blogerRepository.save(bloger);

        User user = userRepository.findById(campaignReq.getClientId()).get();
        user.getAcceptedCampaign().remove(campaignId);
        userRepository.save(user);

        campaignRepository.save(campaignReq);
        return campaignReq;
    }

    public SearchDTO searchBloger(String keyword) {
        System.out.println(1);
        ArrayList<Bloger> blogers = (ArrayList<Bloger>) Optional.ofNullable(blogerRepository.findAll())
                .orElseGet(ArrayList::new);
        if (keyword == null || keyword.isEmpty()) {
            SearchDTO searchDTO = new SearchDTO();
            searchDTO.setBlogers(new ArrayList<>());
            searchDTO.setCategories(new ArrayList<>());
            return searchDTO;
        }
        ArrayList<Bloger> filteredBlogers = new ArrayList<>();
        System.out.println(2);
        for (Bloger bloger : blogers) {
            if (bloger.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                    bloger.getFirst_name().toLowerCase().contains(keyword.toLowerCase()) ||
                    bloger.getLast_name().toLowerCase().contains(keyword.toLowerCase()) ||
                    // i want the search by url to be exactly match
                    bloger.getInstagramUrl().toLowerCase().equals(keyword.toLowerCase()) ||
                    bloger.getSnapchatUrl().toLowerCase().equals(keyword.toLowerCase()) ||
                    bloger.getTiktokUrl().toLowerCase().equals(keyword.toLowerCase()) ||
                    bloger.getYoutubeUrl().toLowerCase().equals(keyword.toLowerCase())) {
                filteredBlogers.add(bloger);
            }
        }
        System.out.println(3);
        ArrayList<Category> categories = (ArrayList<Category>) Optional.ofNullable(categoryRepository.findAll())
                .orElseGet(ArrayList::new);
        ArrayList<Category> filteredCategories = new ArrayList<>();
        for (Category category : categories) {
            if (category.getName().toLowerCase().contains(keyword.toLowerCase())) {
                filteredCategories.add(category);
            }
        }
        System.out.println(4);
        SearchDTO searchDTO = new SearchDTO();
        searchDTO.setBlogers(filteredBlogers);
        searchDTO.setCategories(getCategoryNumber(filteredCategories));

        return searchDTO;
    }

    public ArrayList<CategoryNumber> getCategoryNumber(ArrayList<Category> categories) {
        System.out.println(5);
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
        System.out.println(6);
        return filteredCategoryNumbers;
    }

    public ArrayList<CampaignReq> getCampaignsAdminComplete() {
        return campaignRepository.findByDoneFromBloger(true);
    }

    public List<String> getCountries() {
        // we will get the countries from the blogers (countryOfResidence) and we will
        // remove the duplicates
        List<Bloger> blogers = blogerRepository.findAll();
        List<String> countries = new ArrayList<>();
        for (Bloger bloger : blogers) {
            if (!countries.contains(bloger.getCountryOfResidence())) {
                countries.add(bloger.getCountryOfResidence());
            }
        }
        return countries;
    }

    public List<Integer> getMinMaxAge() {
        List<Integer> minMaxAge = new ArrayList<>();
        minMaxAge.add(blogerCache.getMinAge());
        minMaxAge.add(blogerCache.getMaxAge());
        return minMaxAge;
    }

    public List<Integer> getMinMaxPrice() {
        List<Integer> minMaxCalary = new ArrayList<>();
        minMaxCalary.add(blogerCache.getMinSalary());
        minMaxCalary.add(blogerCache.getMaxSalary());
        return minMaxCalary;
    }

    public ArrayList<CampaignReq> getLiveCampaign(@NotNull String blogerId) {
        if (!blogerRepository.findById(blogerId).isPresent()) {
            throw new ResourceNotFoundException("Bloger Id", "Id", blogerId);
        }
        // we will get the (TO) Date From the campaign if this date is before the
        // current we will delete it from live campaign to done campaign
        Bloger bloger = blogerRepository.findById(blogerId).get();
        ArrayList<CampaignReq> liveCampaigns = new ArrayList<>();
        for (String campaignId : bloger.getLiveCampaign()) {

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
                bloger.getLiveCampaign().remove(campaignId);
                bloger.getDoneCampaign().add(campaignId);
                blogerRepository.save(bloger);
                continue;
            }

            liveCampaigns.add(campaignRepository.findById(campaignId).get());
        }
        return liveCampaigns;
    }

    public List<CampaignReq> getPaidCampaignAdmin() {
        return campaignRepository.findByClientStatus("Paid");
    }

    public List<CampaignReq> getLiveCampaignAdmin() {
        List<CampaignReq> campaign = campaignRepository.findByClientStatusAndBlogerStatus("Done", "Done");
        List<CampaignReq> liveCampaign = new ArrayList<>();

        // Specify the desired time zone
        ZoneId zoneId = ZoneId.of("UTC"); // Change to your desired time zone
        LocalDate now = LocalDate.now(zoneId);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // Adjust pattern as needed

        for (CampaignReq campaignReq : campaign) {
            try {
                LocalDate fromDate = LocalDate.parse(campaignReq.getFrom(), formatter);
                LocalDate toDate = LocalDate.parse(campaignReq.getTo(), formatter);
                System.err.println("Processing Campaign: " + campaignReq.getCampaignDescription());
                System.err.println("Current Date: " + now + ", From: " + fromDate + ", To: " + toDate);

                if ((fromDate.isBefore(now) || fromDate.isEqual(now)) &&
                        (toDate.isAfter(now) || toDate.isEqual(now))) {
                    liveCampaign.add(campaignReq);
                }
            } catch (DateTimeParseException e) {
                System.out.println("Error parsing date for campaign: " + campaignReq.getId());
                System.out.println("Error details: " + e.getMessage());
            }
        }
        return liveCampaign;
    }

    public List<CampaignReq> getDoneCampaignAdmin() {
        // Fetch all campaigns with 'Done' status for both client and blogger
        List<CampaignReq> campaign = campaignRepository.findByClientStatusAndBlogerStatus("Done", "Done");
        List<CampaignReq> doneCampaign = new ArrayList<>();

        // Specify the desired time zone (e.g., UTC)
        ZoneId zoneId = ZoneId.of("UTC"); // Change this to your required time zone
        LocalDate now = LocalDate.now(zoneId);

        // Define the date format if needed (adjust the pattern based on your date
        // string format)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // Adjust the pattern as necessary

        // Iterate through each campaign and check if it's done based on the 'to' date
        for (CampaignReq campaignReq : campaign) {
            try {
                // Parse the 'to' date field
                LocalDate toDate = LocalDate.parse(campaignReq.getTo(), formatter);

                // Log campaign details for debugging
                System.err.println("Processing Campaign: " + campaignReq.getCampaignDescription());
                System.err.println("Current Date: " + now + ", To Date: " + toDate);

                // Check if the 'to' date is before the current date (campaign is done)
                if (toDate.isBefore(now)) {
                    doneCampaign.add(campaignReq);
                }
            } catch (DateTimeParseException e) {
                // Log the error and continue to the next campaign
                System.out.println("Error parsing 'to' date for campaign: " + campaignReq.getId());
                System.out.println("Error details: " + e.getMessage());
            }
        }

        return doneCampaign;
    }

    public Wallet getWallet(@NotNull String blogerId) {
        if (!blogerRepository.findById(blogerId).isPresent()) {
            throw new ResourceNotFoundException("Bloger Id", "Id", blogerId);
        }
        Bloger bloger = blogerRepository.findById(blogerId).get();
        Wallet wallet = new Wallet();
        wallet.setBalance(bloger.getWallet().size() * bloger.getPrice());
        ArrayList<CampaignReq> campaigns = new ArrayList<>();
        for (String campaignId : bloger.getWallet()) {
            if (!campaignRepository.findById(campaignId).isPresent()) {
                continue;
            }
            campaigns.add(campaignRepository.findById(campaignId).get());
        }
        wallet.setCampaigns(campaigns);
        return wallet;
    }

    public ArrayList<CampaignReq> getDoneCampaign(@NotNull String blogerId) {
        if (!blogerRepository.findById(blogerId).isPresent()) {
            throw new ResourceNotFoundException("Bloger Id", "Id", blogerId);
        }

        Bloger bloger = blogerRepository.findById(blogerId).get();
        ArrayList<CampaignReq> doneCampaigns = new ArrayList<>();
        for (String campaignId : bloger.getDoneCampaign()) {
            if (!campaignRepository.findById(campaignId).isPresent()) {
                continue;
            }
            doneCampaigns.add(campaignRepository.findById(campaignId).get());
        }
        return doneCampaigns;
    }
}
