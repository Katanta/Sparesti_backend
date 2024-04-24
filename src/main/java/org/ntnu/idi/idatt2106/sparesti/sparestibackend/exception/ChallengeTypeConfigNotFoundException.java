package org.ntnu.idi.idatt2106.sparesti.sparestibackend.exception;

public class ChallengeTypeConfigNotFoundException extends RuntimeException {
    public ChallengeTypeConfigNotFoundException(String type) {
        super("No configuration found for challenge type: " + type);
    }
}