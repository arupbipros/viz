package org.thingsboard.server.service.security.auth;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.thingsboard.server.common.data.CacheConstants;
import org.thingsboard.server.common.data.id.UserId;
import org.thingsboard.server.common.data.security.event.UserAuthDataChangedEvent;
import org.thingsboard.server.common.data.security.model.JwtToken;
import org.thingsboard.server.config.JwtSettings;
import org.thingsboard.server.service.security.model.token.JwtTokenFactory;

import javax.annotation.PostConstruct;
import java.util.Optional;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

@Service
@RequiredArgsConstructor
public class TokenOutdatingService {
    private final CacheManager cacheManager;
    private final JwtTokenFactory tokenFactory;
    private final JwtSettings jwtSettings;
    private Cache usersUpdateTimeCache;

    @PostConstruct
    protected void initCache() {
        usersUpdateTimeCache = cacheManager.getCache(CacheConstants.USERS_UPDATE_TIME_CACHE);
    }

    @EventListener(classes = UserAuthDataChangedEvent.class)
    public void onUserAuthDataChanged(UserAuthDataChangedEvent event) {
        usersUpdateTimeCache.put(toKey(event.getUserId()), event.getTs());
    }

    public boolean isOutdated(JwtToken token, UserId userId) {
        Claims claims = tokenFactory.parseTokenClaims(token).getBody();
        long issueTime = claims.getIssuedAt().getTime();

        return Optional.ofNullable(usersUpdateTimeCache.get(toKey(userId), Long.class))
                .map(outdatageTime -> {
                    if (System.currentTimeMillis() - outdatageTime <= SECONDS.toMillis(jwtSettings.getRefreshTokenExpTime())) {
                        return MILLISECONDS.toSeconds(issueTime) < MILLISECONDS.toSeconds(outdatageTime);
                    } else {
                        /*
                         * Means that since the outdating has passed more than
                         * the lifetime of refresh token (the longest lived)
                         * and there is no need to store outdatage time anymore
                         * as all the tokens issued before the outdatage time
                         * are now expired by themselves
                         * */
                        usersUpdateTimeCache.evict(toKey(userId));
                        return false;
                    }
                })
                .orElse(false);
    }

    private String toKey(UserId userId) {
        return userId.getId().toString();
    }
}
