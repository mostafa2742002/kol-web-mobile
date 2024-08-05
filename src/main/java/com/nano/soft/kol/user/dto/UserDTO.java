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

    private String name;

    private String first_name;
    private String last_name;

    private String phone;

    private String parentPhone;

    public UserDTO(User user) {
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.name = user.getName();
        this.phone = user.getPhone();
        this.parentPhone = user.getParentPhone();
        this.first_name = user.getFirst_name();
        this.last_name = user.getLast_name();
    }
}
