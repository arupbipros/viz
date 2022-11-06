package org.thingsboard.server.queue.usagestats;

import org.thingsboard.server.common.data.ApiUsageRecordKey;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.TenantId;

public interface TbApiUsageClient {

    void report(TenantId tenantId, CustomerId customerId, ApiUsageRecordKey key, long value);

    void report(TenantId tenantId, CustomerId customerId, ApiUsageRecordKey key);

}
