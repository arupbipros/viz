package org.thingsboard.server.common.data;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.thingsboard.server.common.data.edge.EdgeEvent;
import org.thingsboard.server.common.data.edge.EdgeEventActionType;
import org.thingsboard.server.common.data.edge.EdgeEventType;
import org.thingsboard.server.common.data.id.EdgeId;
import org.thingsboard.server.common.data.id.EntityId;
import org.thingsboard.server.common.data.id.TenantId;

import java.util.concurrent.ThreadLocalRandom;

@Slf4j
public final class EdgeUtils {

    private EdgeUtils() {
    }

    public static EdgeEventType getEdgeEventTypeByEntityType(EntityType entityType) {
        switch (entityType) {
            case EDGE:
                return EdgeEventType.EDGE;
            case DEVICE:
                return EdgeEventType.DEVICE;
            case DEVICE_PROFILE:
                return EdgeEventType.DEVICE_PROFILE;
            case ASSET:
                return EdgeEventType.ASSET;
            case ENTITY_VIEW:
                return EdgeEventType.ENTITY_VIEW;
            case DASHBOARD:
                return EdgeEventType.DASHBOARD;
            case USER:
                return EdgeEventType.USER;
            case RULE_CHAIN:
                return EdgeEventType.RULE_CHAIN;
            case ALARM:
                return EdgeEventType.ALARM;
            case TENANT:
                return EdgeEventType.TENANT;
            case CUSTOMER:
                return EdgeEventType.CUSTOMER;
            case WIDGETS_BUNDLE:
                return EdgeEventType.WIDGETS_BUNDLE;
            case WIDGET_TYPE:
                return EdgeEventType.WIDGET_TYPE;
            case OTA_PACKAGE:
                return EdgeEventType.OTA_PACKAGE;
            case QUEUE:
                return EdgeEventType.QUEUE;
            default:
                log.warn("Unsupported entity type [{}]", entityType);
                return null;
        }
    }

    public static int nextPositiveInt() {
        return ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE);
    }

    public static EdgeEvent constructEdgeEvent(TenantId tenantId,
                                               EdgeId edgeId,
                                               EdgeEventType type,
                                               EdgeEventActionType action,
                                               EntityId entityId,
                                               JsonNode body) {
        EdgeEvent edgeEvent = new EdgeEvent();
        edgeEvent.setTenantId(tenantId);
        edgeEvent.setEdgeId(edgeId);
        edgeEvent.setType(type);
        edgeEvent.setAction(action);
        if (entityId != null) {
            edgeEvent.setEntityId(entityId.getId());
        }
        edgeEvent.setBody(body);
        return edgeEvent;
    }
}
