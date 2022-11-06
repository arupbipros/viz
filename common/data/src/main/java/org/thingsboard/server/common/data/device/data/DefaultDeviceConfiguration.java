package org.thingsboard.server.common.data.device.data;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.thingsboard.server.common.data.DeviceProfileType;

@ApiModel
@Data
public class DefaultDeviceConfiguration implements DeviceConfiguration {

    @Override
    public DeviceProfileType getType() {
        return DeviceProfileType.DEFAULT;
    }

}
