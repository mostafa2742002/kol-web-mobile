package com.nano.soft.kol.user.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import java.util.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.nano.soft.kol.bloger.dto.BlogerDTO;
import com.nano.soft.kol.bloger.entity.Bloger;
import com.nano.soft.kol.bloger.repo.BlogerRepository;
import com.nano.soft.kol.email.EmailService;
import com.nano.soft.kol.exception.ResourceNotFoundException;
import com.nano.soft.kol.jwt.JwtResponse;
import com.nano.soft.kol.jwt.JwtService;
import com.nano.soft.kol.user.dto.LoginDTO;
import com.nano.soft.kol.user.dto.UserDTO;
import com.nano.soft.kol.user.entity.User;
import com.nano.soft.kol.user.repo.UserRepository;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;
    private final EmailService emailService;
    private final BlogerRepository blogerRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username);
        if (user == null)
            throw new UsernameNotFoundException("User not found");
        return user;
    }

    public String registerUser(@NonNull UserDTO userDTO) throws MessagingException, InterruptedException {
        if (userRepository.findByEmail(userDTO.getEmail()) != null) {
            throw new IllegalArgumentException("User already exists");
        }
        userDTO.setEmail(userDTO.getEmail().toLowerCase());
        User user = new User(userDTO);
        String verificationToken = jwtService.generateToken(user);

        user.setEmailVerified(false);
        user.setVerificationToken(verificationToken);
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));

        User savedUser = userRepository.save(user);

        userRepository.save(savedUser);

        String subject = "Verify Your Email";

        // if we use render site then use this
        // String body = "Click the link to verify your
        // email:https://courses-website-q0gf.onrender.com/api/verifyemail?token="
        // + verificationToken;

        // if we use localhost then use this
        String body = "Click the link to verify your email:http://localhost:8080/api/verifyemail?token="
                + verificationToken;
        emailService.sendEmail(savedUser.getEmail(), subject, body);

        return "the user added successfully go to your email to verify your email";
    }

    public JwtResponse login(@NonNull LoginDTO loginDTO) {
        loginDTO.setEmail(loginDTO.getEmail().toLowerCase());
        User user = userRepository.findByEmail(loginDTO.getEmail());
        if (user != null && bCryptPasswordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {

            // if (user.isEmailVerified() == false)
            // throw new IllegalArgumentException("Email not verified");

            userRepository.save(user);

            return new JwtResponse(jwtService.generateToken(user), jwtService.generateRefreshToken(user), user, "user");
        }

        Bloger bloger = blogerRepository.findByEmail(loginDTO.getEmail());
        if (bloger != null && bCryptPasswordEncoder.matches(loginDTO.getPassword(), bloger.getPassword())) {

            // if (bloger.isEmailVerified() == false)
            // throw new IllegalArgumentException("Email not verified");

            blogerRepository.save(bloger);

            return new JwtResponse(jwtService.generateToken(bloger), jwtService.generateRefreshToken(bloger), bloger,
                    "bloger");
        }

        throw new IllegalArgumentException("Invalid credentials");
    }

    public User findUserByEmail(String email) {
        if (userRepository.findByEmail(email) == null) {
            throw new IllegalArgumentException("User not found");
        }
        return userRepository.findByEmail(email);
    }

    public String refreshToken(String refreshToken) {
        String email = jwtService.extractUserName(refreshToken);
        if (email == null) {
            throw new RuntimeException("Invalid Token");
        }
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        if (!jwtService.validateToken(refreshToken, userDetails)) {
            throw new RuntimeException("expired Token or Invalid");
        }

        return jwtService.generateToken(userDetails);
    }

    public ResponseEntity<String> verifyEmail(String token) {
        String email = jwtService.extractUserName(token);
        User user = userRepository.findByEmail(email);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        User user1 = user;
        if (user1.getVerificationToken().equals(token)) {
            user1.setEmailVerified(true);
            user1.setVerificationToken(null);
            userRepository.save(user1);

            return ResponseEntity.ok("Email verified successfully");

        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid Token");
        }
    }

    public ResponseEntity<String> updateProfile(UserDTO user, String user_id) {

        User user1 = userRepository.findById(user_id).orElse(null);

        if (user1 == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        if (user.getName() != null)
            user1.setName(user.getName());
        if (user.getPhone() != null)
            user1.setPhone(user.getPhone());
        if (user.getEmail() != null)
            user1.setEmail(user.getEmail());

        userRepository.save(user1);

        return ResponseEntity.ok("Profile updated successfully");
    }

    public ResponseEntity<String> updatePassword(String user_id, String oldPassword, String newPassword) {
        User user = userRepository.findById(user_id).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        if (!bCryptPasswordEncoder.matches(oldPassword, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Old password is incorrect");
        }

        user.setPassword(bCryptPasswordEncoder.encode(newPassword));
        userRepository.save(user);

        return ResponseEntity.ok("Password updated successfully");
    }

    public String forgotPassword(@NotNull String email) throws MessagingException, InterruptedException {

        // random otp from 5 digits
        int otp = (int) (Math.random() * (99999 - 10000 + 1) + 10000);
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        user.setOtp(String.valueOf(otp));
        userRepository.save(user);

        String subject = "Reset Password";
        String body = "Your OTP is: " + otp;

        emailService.sendEmail(email, subject, body);

        return "OTP sent to your email";
    }

    public void resetPassword(@NotNull String userEmail, @NotNull String otp, @NotNull String newPassword) {
        User user = userRepository.findByEmail(userEmail);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        if (!user.getOtp().equals(otp)) {
            throw new IllegalArgumentException("Invalid OTP");
        }

        user.setPassword(bCryptPasswordEncoder.encode(newPassword));
        user.setOtp(null);
        userRepository.save(user);
    }

    public User getProfileUser(String email) {

        if (userRepository.findByEmail(email) == null) {
            throw new ResourceNotFoundException("the user", "Email", email);
        }

        return userRepository.findByEmail(email);
    }

    public void addFavorite(@NotNull String userId, @NotNull String blogerId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new ResourceNotFoundException("the user", "id", userId);
        }

        Bloger bloger = blogerRepository.findById(blogerId).orElse(null);
        if (bloger == null) {
            throw new ResourceNotFoundException("the bloger", "id", blogerId);
        }

        user.getFavoriteBlogers().add(blogerId);
        userRepository.save(user);
    }

    public void removeFavorite(@NotNull String userId, @NotNull String blogerId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new ResourceNotFoundException("the user", "id", userId);
        }

        Bloger bloger = blogerRepository.findById(blogerId).orElse(null);
        if (bloger == null) {
            throw new ResourceNotFoundException("the bloger", "id", blogerId);
        }

        user.getFavoriteBlogers().remove(blogerId);
        userRepository.save(user);
    }

    public List<Bloger> getFavorite(@NotNull String userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new ResourceNotFoundException("the user", "id", userId);
        }

        List<Bloger> blogers = new ArrayList<>();
        for (String blogerId : user.getFavoriteBlogers()) {
            Bloger bloger = blogerRepository.findById(blogerId).orElse(null);
            if (bloger != null) {
                blogers.add(bloger);
            }
        }

        return blogers;
    }

}
