package com.nano.soft.kol.user.dto;

import com.nano.soft.kol.user.entity.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    @NotBlank(message = "Email is mandatory")
    @Email(message = "Provide a valid email")
    private String email;

    @NotBlank(message = "Password is mandatory")
    private String password;

    private String fullname;

    private String username;
    private String city;
    private String country;

    private String phone;

    public UserDTO(User user) {
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.fullname = user.getFullname();
        this.username = user.getUsername();
        this.city = user.getCity();
        this.country = user.getCountry();
        this.phone = user.getPhone();
        
    }
}
