package org.thingsboard.server.cache;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "cache")
@Data
public class CacheSpecsMap {

    @Getter
    private Map<String, CacheSpecs> specs;

}
