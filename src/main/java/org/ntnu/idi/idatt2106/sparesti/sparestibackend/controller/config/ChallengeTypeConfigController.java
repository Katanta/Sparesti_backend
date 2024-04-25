package org.ntnu.idi.idatt2106.sparesti.sparestibackend.controller.config;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ntnu.idi.idatt2106.sparesti.sparestibackend.dto.config.ChallengeTypeConfigDTO;
import org.ntnu.idi.idatt2106.sparesti.sparestibackend.exception.config.ChallengeConfigNotFoundException;
import org.ntnu.idi.idatt2106.sparesti.sparestibackend.exception.config.ChallengeTypeConfigNotFoundException;
import org.ntnu.idi.idatt2106.sparesti.sparestibackend.exception.validation.BadInputException;
import org.ntnu.idi.idatt2106.sparesti.sparestibackend.exception.validation.ObjectNotValidException;
import org.ntnu.idi.idatt2106.sparesti.sparestibackend.service.UserConfigService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/users/me/config/challenge/type")
public class ChallengeTypeConfigController {

    private final UserConfigService userConfigService;

    @PostMapping
    public ResponseEntity<ChallengeTypeConfigDTO> createChallengeTypeConfig(
            @RequestBody ChallengeTypeConfigDTO challengeTypeConfigDTO,
            @AuthenticationPrincipal UserDetails userDetails)
            throws ChallengeConfigNotFoundException, ObjectNotValidException {
        log.info(
                "Creating challenge type config: {} for user: {}",
                challengeTypeConfigDTO,
                userDetails.getUsername());
        ChallengeTypeConfigDTO newConfig =
                userConfigService.createChallengeTypeConfig(
                        userDetails.getUsername(), challengeTypeConfigDTO);
        return ResponseEntity.ok(newConfig);
    }

    @GetMapping("/{type}")
    public ResponseEntity<ChallengeTypeConfigDTO> getChallengeTypeConfig(
            @PathVariable String type, @AuthenticationPrincipal UserDetails userDetails)
            throws ChallengeTypeConfigNotFoundException {
        log.info(
                "Getting challenge type config for user '{}' and type {}",
                userDetails.getUsername(),
                type);
        ChallengeTypeConfigDTO config =
                userConfigService.getChallengeTypeConfig(type, userDetails.getUsername());
        log.info("Successfully retrieved challenge type config '{}'", config);
        return ResponseEntity.ok(config);
    }

    @Operation(
            summary = "Update challenge type config",
            description = "Updates the challenge type config for the authenticated user.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Challenge type config updated"),
                @ApiResponse(responseCode = "404", description = "Challenge type config not found"),
                @ApiResponse(responseCode = "400", description = "Bad input")
            })
    @PutMapping
    public ResponseEntity<ChallengeTypeConfigDTO> updateChallengeTypeConfig(
            @Parameter(description = "Updated challenge type config details") @RequestBody
                    ChallengeTypeConfigDTO challengeTypeConfigDTO,
            @Parameter(description = "Details of the authenticated user") @AuthenticationPrincipal
                    UserDetails userDetails)
            throws ChallengeTypeConfigNotFoundException,
                    BadInputException,
                    ObjectNotValidException {
        log.info(
                "Received request to update challenge type config for user: {}",
                userDetails.getUsername());
        ChallengeTypeConfigDTO updatedConfig =
                userConfigService.updateChallengeTypeConfig(
                        userDetails.getUsername(), challengeTypeConfigDTO);
        log.info(
                "Successfully updated challenge type config for user: {} to {}",
                userDetails.getUsername(),
                updatedConfig);

        return ResponseEntity.ok(updatedConfig);
    }

    @Operation(
            summary = "Delete challenge type config",
            description = "Deletes the challenge type config for the authenticated user by type.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "Challenge type config deleted"),
                @ApiResponse(responseCode = "404", description = "Challenge type config not found")
            })
    @DeleteMapping("/{type}")
    public ResponseEntity<Void> deleteChallengeTypeConfig(
            @Parameter(description = "Type of the challenge config to delete") @PathVariable
                    String type,
            @Parameter(description = "Details of the authenticated user") @AuthenticationPrincipal
                    UserDetails userDetails) {
        log.info(
                "Received delete request by '{}' for challenge type config {}",
                userDetails.getUsername(),
                type);
        userConfigService.deleteChallengeTypeConfig(type, userDetails.getUsername());

        log.info("Successfully deleted challenge type config");
        return ResponseEntity.noContent().build();
    }
}
