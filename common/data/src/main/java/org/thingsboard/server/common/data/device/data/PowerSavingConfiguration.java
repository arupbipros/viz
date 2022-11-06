package org.thingsboard.server.common.data.device.data;

import lombok.Data;

@Data
public class PowerSavingConfiguration {
    private PowerMode powerMode;
    private Long psmActivityTimer;
    private Long edrxCycle;
    private Long pagingTransmissionWindow;
}
