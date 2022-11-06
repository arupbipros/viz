package org.thingsboard.server.common.data.query;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;

@ApiModel
@Data
public class EntityKey implements Serializable {
    private final EntityKeyType type;
    private final String key;
}
