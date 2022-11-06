package org.thingsboard.server.common.data.transport.snmp.config.impl;

import org.thingsboard.server.common.data.transport.snmp.SnmpCommunicationSpec;
import org.thingsboard.server.common.data.transport.snmp.SnmpMethod;
import org.thingsboard.server.common.data.transport.snmp.config.MultipleMappingsSnmpCommunicationConfig;

public class SharedAttributesSettingSnmpCommunicationConfig extends MultipleMappingsSnmpCommunicationConfig {

    @Override
    public SnmpCommunicationSpec getSpec() {
        return SnmpCommunicationSpec.SHARED_ATTRIBUTES_SETTING;
    }

    @Override
    public SnmpMethod getMethod() {
        return SnmpMethod.SET;
    }

}
