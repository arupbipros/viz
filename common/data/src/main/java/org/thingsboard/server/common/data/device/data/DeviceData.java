package org.thingsboard.server.common.data.device.data;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel
@Data
public class DeviceData {

    @ApiModelProperty(position = 1, value = "Device configuration for device profile type. DEFAULT is only supported value for now")
    private DeviceConfiguration configuration;
    @ApiModelProperty(position = 2, value = "Device transport configuration used to connect the device")
    private DeviceTransportConfiguration transportConfiguration;

}
