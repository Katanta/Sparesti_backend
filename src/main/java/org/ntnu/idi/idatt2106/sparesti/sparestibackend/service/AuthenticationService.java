package org.ntnu.idi.idatt2106.sparesti.sparestibackend.service;

import lombok.RequiredArgsConstructor;
import org.ntnu.idi.idatt2106.sparesti.sparestibackend.dto.token.AccessTokenResponse;
import org.ntnu.idi.idatt2106.sparesti.sparestibackend.dto.token.LoginRegisterResponse;
import org.ntnu.idi.idatt2106.sparesti.sparestibackend.dto.user.AuthenticationRequest;
import org.ntnu.idi.idatt2106.sparesti.sparestibackend.dto.user.RegisterRequest;
import org.ntnu.idi.idatt2106.sparesti.sparestibackend.exception.BadInputException;
import org.ntnu.idi.idatt2106.sparesti.sparestibackend.exception.UserAlreadyExistsException;
import org.ntnu.idi.idatt2106.sparesti.sparestibackend.exception.UserNotFoundException;
import org.ntnu.idi.idatt2106.sparesti.sparestibackend.exception.validation.ObjectNotValidException;
import org.ntnu.idi.idatt2106.sparesti.sparestibackend.mapper.RegisterMapper;
import org.ntnu.idi.idatt2106.sparesti.sparestibackend.model.User;
import org.ntnu.idi.idatt2106.sparesti.sparestibackend.model.enums.Role;
import org.ntnu.idi.idatt2106.sparesti.sparestibackend.security.JWTService;
import org.ntnu.idi.idatt2106.sparesti.sparestibackend.validation.ObjectValidator;
import org.ntnu.idi.idatt2106.sparesti.sparestibackend.validation.RegexValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service responsible for registering a new user, logging in an existing user and refreshing
 * a users access token
 *
 * @author Harry L.X. & Lars M.L.N
 * @version 1.0
 * @since 17.4.24
 */
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;
    private final AuthenticationManager manager;

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    private static final int ONE_DAY_IN_MINUTES = 60 * 24;
    private static final int ONE_WEEK_IN_MINUTES = ONE_DAY_IN_MINUTES * 7;

    private final ObjectValidator<RegisterRequest> registerRequestValidator;
    private final ObjectValidator<AuthenticationRequest> authenticationRequestValidator;

    /**
     * Registers a new, valid user. For a user to be valid, they have to
     * have a valid and unique username, a valid and unique email, valid first name and last names,
     * and a strong password.
     * @param request Wrapper for user information used for registering
     * @return Jwt tokens for the registered user
     * @throws BadInputException If the user information is invalid (username, first/last names, email) of if password is weak
     * @throws UserAlreadyExistsException If username or email have been taken
     */
    public LoginRegisterResponse register(RegisterRequest request)
            throws BadInputException, UserAlreadyExistsException, ObjectNotValidException {
        registerRequestValidator.validate(request);
        if (!(RegexValidator.isUsernameValid(request.username()))) {
            throw new BadInputException(
                    "The username can only contain letters, numbers and underscore, "
                            + "with the first character being a letter. "
                            + "The length must be between 3 and 30 characters");
        }
        if (!RegexValidator.isEmailValid(request.email())) {
            throw new BadInputException("The email address is invalid.");
        }
        if (!RegexValidator.isNameValid(request.firstName())) {
            throw new BadInputException(
                    "The first name: '" + request.firstName() + "' is invalid.");
        }
        if (!RegexValidator.isNameValid(request.lastName())) {
            throw new BadInputException("The last name: '" + request.lastName() + "' is invalid.");
        }
        if (userService.userExistsByUsername(request.username())) {
            throw new UserAlreadyExistsException(
                    "User with username: " + request.username() + " already exists");
        }
        if (userService.userExistByEmail(request.email())) {
            throw new UserAlreadyExistsException(
                    "User with email: " + request.email() + " already exists");
        }
        if (!RegexValidator.isPasswordStrong(request.password())) {
            throw new BadInputException(
                    "Password must be at least 8 characters long, include numbers, upper and lower"
                            + " case letters, and at least one special character");
        }

        logger.info("Creating user");
        String encodedPassword = passwordEncoder.encode(request.password());
        User user = RegisterMapper.INSTANCE.toEntity(request, Role.USER, encodedPassword);
        logger.info("Saving user with username '{}'", user.getUsername());
        userService.save(user);
        logger.info("Generating tokens");
        String jwtAccessToken = jwtService.generateToken(user, ONE_DAY_IN_MINUTES);
        String jwtRefreshToken = jwtService.generateToken(user, ONE_WEEK_IN_MINUTES);
        return RegisterMapper.INSTANCE.toDTO(user, jwtAccessToken, jwtRefreshToken);
    }

    /**
     * Log in user with credentials (username and password)
     * @param request Wrapper for username and password
     * @return Jwt tokens upon successful login
     * @throws BadInputException if no user has a matching username or password
     */
    public LoginRegisterResponse login(AuthenticationRequest request)
            throws BadInputException, ObjectNotValidException {
        authenticationRequestValidator.validate(request);
        if (!userService.userExistsByUsername(request.username())
                || !matches(
                        request.password(),
                        userService.findUserByUsername(request.username()).getPassword())) {
            throw new BadInputException("Username or password is incorrect");
        }

        logger.info("Setting authentication context");
        manager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        User user = userService.findUserByUsername(request.username());
        System.out.println("Generating tokens");
        String jwtAccessToken = jwtService.generateToken(user, ONE_DAY_IN_MINUTES);
        String jwtRefreshToken = jwtService.generateToken(user, ONE_WEEK_IN_MINUTES);
        return RegisterMapper.INSTANCE.toDTO(user, jwtAccessToken, jwtRefreshToken);
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

    /**
     * Refreshes access token given a valid refresh token
     * @param bearerToken Stringified HTTP-header (Authorization-header)
     * @return Access token wrapper if the refresh token is valid
     * @throws UserNotFoundException If the tokens subject matches no existing username
     */
    public AccessTokenResponse refreshAccessToken(String bearerToken) throws UserNotFoundException {
        String parsedRefreshToken = bearerToken.substring(7);
        User user = userService.findUserByUsername(jwtService.extractUsername(parsedRefreshToken));
        String newJWTAccessToken = jwtService.generateToken(user, 5);
        return new AccessTokenResponse(newJWTAccessToken);
    }
}
