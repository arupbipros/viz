package org.thingsboard.server.common.data.edge;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import org.thingsboard.server.common.data.BaseData;
import org.thingsboard.server.common.data.id.EdgeEventId;
import org.thingsboard.server.common.data.id.EdgeId;
import org.thingsboard.server.common.data.id.TenantId;

import java.util.UUID;

@Data
public class EdgeEvent extends BaseData<EdgeEventId> {

    private TenantId tenantId;
    private EdgeId edgeId;
    private EdgeEventActionType action;
    private UUID entityId;
    private String uid;
    private EdgeEventType type;
    private transient JsonNode body;

    public EdgeEvent() {
        super();
    }

    public EdgeEvent(EdgeEventId id) {
        super(id);
    }

    public EdgeEvent(EdgeEvent event) {
        super(event);
    }

}
