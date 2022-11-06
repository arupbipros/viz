package org.thingsboard.server.common.data.transport.snmp.config.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.thingsboard.server.common.data.transport.snmp.SnmpCommunicationSpec;
import org.thingsboard.server.common.data.transport.snmp.config.RepeatingQueryingSnmpCommunicationConfig;

@EqualsAndHashCode(callSuper = true)
@Data
public class TelemetryQueryingSnmpCommunicationConfig extends RepeatingQueryingSnmpCommunicationConfig {

    @Override
    public SnmpCommunicationSpec getSpec() {
        return SnmpCommunicationSpec.TELEMETRY_QUERYING;
    }

}
