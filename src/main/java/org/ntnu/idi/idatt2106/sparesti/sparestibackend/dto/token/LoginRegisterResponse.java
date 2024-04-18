package org.ntnu.idi.idatt2106.sparesti.sparestibackend.dto.token;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class LoginRegisterResponse {
    private String accessToken;
    private String refreshToken;
}