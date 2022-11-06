package org.thingsboard.server.service.security.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.thingsboard.server.common.data.security.Authority;

@ApiModel(value = "JWT Token Pair")
@Data
@NoArgsConstructor
public class JwtTokenPair {

    @ApiModelProperty(position = 1, value = "The JWT Access Token. Used to perform API calls.", example = "AAB254FF67D..")
    private String token;
    @ApiModelProperty(position = 1, value = "The JWT Refresh Token. Used to get new JWT Access Token if old one has expired.", example = "AAB254FF67D..")
    private String refreshToken;

    private Authority scope;

    public JwtTokenPair(String token, String refreshToken) {
        this.token = token;
        this.refreshToken = refreshToken;
    }

}
