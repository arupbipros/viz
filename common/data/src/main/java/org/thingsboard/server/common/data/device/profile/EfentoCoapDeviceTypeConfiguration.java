package org.thingsboard.server.common.data.device.profile;

import lombok.Data;
import org.thingsboard.server.common.data.CoapDeviceType;

@Data
public class EfentoCoapDeviceTypeConfiguration implements CoapDeviceTypeConfiguration {

    @Override
    public CoapDeviceType getCoapDeviceType() {
        return CoapDeviceType.EFENTO;
    }
}
