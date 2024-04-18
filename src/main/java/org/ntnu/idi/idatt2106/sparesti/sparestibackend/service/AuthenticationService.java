package org.ntnu.idi.idatt2106.sparesti.sparestibackend.service;

import java.util.regex.Pattern;

import lombok.RequiredArgsConstructor;
import org.ntnu.idi.idatt2106.sparesti.sparestibackend.dto.AuthenticationRequest;
import org.ntnu.idi.idatt2106.sparesti.sparestibackend.dto.token.AccessTokenRequest;
import org.ntnu.idi.idatt2106.sparesti.sparestibackend.dto.token.AccessTokenResponse;
import org.ntnu.idi.idatt2106.sparesti.sparestibackend.dto.token.LoginRegisterResponse;
import org.ntnu.idi.idatt2106.sparesti.sparestibackend.exception.BadInputException;
import org.ntnu.idi.idatt2106.sparesti.sparestibackend.exception.InvalidTokenException;
import org.ntnu.idi.idatt2106.sparesti.sparestibackend.exception.UserAlreadyExistsException;
import org.ntnu.idi.idatt2106.sparesti.sparestibackend.model.User;
import org.ntnu.idi.idatt2106.sparesti.sparestibackend.model.UserConfig;
import org.ntnu.idi.idatt2106.sparesti.sparestibackend.model.enums.Experience;
import org.ntnu.idi.idatt2106.sparesti.sparestibackend.model.enums.Motivation;
import org.ntnu.idi.idatt2106.sparesti.sparestibackend.model.enums.Role;
import org.ntnu.idi.idatt2106.sparesti.sparestibackend.security.JWTService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;
    private final AuthenticationManager manager;

    public LoginRegisterResponse register(AuthenticationRequest request)
            throws UserAlreadyExistsException {
        if (!(isUsernameValid(request.getUsername()))) {
            throw new BadInputException(
            "The username can only contain letters, numbers and underscore, " +
              "with the first character being a letter. " +
              "The length must be between 3 and 30 characters");
        }
        if (userService.userExists(request.getUsername())) {
            throw new UserAlreadyExistsException(
              "User with username: " + request.getUsername() + " already exists");
        }
        if (!isPasswordStrong(request.getPassword())) {
            throw new BadInputException(
                    "Password must be at least 8 characters long, include numbers, upper and lower"
                            + " case letters, and at least one special character");
        }
        User user =
                User.builder()
                        .username(request.getUsername())
                        .password(passwordEncoder.encode(request.getPassword()))
                        .userConfig(UserConfig.builder().role(Role.USER).experience(Experience.LOW).motivation(Motivation.LOW).build())
                        .build();
        userService.save(user);
        String jwtAccessToken = jwtService.generateToken(user, 5);
        String jwtRefreshToken = jwtService.generateToken(user, 30);
        return LoginRegisterResponse.builder()
                .accessToken(jwtAccessToken)
                .refreshToken(jwtRefreshToken)
                .build();
    }

    private boolean isUsernameValid(String username) {
        String usernamePattern =
          "^[A-Za-z][A-Za-z0-9_]{2,29}$";
        return Pattern.compile(usernamePattern).matcher(username).matches();
    }

    /**
     * Checks if a password meets the strength criteria.
     *
     * @param password
     *            The password to check
     * @return true if the password meets the criteria, false otherwise
     */
    private boolean isPasswordStrong(String password) {
        // Example criteria: at least 8 characters, including numbers, letters and at least one
        // special character
        String passwordPattern =
                "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";
        return Pattern.compile(passwordPattern).matcher(password).matches();
    }

    public LoginRegisterResponse login(AuthenticationRequest request) {
        if (!userService.userExists(request.getUsername()) || !matches(request.getPassword(), userService.findUserByUsername(request.getUsername()).getPassword())) {
            throw new BadInputException(
              "Username or password is incorrect");
        }

        manager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(), request.getPassword()));
        User user = userService.findUserByUsername(request.getUsername());
        String jwtAccessToken = jwtService.generateToken(user, 5);
        String jwtRefreshToken = jwtService.generateToken(user, 30);
        return LoginRegisterResponse.builder()
                .accessToken(jwtAccessToken)
                .refreshToken(jwtRefreshToken)
                .build();
    }

    /**
     * Checks if an input password matches the stored (encrypted) password.
     *
     * @param inputPassword
     *            The input password to check
     * @param storedPassword
     *            The stored (encrypted) password to compare with
     * @return true if the input password matches the stored password, false otherwise
     */
    public boolean matches(String inputPassword, String storedPassword) {
        return passwordEncoder.matches(inputPassword, storedPassword);
    }

    public AccessTokenResponse refreshAccessToken(AccessTokenRequest request) {
        User user =
                userService.findUserByUsername(
                        jwtService.extractUsername(request.getRefreshToken()));
        String newJWTAccessToken;

        if (jwtService.isTokenValid(request.getRefreshToken(), user)) {
            newJWTAccessToken = jwtService.generateToken(user, 5);
        } else {
            throw new InvalidTokenException("Token is invalid");
        }

        return AccessTokenResponse.builder().accessToken(newJWTAccessToken).build();
    }
}
