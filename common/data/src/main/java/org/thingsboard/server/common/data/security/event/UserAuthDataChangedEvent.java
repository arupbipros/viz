package org.thingsboard.server.common.data.security.event;

import lombok.Data;
import org.thingsboard.server.common.data.id.UserId;

@Data
public class UserAuthDataChangedEvent {
    private final UserId userId;
    private final long ts;

    public UserAuthDataChangedEvent(UserId userId) {
        this.userId = userId;
        this.ts = System.currentTimeMillis();
    }

}
