package com.nano.soft.kol.bloger.dto;

import java.util.ArrayList;
import java.util.Date;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogerDTO {

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

    private Integer price;

    private String image;

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
}
