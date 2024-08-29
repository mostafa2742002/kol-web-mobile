package com.nano.soft.kol.bloger.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nano.soft.kol.bloger.dto.BlogerDTO;
import com.nano.soft.kol.bloger.entity.Bloger;

import com.nano.soft.kol.bloger.entity.CampaignReq;
import com.nano.soft.kol.bloger.entity.CategoryNumber;
import com.nano.soft.kol.bloger.entity.PageResponse;
import com.nano.soft.kol.bloger.service.BlogerService;
import com.nano.soft.kol.dto.ErrorResponseDto;
import com.nano.soft.kol.dto.ResponseDto;
import com.nano.soft.kol.user.entity.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import java.util.*;

@RestController
@AllArgsConstructor
@Validated
@RequestMapping(path = "/api", produces = { MediaType.APPLICATION_JSON_VALUE })
public class BlogerController {
    private final BlogerService blogerService;

    @PostMapping("/signup/bloger")
    public ResponseEntity<?> signup(@RequestBody @Valid @NotNull BlogerDTO blogerDTO) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(blogerService.registerBloger(blogerDTO));
        } catch (MessagingException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to send verification email");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/bloger/{id}")
    public ResponseEntity<Bloger> getBloger(@PathVariable String id) {
        return ResponseEntity.ok(blogerService.getBloger(id));
    }

    @GetMapping("/bloger")
    public ResponseEntity<PageResponse<Bloger>> getBlogers(
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size) {
        PageResponse<Bloger> blogers = blogerService.getAllBlogers(page, size);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(blogers);
    }

    @GetMapping("/bloger/filter")
    public ResponseEntity<List<Bloger>> getBlogerByFilter(
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "country", required = false) String country,
            @RequestParam(name = "type", required = false) String type,
            @RequestParam(name = "age", required = false) Integer age) {
        return ResponseEntity.ok(blogerService.getBlogerByFilter(category, country, type, age));
    }

    @Operation(summary = "Get bloger profile", description = "Get bloger profile by bloger id")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)

            )) })
    @GetMapping("/profile/bloger")
    public ResponseEntity<Bloger> getProfileBloger(@RequestParam String email) {
        return ResponseEntity.ok(blogerService.getProfileBloger(email));
    }

    @GetMapping("/category")
    public ResponseEntity<ArrayList<CategoryNumber>> getCategories() {
        return ResponseEntity.ok(blogerService.getCategories());
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<PageResponse<Bloger>> getBlogersByCategory(
            @PathVariable String category,
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size) {
        PageResponse<Bloger> blogers = blogerService.getBlogersByCategory(category, page, size);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(blogers);
    }

    @PostMapping("/campaign/response/to-client")
    public ResponseEntity<ResponseDto> responseToClient(
            @RequestParam(name = "campaignId") String campaignId,
            @RequestParam(name = "blogerResponse") Boolean BlogerResponse,
            @RequestParam(name = "content") String content) {
        return ResponseEntity.ok(blogerService.responseToClient(campaignId, BlogerResponse, content));
    }

    @PostMapping("/campaign/complete/to-admin")
    public ResponseEntity<ResponseDto> completeToAdmin(
            @RequestBody @Valid @NotNull CampaignReq campaignComplete) {
        return ResponseEntity.ok(blogerService.completeToAdmin(campaignComplete));
    }

    @PostMapping("/campaign/complete/to-client")
    public ResponseEntity<ResponseDto> completeToClient(
            @RequestBody @Valid @NotNull CampaignReq campaignComplete) {
        return ResponseEntity.ok(blogerService.completeToClient(campaignComplete));
    }

    @GetMapping("/bloger/requested-campaign")
    public ResponseEntity<ArrayList<String>> getRequestedCampaign(@RequestParam @NotNull String blogerId) {
        return ResponseEntity.ok(blogerService.getRequestedCampaign(blogerId));
    }

    @GetMapping("/bloger/paid-campaign")
    public ResponseEntity<ArrayList<String>> getPaidCampaign(@RequestParam @NotNull String blogerId) {
        return ResponseEntity.ok(blogerService.getPaidCampaign(blogerId));
    }
}
