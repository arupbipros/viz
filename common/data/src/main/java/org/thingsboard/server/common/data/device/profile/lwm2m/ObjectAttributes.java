package org.thingsboard.server.common.data.device.profile.lwm2m;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ObjectAttributes {

    private Long dim;
    private String ver;
    private Long pmin;
    private Long pmax;
    private Double gt;
    private Double lt;
    private Double st;

}
