package com.nano.soft.kol.bloger.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.nano.soft.kol.bloger.dto.BlogerDTO;
import com.nano.soft.kol.user.entity.AuditableBase;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "blogers")
public class Bloger extends AuditableBase implements UserDetails {

    @Id
    @Schema(hidden = true)
    private String id;

    @NotNull(message = "username shouldn't be null")
    @Pattern(regexp = "^[a-zA-Z0-9 ]{3,30}$", message = "username must be between 3 and 30 characters long and can only contain letters and numbers")
    private String name;

    private String first_name;

    private String last_name;

    @Email(message = "invalid email address")
    @NotNull(message = "email shouldn't be null")
    private String email;

    private String password;

    @NotNull(message = "phone shouldn't be null")
    private String phone;

    private String image;

    private Integer price;

    @Schema(hidden = true)
    private ArrayList<String> notifications = new ArrayList<>();
    private ArrayList<String> requestedCampaign = new ArrayList<>();
    private ArrayList<String> paidCampaign = new ArrayList<>();

    @Schema(hidden = true)
    private String token;
    @Schema(hidden = true)
    private boolean emailVerified;
    @Schema(hidden = true)
    private String verificationToken;
    @Schema(hidden = true)
    private String otp;

    // bloger
    // New attributes based on the form
    private String whatsapp;
    private String countryOfResidence;
    private String city;
    private String fullAddress;
    private String bio;
    private String instagramUrl;
    private Integer instagramFollowers;
    private String instagramPosts;
    private Integer instagramEngagement;
    private String snapchatUrl;
    private Integer snapchatFollowers;
    private String tiktokUrl;
    private Integer tiktokFollowers;
    private String youtubeUrl;
    private Integer youtubeFollowers;
    private String career;
    private String specialization;
    private Date dateOfBirth; // Consider using a Date type and appropriate format annotation
    private String language;
    private String gender;
    private String maritalStatus;
    private Boolean showsFaceInStories;
    private Boolean usesVoiceInContent;
    private Boolean goesInPublicPlaces;
    private Boolean wearsHijab;
    private String nationality;
    private ArrayList<String> interests;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public Bloger(@Valid @NotNull BlogerDTO blogerDTO) {
        this.name = blogerDTO.getName();
        this.first_name = blogerDTO.getFirst_name();
        this.last_name = blogerDTO.getLast_name();
        this.email = blogerDTO.getEmail();
        this.password = blogerDTO.getPassword();
        this.phone = blogerDTO.getPhone();
        this.image = blogerDTO.getImage();
        this.whatsapp = blogerDTO.getWhatsapp();
        this.countryOfResidence = blogerDTO.getCountryOfResidence();
        this.city = blogerDTO.getCity();
        this.fullAddress = blogerDTO.getFullAddress();
        this.bio = blogerDTO.getBio();
        this.instagramUrl = blogerDTO.getInstagramUrl();
        this.instagramFollowers = blogerDTO.getInstagramFollowers();
        this.instagramPosts = blogerDTO.getInstagramPosts();
        this.instagramEngagement = blogerDTO.getInstagramEngagement();
        this.snapchatUrl = blogerDTO.getSnapchatUrl();
        this.snapchatFollowers = blogerDTO.getSnapchatFollowers();
        this.tiktokUrl = blogerDTO.getTiktokUrl();
        this.tiktokFollowers = blogerDTO.getTiktokFollowers();
        this.youtubeUrl = blogerDTO.getYoutubeUrl();
        this.youtubeFollowers = blogerDTO.getYoutubeFollowers();
        this.career = blogerDTO.getCareer();
        this.specialization = blogerDTO.getSpecialization();
        this.dateOfBirth = blogerDTO.getDateOfBirth();
        this.language = blogerDTO.getLanguage();
        this.gender = blogerDTO.getGender();
        this.maritalStatus = blogerDTO.getMaritalStatus();
        this.showsFaceInStories = blogerDTO.getShowsFaceInStories();
        this.usesVoiceInContent = blogerDTO.getUsesVoiceInContent();
        this.goesInPublicPlaces = blogerDTO.getGoesInPublicPlaces();
        this.wearsHijab = blogerDTO.getWearsHijab();
        this.nationality = blogerDTO.getNationality();
        this.price = blogerDTO.getPrice();
        // this.interests = blogerDTO.getInterests();
    }

}
