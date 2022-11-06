package org.thingsboard.rule.engine.action;

import lombok.Data;

@Data
public abstract class TbAbstractAlarmNodeConfiguration {

    static final String ALARM_DETAILS_BUILD_JS_TEMPLATE = "" +
            "var details = {};\n" +
            "if (metadata.prevAlarmDetails) {\n" +
            "    details = JSON.parse(metadata.prevAlarmDetails);\n" +
            "    //remove prevAlarmDetails from metadata\n" +
            "    delete metadata.prevAlarmDetails;\n" +
            "    //now metadata is the same as it comes IN this rule node\n" +
            "}\n" +
            "\n" +
            "\n" +
            "return details;";

    private String alarmType;
    private String alarmDetailsBuildJs;

}
