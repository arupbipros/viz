package org.thingsboard.server.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.rule.engine.api.MailService;
import org.thingsboard.server.common.data.AdminSettings;
import org.thingsboard.server.service.mail.DefaultMailService;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public abstract class BaseAdminControllerTest extends AbstractControllerTest {

    @Autowired
    MailService mailService;

    @Autowired
    DefaultMailService defaultMailService;

    @Test
    public void testFindAdminSettingsByKey() throws Exception {
        loginSysAdmin();
        doGet("/api/admin/settings/general")
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.id", notNullValue()))
        .andExpect(jsonPath("$.key", is("general")))
        .andExpect(jsonPath("$.jsonValue.baseUrl", is("http://localhost:8080")));
        
        doGet("/api/admin/settings/mail")
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.id", notNullValue()))
        .andExpect(jsonPath("$.key", is("mail")))
        .andExpect(jsonPath("$.jsonValue.smtpProtocol", is("smtp")))
        .andExpect(jsonPath("$.jsonValue.smtpHost", is("localhost")))
        .andExpect(jsonPath("$.jsonValue.smtpPort", is("25")));
        
        doGet("/api/admin/settings/unknown")
        .andExpect(status().isNotFound());
        
    }
    
    @Test
    public void testSaveAdminSettings() throws Exception {
        loginSysAdmin();
        AdminSettings adminSettings = doGet("/api/admin/settings/general", AdminSettings.class); 
        
        JsonNode jsonValue = adminSettings.getJsonValue();
        ((ObjectNode) jsonValue).put("baseUrl", "http://myhost.org");
        adminSettings.setJsonValue(jsonValue);

        doPost("/api/admin/settings", adminSettings).andExpect(status().isOk());
        
        doGet("/api/admin/settings/general")
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.jsonValue.baseUrl", is("http://myhost.org")));
        
        ((ObjectNode) jsonValue).put("baseUrl", "http://localhost:8080");
        adminSettings.setJsonValue(jsonValue);
        
        doPost("/api/admin/settings", adminSettings)
        .andExpect(status().isOk());
    }

    @Test
    public void testSaveAdminSettingsWithEmptyKey() throws Exception {
        loginSysAdmin();
        AdminSettings adminSettings = doGet("/api/admin/settings/mail", AdminSettings.class); 
        adminSettings.setKey(null);
        doPost("/api/admin/settings", adminSettings)
        .andExpect(status().isBadRequest())
        .andExpect(statusReason(containsString("Key should be specified")));
    }
    
    @Test
    public void testChangeAdminSettingsKey() throws Exception {
        loginSysAdmin();
        AdminSettings adminSettings = doGet("/api/admin/settings/mail", AdminSettings.class); 
        adminSettings.setKey("newKey");
        doPost("/api/admin/settings", adminSettings)
        .andExpect(status().isBadRequest())
        .andExpect(statusReason(containsString("is prohibited")));
    }

    @Test
    public void testSendTestMail() throws Exception {
        loginSysAdmin();
        AdminSettings adminSettings = doGet("/api/admin/settings/mail", AdminSettings.class);
        doPost("/api/admin/settings/testMail", adminSettings)
        .andExpect(status().isOk());
    }

    @Test
    public void testSendTestMailTimeout() throws Exception {
        loginSysAdmin();
        AdminSettings adminSettings = doGet("/api/admin/settings/mail", AdminSettings.class);
        ObjectNode objectNode = JacksonUtil.fromString(adminSettings.getJsonValue().toString(), ObjectNode.class);

        objectNode.put("smtpHost", "mail.gandi.net");
        objectNode.put("timeout", 1_000);
        objectNode.put("username", "username");
        objectNode.put("password", "password");

        adminSettings.setJsonValue(objectNode);

        Mockito.doAnswer((invocations) -> {
            var jsonConfig = (JsonNode) invocations.getArgument(0);
            var email = (String) invocations.getArgument(1);

            defaultMailService.sendTestMail(jsonConfig, email);
            return null;
        }).when(mailService).sendTestMail(Mockito.any(), Mockito.anyString());
        doPost("/api/admin/settings/testMail", adminSettings).andExpect(status().is5xxServerError());
        Mockito.doNothing().when(mailService).sendTestMail(Mockito.any(), Mockito.any());
    }
}
