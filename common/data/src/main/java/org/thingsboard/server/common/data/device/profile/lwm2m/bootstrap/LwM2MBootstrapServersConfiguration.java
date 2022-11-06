package org.thingsboard.server.common.data.device.profile.lwm2m.bootstrap;

import lombok.Data;

import java.util.List;

@Data
public class LwM2MBootstrapServersConfiguration {

    List<LwM2MBootstrapServerCredential> bootstrap;

}
