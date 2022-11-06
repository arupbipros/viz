package org.thingsboard.server.service.edge.rpc;

import org.thingsboard.server.common.data.edge.Edge;
import org.thingsboard.server.common.data.id.EdgeId;
import org.thingsboard.server.common.data.id.TenantId;

public interface EdgeRpcService {

    void updateEdge(TenantId tenantId, Edge edge);

    void deleteEdge(TenantId tenantId, EdgeId edgeId);

    void onEdgeEvent(TenantId tenantId, EdgeId edgeId);

    void startSyncProcess(TenantId tenantId, EdgeId edgeId);
}
