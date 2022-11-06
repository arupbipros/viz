package org.thingsboard.server.common.data.device.profile;

import lombok.Data;
import org.thingsboard.server.common.data.CoapDeviceType;

@Data
public class DefaultCoapDeviceTypeConfiguration implements CoapDeviceTypeConfiguration {

    private TransportPayloadTypeConfiguration transportPayloadTypeConfiguration;

    @Override
    public CoapDeviceType getCoapDeviceType() {
        return CoapDeviceType.DEFAULT;
    }

    public TransportPayloadTypeConfiguration getTransportPayloadTypeConfiguration() {
        if (transportPayloadTypeConfiguration != null) {
            return transportPayloadTypeConfiguration;
        } else {
            return new JsonTransportPayloadConfiguration();
        }
    }

}
