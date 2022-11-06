package org.thingsboard.server.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.os72.protobuf.dynamic.DynamicSchema;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.squareup.wire.schema.internal.parser.ProtoFileElement;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.thingsboard.server.common.data.Customer;
import org.thingsboard.server.common.data.Dashboard;
import org.thingsboard.server.common.data.Device;
import org.thingsboard.server.common.data.DeviceProfile;
import org.thingsboard.server.common.data.DeviceProfileInfo;
import org.thingsboard.server.common.data.DeviceProfileProvisionType;
import org.thingsboard.server.common.data.DeviceProfileType;
import org.thingsboard.server.common.data.DeviceTransportType;
import org.thingsboard.server.common.data.OtaPackageInfo;
import org.thingsboard.server.common.data.SaveOtaPackageInfoRequest;
import org.thingsboard.server.common.data.StringUtils;
import org.thingsboard.server.common.data.Tenant;
import org.thingsboard.server.common.data.User;
import org.thingsboard.server.common.data.audit.ActionType;
import org.thingsboard.server.common.data.device.profile.DeviceProfileTransportConfiguration;
import org.thingsboard.server.common.data.device.profile.JsonTransportPayloadConfiguration;
import org.thingsboard.server.common.data.device.profile.MqttDeviceProfileTransportConfiguration;
import org.thingsboard.server.common.data.device.profile.ProtoTransportPayloadConfiguration;
import org.thingsboard.server.common.data.device.profile.TransportPayloadTypeConfiguration;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;
import org.thingsboard.server.common.data.rule.RuleChain;
import org.thingsboard.server.common.data.security.Authority;
import org.thingsboard.server.dao.exception.DataValidationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.thingsboard.server.common.data.ota.OtaPackageType.FIRMWARE;
import static org.thingsboard.server.common.data.ota.OtaPackageType.SOFTWARE;

public abstract class BaseDeviceProfileControllerTest extends AbstractControllerTest {

    private IdComparator<DeviceProfile> idComparator = new IdComparator<>();
    private IdComparator<DeviceProfileInfo> deviceProfileInfoIdComparator = new IdComparator<>();

    private Tenant savedTenant;
    private User tenantAdmin;

    @Before
    public void beforeTest() throws Exception {
        loginSysAdmin();

        Tenant tenant = new Tenant();
        tenant.setTitle("My tenant");
        savedTenant = doPost("/api/tenant", tenant, Tenant.class);
        Assert.assertNotNull(savedTenant);

        tenantAdmin = new User();
        tenantAdmin.setAuthority(Authority.TENANT_ADMIN);
        tenantAdmin.setTenantId(savedTenant.getId());
        tenantAdmin.setEmail("tenant2@thingsboard.org");
        tenantAdmin.setFirstName("Joe");
        tenantAdmin.setLastName("Downs");

        tenantAdmin = createUserAndLogin(tenantAdmin, "testPassword1");
    }

    @After
    public void afterTest() throws Exception {
        loginSysAdmin();

        doDelete("/api/tenant/" + savedTenant.getId().getId().toString())
                .andExpect(status().isOk());
    }

    @Test
    public void testSaveDeviceProfile() throws Exception {
        DeviceProfile deviceProfile = this.createDeviceProfile("Device Profile");

        Mockito.reset(tbClusterService, auditLogService);

        DeviceProfile savedDeviceProfile = doPost("/api/deviceProfile", deviceProfile, DeviceProfile.class);
        Assert.assertNotNull(savedDeviceProfile);
        Assert.assertNotNull(savedDeviceProfile.getId());
        Assert.assertTrue(savedDeviceProfile.getCreatedTime() > 0);
        Assert.assertEquals(deviceProfile.getName(), savedDeviceProfile.getName());
        Assert.assertEquals(deviceProfile.getDescription(), savedDeviceProfile.getDescription());
        Assert.assertEquals(deviceProfile.getProfileData(), savedDeviceProfile.getProfileData());
        Assert.assertEquals(deviceProfile.isDefault(), savedDeviceProfile.isDefault());
        Assert.assertEquals(deviceProfile.getDefaultRuleChainId(), savedDeviceProfile.getDefaultRuleChainId());
        Assert.assertEquals(DeviceProfileProvisionType.DISABLED, savedDeviceProfile.getProvisionType());

        testNotifyEntityBroadcastEntityStateChangeEventOneTime(savedDeviceProfile, savedDeviceProfile.getId(), savedDeviceProfile.getId(),
                savedTenant.getId(), tenantAdmin.getCustomerId(), tenantAdmin.getId(), tenantAdmin.getEmail(),
                ActionType.ADDED);

        savedDeviceProfile.setName("New device profile");
        doPost("/api/deviceProfile", savedDeviceProfile, DeviceProfile.class);
        DeviceProfile foundDeviceProfile = doGet("/api/deviceProfile/" + savedDeviceProfile.getId().getId().toString(), DeviceProfile.class);
        Assert.assertEquals(savedDeviceProfile.getName(), foundDeviceProfile.getName());

        testNotifyEntityBroadcastEntityStateChangeEventOneTime(foundDeviceProfile, foundDeviceProfile.getId(), foundDeviceProfile.getId(),
                savedTenant.getId(), tenantAdmin.getCustomerId(), tenantAdmin.getId(), tenantAdmin.getEmail(),
                ActionType.UPDATED);
    }

    @Test
    public void saveDeviceProfileWithViolationOfValidation() throws Exception {
        String msgError = msgErrorFieldLength("name");

        Mockito.reset(tbClusterService, auditLogService);

        DeviceProfile createDeviceProfile = this.createDeviceProfile(StringUtils.randomAlphabetic(300));
        doPost("/api/deviceProfile", createDeviceProfile)
                .andExpect(status().isBadRequest())
                .andExpect(statusReason(containsString(msgError)));

        testNotifyEntityEqualsOneTimeServiceNeverError(createDeviceProfile, savedTenant.getId(),
                tenantAdmin.getId(), tenantAdmin.getEmail(), ActionType.ADDED, new DataValidationException(msgError));
    }

    @Test
    public void testFindDeviceProfileById() throws Exception {
        DeviceProfile deviceProfile = this.createDeviceProfile("Device Profile");
        DeviceProfile savedDeviceProfile = doPost("/api/deviceProfile", deviceProfile, DeviceProfile.class);
        DeviceProfile foundDeviceProfile = doGet("/api/deviceProfile/" + savedDeviceProfile.getId().getId().toString(), DeviceProfile.class);
        Assert.assertNotNull(foundDeviceProfile);
        Assert.assertEquals(savedDeviceProfile, foundDeviceProfile);
    }

    @Test
    public void whenGetDeviceProfileById_thenPermissionsAreChecked() throws Exception {
        DeviceProfile deviceProfile = createDeviceProfile("Device profile 1", null);
        deviceProfile = doPost("/api/deviceProfile", deviceProfile, DeviceProfile.class);

        loginDifferentTenant();

        doGet("/api/deviceProfile/" + deviceProfile.getId())
                .andExpect(status().isForbidden())
                .andExpect(statusReason(containsString(msgErrorPermission)));
    }

    @Test
    public void testFindDeviceProfileInfoById() throws Exception {
        DeviceProfile deviceProfile = this.createDeviceProfile("Device Profile");
        DeviceProfile savedDeviceProfile = doPost("/api/deviceProfile", deviceProfile, DeviceProfile.class);
        DeviceProfileInfo foundDeviceProfileInfo = doGet("/api/deviceProfileInfo/" + savedDeviceProfile.getId().getId().toString(), DeviceProfileInfo.class);
        Assert.assertNotNull(foundDeviceProfileInfo);
        Assert.assertEquals(savedDeviceProfile.getId(), foundDeviceProfileInfo.getId());
        Assert.assertEquals(savedDeviceProfile.getName(), foundDeviceProfileInfo.getName());
        Assert.assertEquals(savedDeviceProfile.getType(), foundDeviceProfileInfo.getType());

        Customer customer = new Customer();
        customer.setTitle("Customer");
        customer.setTenantId(savedTenant.getId());
        Customer savedCustomer = doPost("/api/customer", customer, Customer.class);

        User customerUser = new User();
        customerUser.setAuthority(Authority.CUSTOMER_USER);
        customerUser.setTenantId(savedTenant.getId());
        customerUser.setCustomerId(savedCustomer.getId());
        customerUser.setEmail("customer2@thingsboard.org");

        createUserAndLogin(customerUser, "customer");

        foundDeviceProfileInfo = doGet("/api/deviceProfileInfo/" + savedDeviceProfile.getId().getId().toString(), DeviceProfileInfo.class);
        Assert.assertNotNull(foundDeviceProfileInfo);
        Assert.assertEquals(savedDeviceProfile.getId(), foundDeviceProfileInfo.getId());
        Assert.assertEquals(savedDeviceProfile.getName(), foundDeviceProfileInfo.getName());
        Assert.assertEquals(savedDeviceProfile.getType(), foundDeviceProfileInfo.getType());
    }

    @Test
    public void whenGetDeviceProfileInfoById_thenPermissionsAreChecked() throws Exception {
        DeviceProfile deviceProfile = createDeviceProfile("Device profile 1", null);
        deviceProfile = doPost("/api/deviceProfile", deviceProfile, DeviceProfile.class);

        loginDifferentTenant();
        doGet("/api/deviceProfileInfo/" + deviceProfile.getId())
                .andExpect(status().isForbidden())
                .andExpect(statusReason(containsString(msgErrorPermission)));
    }

    @Test
    public void testFindDefaultDeviceProfileInfo() throws Exception {
        DeviceProfileInfo foundDefaultDeviceProfileInfo = doGet("/api/deviceProfileInfo/default", DeviceProfileInfo.class);
        Assert.assertNotNull(foundDefaultDeviceProfileInfo);
        Assert.assertNotNull(foundDefaultDeviceProfileInfo.getId());
        Assert.assertNotNull(foundDefaultDeviceProfileInfo.getName());
        Assert.assertNotNull(foundDefaultDeviceProfileInfo.getType());
        Assert.assertEquals(DeviceProfileType.DEFAULT, foundDefaultDeviceProfileInfo.getType());
        Assert.assertEquals("default", foundDefaultDeviceProfileInfo.getName());
    }

    @Test
    public void testSetDefaultDeviceProfile() throws Exception {
        DeviceProfile deviceProfile = this.createDeviceProfile("Device Profile 1");
        DeviceProfile savedDeviceProfile = doPost("/api/deviceProfile", deviceProfile, DeviceProfile.class);

        Mockito.reset(tbClusterService, auditLogService);

        DeviceProfile defaultDeviceProfile = doPost("/api/deviceProfile/" + savedDeviceProfile.getId().getId().toString() + "/default", DeviceProfile.class);
        Assert.assertNotNull(defaultDeviceProfile);
        DeviceProfileInfo foundDefaultDeviceProfile = doGet("/api/deviceProfileInfo/default", DeviceProfileInfo.class);
        Assert.assertNotNull(foundDefaultDeviceProfile);
        Assert.assertEquals(savedDeviceProfile.getName(), foundDefaultDeviceProfile.getName());
        Assert.assertEquals(savedDeviceProfile.getId(), foundDefaultDeviceProfile.getId());
        Assert.assertEquals(savedDeviceProfile.getType(), foundDefaultDeviceProfile.getType());

        testNotifyEntityOneTimeMsgToEdgeServiceNever(defaultDeviceProfile, defaultDeviceProfile.getId(), defaultDeviceProfile.getId(),
                savedTenant.getId(), tenantAdmin.getCustomerId(), tenantAdmin.getId(), tenantAdmin.getEmail(),
                ActionType.UPDATED);
    }

    @Test
    public void testSaveDeviceProfileWithEmptyName() throws Exception {
        DeviceProfile deviceProfile = new DeviceProfile();

        Mockito.reset(tbClusterService, auditLogService);

        String msgError = "Device profile name " + msgErrorShouldBeSpecified;
        doPost("/api/deviceProfile", deviceProfile)
                .andExpect(status().isBadRequest())
                .andExpect(statusReason(containsString(msgError)));

        testNotifyEntityEqualsOneTimeServiceNeverError(deviceProfile, savedTenant.getId(),
                tenantAdmin.getId(), tenantAdmin.getEmail(), ActionType.ADDED, new DataValidationException(msgError));
    }

    @Test
    public void testSaveDeviceProfileWithSameName() throws Exception {
        DeviceProfile deviceProfile = this.createDeviceProfile("Device Profile");
        doPost("/api/deviceProfile", deviceProfile).andExpect(status().isOk());
        DeviceProfile deviceProfile2 = this.createDeviceProfile("Device Profile");

        Mockito.reset(tbClusterService, auditLogService);

        String msgError = "Device profile with such name already exists";
        doPost("/api/deviceProfile", deviceProfile2)
                .andExpect(status().isBadRequest())
                .andExpect(statusReason(containsString(msgError)));

        testNotifyEntityEqualsOneTimeServiceNeverError(deviceProfile, savedTenant.getId(),
                tenantAdmin.getId(), tenantAdmin.getEmail(), ActionType.ADDED, new DataValidationException(msgError));
    }

    @Test
    public void testSaveDeviceProfileWithSameProvisionDeviceKey() throws Exception {
        DeviceProfile deviceProfile = this.createDeviceProfile("Device Profile");
        deviceProfile.setProvisionDeviceKey("testProvisionDeviceKey");
        doPost("/api/deviceProfile", deviceProfile).andExpect(status().isOk());
        DeviceProfile deviceProfile2 = this.createDeviceProfile("Device Profile 2");
        deviceProfile2.setProvisionDeviceKey("testProvisionDeviceKey");

        Mockito.reset(tbClusterService, auditLogService);

        String msgError = "Device profile with such provision device key already exists";
        doPost("/api/deviceProfile", deviceProfile2)
                .andExpect(status().isBadRequest())
                .andExpect(statusReason(containsString(msgError)));

        testNotifyEntityEqualsOneTimeServiceNeverError(deviceProfile, savedTenant.getId(),
                tenantAdmin.getId(), tenantAdmin.getEmail(), ActionType.ADDED, new DataValidationException(msgError));
    }

    @Test
    public void testChangeDeviceProfileTypeNull() throws Exception {
        DeviceProfile deviceProfile = this.createDeviceProfile("Device Profile");
        DeviceProfile savedDeviceProfile = doPost("/api/deviceProfile", deviceProfile, DeviceProfile.class);

        Mockito.reset(tbClusterService, auditLogService);

        savedDeviceProfile.setType(null);
        String msgError = "Device profile type " + msgErrorShouldBeSpecified;
        doPost("/api/deviceProfile", savedDeviceProfile)
                .andExpect(status().isBadRequest())
                .andExpect(statusReason(containsString(msgError)));

        testNotifyEntityEqualsOneTimeServiceNeverError(deviceProfile, savedTenant.getId(),
                tenantAdmin.getId(), tenantAdmin.getEmail(), ActionType.UPDATED, new DataValidationException(msgError));
    }

    @Test
    public void testChangeDeviceProfileTransportTypeWithExistingDevices() throws Exception {
        DeviceProfile deviceProfile = this.createDeviceProfile("Device Profile");
        DeviceProfile savedDeviceProfile = doPost("/api/deviceProfile", deviceProfile, DeviceProfile.class);
        Device device = new Device();
        device.setName("Test device");
        device.setType("default");
        device.setDeviceProfileId(savedDeviceProfile.getId());
        doPost("/api/device", device, Device.class);

        Mockito.reset(tbClusterService, auditLogService);

        String msgError = "Can't change device profile transport type because devices referenced it";
        savedDeviceProfile.setTransportType(DeviceTransportType.MQTT);
        doPost("/api/deviceProfile", savedDeviceProfile)
                .andExpect(status().isBadRequest())
                .andExpect(statusReason(containsString(msgError)));

        testNotifyEntityEqualsOneTimeServiceNeverError(deviceProfile, savedTenant.getId(),
                tenantAdmin.getId(), tenantAdmin.getEmail(), ActionType.UPDATED, new DataValidationException(msgError));
    }

    @Test
    public void testDeleteDeviceProfileWithExistingDevice() throws Exception {
        DeviceProfile deviceProfile = this.createDeviceProfile("Device Profile");
        DeviceProfile savedDeviceProfile = doPost("/api/deviceProfile", deviceProfile, DeviceProfile.class);

        Device device = new Device();
        device.setName("Test device");
        device.setType("default");
        device.setDeviceProfileId(savedDeviceProfile.getId());

        doPost("/api/device", device, Device.class);

        Mockito.reset(tbClusterService, auditLogService);

        doDelete("/api/deviceProfile/" + savedDeviceProfile.getId().getId().toString())
                .andExpect(status().isBadRequest())
                .andExpect(statusReason(containsString("The device profile referenced by the devices cannot be deleted")));

        testNotifyEntityNever(savedDeviceProfile.getId(), savedDeviceProfile);
    }

    @Test
    public void testSaveDeviceProfileWithRuleChainFromDifferentTenant() throws Exception {
        loginDifferentTenant();
        RuleChain ruleChain = new RuleChain();
        ruleChain.setName("Different rule chain");
        RuleChain savedRuleChain = doPost("/api/ruleChain", ruleChain, RuleChain.class);

        loginTenantAdmin();

        DeviceProfile deviceProfile = this.createDeviceProfile("Device Profile");
        deviceProfile.setDefaultRuleChainId(savedRuleChain.getId());
        doPost("/api/deviceProfile", deviceProfile).andExpect(status().isBadRequest())
                .andExpect(statusReason(containsString("Can't assign rule chain from different tenant!")));
    }

    @Test
    public void testSaveDeviceProfileWithDashboardFromDifferentTenant() throws Exception {
        loginDifferentTenant();
        Dashboard dashboard = new Dashboard();
        dashboard.setTitle("Different dashboard");
        Dashboard savedDashboard = doPost("/api/dashboard", dashboard, Dashboard.class);

        loginTenantAdmin();

        DeviceProfile deviceProfile = this.createDeviceProfile("Device Profile");
        deviceProfile.setDefaultDashboardId(savedDashboard.getId());
        doPost("/api/deviceProfile", deviceProfile).andExpect(status().isBadRequest())
                .andExpect(statusReason(containsString("Can't assign dashboard from different tenant!")));
    }

    @Test
    public void testSaveDeviceProfileWithFirmwareFromDifferentTenant() throws Exception {
        loginDifferentTenant();
        DeviceProfile differentProfile = createDeviceProfile("Different profile");
        differentProfile = doPost("/api/deviceProfile", differentProfile, DeviceProfile.class);
        SaveOtaPackageInfoRequest firmwareInfo = new SaveOtaPackageInfoRequest();
        firmwareInfo.setDeviceProfileId(differentProfile.getId());
        firmwareInfo.setType(FIRMWARE);
        firmwareInfo.setTitle("title");
        firmwareInfo.setVersion("1.0");
        firmwareInfo.setUrl("test.url");
        firmwareInfo.setUsesUrl(true);
        OtaPackageInfo savedFw = doPost("/api/otaPackage", firmwareInfo, OtaPackageInfo.class);

        loginTenantAdmin();

        DeviceProfile deviceProfile = this.createDeviceProfile("Device Profile");
        deviceProfile.setFirmwareId(savedFw.getId());
        doPost("/api/deviceProfile", deviceProfile).andExpect(status().isBadRequest())
                .andExpect(statusReason(containsString("Can't assign firmware from different tenant!")));
    }

    @Test
    public void testSaveDeviceProfileWithSoftwareFromDifferentTenant() throws Exception {
        loginDifferentTenant();
        DeviceProfile differentProfile = createDeviceProfile("Different profile");
        differentProfile = doPost("/api/deviceProfile", differentProfile, DeviceProfile.class);
        SaveOtaPackageInfoRequest softwareInfo = new SaveOtaPackageInfoRequest();
        softwareInfo.setDeviceProfileId(differentProfile.getId());
        softwareInfo.setType(SOFTWARE);
        softwareInfo.setTitle("title");
        softwareInfo.setVersion("1.0");
        softwareInfo.setUrl("test.url");
        softwareInfo.setUsesUrl(true);
        OtaPackageInfo savedSw = doPost("/api/otaPackage", softwareInfo, OtaPackageInfo.class);

        loginTenantAdmin();

        DeviceProfile deviceProfile = this.createDeviceProfile("Device Profile");
        deviceProfile.setSoftwareId(savedSw.getId());
        doPost("/api/deviceProfile", deviceProfile).andExpect(status().isBadRequest())
                .andExpect(statusReason(containsString("Can't assign software from different tenant!")));
    }

    @Test
    public void testDeleteDeviceProfile() throws Exception {
        DeviceProfile deviceProfile = this.createDeviceProfile("Device Profile");
        DeviceProfile savedDeviceProfile = doPost("/api/deviceProfile", deviceProfile, DeviceProfile.class);

        Mockito.reset(tbClusterService, auditLogService);

        doDelete("/api/deviceProfile/" + savedDeviceProfile.getId().getId().toString())
                .andExpect(status().isOk());

        String savedDeviceProfileIdFtr = savedDeviceProfile.getId().getId().toString();
        testNotifyEntityBroadcastEntityStateChangeEventOneTime(savedDeviceProfile, savedDeviceProfile.getId(), savedDeviceProfile.getId(),
                savedTenant.getId(), tenantAdmin.getCustomerId(), tenantAdmin.getId(), tenantAdmin.getEmail(),
                ActionType.DELETED, savedDeviceProfileIdFtr);

        doGet("/api/deviceProfile/" + savedDeviceProfile.getId().getId().toString())
                .andExpect(status().isNotFound())
                .andExpect(statusReason(containsString(msgErrorNoFound("Device profile", savedDeviceProfileIdFtr))));
    }

    @Test
    public void testFindDeviceProfiles() throws Exception {
        List<DeviceProfile> deviceProfiles = new ArrayList<>();
        PageLink pageLink = new PageLink(17);
        PageData<DeviceProfile> pageData = doGetTypedWithPageLink("/api/deviceProfiles?",
                new TypeReference<>() {
                }, pageLink);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertEquals(1, pageData.getTotalElements());
        deviceProfiles.addAll(pageData.getData());

        Mockito.reset(tbClusterService, auditLogService);

        int cntEntity = 28;
        for (int i = 0; i < cntEntity; i++) {
            DeviceProfile deviceProfile = this.createDeviceProfile("Device Profile" + i);
            deviceProfiles.add(doPost("/api/deviceProfile", deviceProfile, DeviceProfile.class));
        }

        testNotifyManyEntityManyTimeMsgToEdgeServiceEntityEqAny(new DeviceProfile(), new DeviceProfile(),
                savedTenant.getId(), tenantAdmin.getCustomerId(), tenantAdmin.getId(), tenantAdmin.getEmail(),
                ActionType.ADDED, ActionType.ADDED, cntEntity, cntEntity, cntEntity);
        Mockito.reset(tbClusterService, auditLogService);

        List<DeviceProfile> loadedDeviceProfiles = new ArrayList<>();
        pageLink = new PageLink(17);
        do {
            pageData = doGetTypedWithPageLink("/api/deviceProfiles?",
                    new TypeReference<>() {
                    }, pageLink);
            loadedDeviceProfiles.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageLink.nextPageLink();
            }
        } while (pageData.hasNext());

        Collections.sort(deviceProfiles, idComparator);
        Collections.sort(loadedDeviceProfiles, idComparator);

        Assert.assertEquals(deviceProfiles, loadedDeviceProfiles);

        for (DeviceProfile deviceProfile : loadedDeviceProfiles) {
            if (!deviceProfile.isDefault()) {
                doDelete("/api/deviceProfile/" + deviceProfile.getId().getId().toString())
                        .andExpect(status().isOk());
            }
        }

        testNotifyManyEntityManyTimeMsgToEdgeServiceEntityEqAny(loadedDeviceProfiles.get(0), loadedDeviceProfiles.get(0),
                savedTenant.getId(), tenantAdmin.getCustomerId(), tenantAdmin.getId(), tenantAdmin.getEmail(),
                ActionType.DELETED, ActionType.DELETED, cntEntity, cntEntity, cntEntity, loadedDeviceProfiles.get(0).getId().getId().toString());

        pageLink = new PageLink(17);
        pageData = doGetTypedWithPageLink("/api/deviceProfiles?",
                new TypeReference<>() {
                }, pageLink);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertEquals(1, pageData.getTotalElements());
    }

    @Test
    public void testFindDeviceProfileInfos() throws Exception {
        List<DeviceProfile> deviceProfiles = new ArrayList<>();
        PageLink pageLink = new PageLink(17);
        PageData<DeviceProfile> deviceProfilePageData = doGetTypedWithPageLink("/api/deviceProfiles?",
                new TypeReference<PageData<DeviceProfile>>() {
                }, pageLink);
        Assert.assertFalse(deviceProfilePageData.hasNext());
        Assert.assertEquals(1, deviceProfilePageData.getTotalElements());
        deviceProfiles.addAll(deviceProfilePageData.getData());

        for (int i = 0; i < 28; i++) {
            DeviceProfile deviceProfile = this.createDeviceProfile("Device Profile" + i);
            deviceProfiles.add(doPost("/api/deviceProfile", deviceProfile, DeviceProfile.class));
        }

        List<DeviceProfileInfo> loadedDeviceProfileInfos = new ArrayList<>();
        pageLink = new PageLink(17);
        PageData<DeviceProfileInfo> pageData;
        do {
            pageData = doGetTypedWithPageLink("/api/deviceProfileInfos?",
                    new TypeReference<>() {
                    }, pageLink);
            loadedDeviceProfileInfos.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageLink.nextPageLink();
            }
        } while (pageData.hasNext());

        Collections.sort(deviceProfiles, idComparator);
        Collections.sort(loadedDeviceProfileInfos, deviceProfileInfoIdComparator);

        List<DeviceProfileInfo> deviceProfileInfos = deviceProfiles.stream().map(deviceProfile -> new DeviceProfileInfo(deviceProfile.getId(),
                deviceProfile.getName(), deviceProfile.getImage(), deviceProfile.getDefaultDashboardId(),
                deviceProfile.getType(), deviceProfile.getTransportType())).collect(Collectors.toList());

        Assert.assertEquals(deviceProfileInfos, loadedDeviceProfileInfos);

        for (DeviceProfile deviceProfile : deviceProfiles) {
            if (!deviceProfile.isDefault()) {
                doDelete("/api/deviceProfile/" + deviceProfile.getId().getId().toString())
                        .andExpect(status().isOk());
            }
        }

        pageLink = new PageLink(17);
        pageData = doGetTypedWithPageLink("/api/deviceProfileInfos?",
                new TypeReference<PageData<DeviceProfileInfo>>() {
                }, pageLink);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertEquals(1, pageData.getTotalElements());
    }

    @Test
    public void testSaveProtoDeviceProfileWithInvalidProtoFile() throws Exception {
        testSaveDeviceProfileWithInvalidProtoSchema("syntax = \"proto3\";\n" +
                "\n" +
                "package schemavalidation;\n" +
                "\n" +
                "message SchemaValidationTest {\n" +
                "   required int32 parameter = 1;\n" +
                "}", "[Transport Configuration] failed to parse attributes proto schema due to: Syntax error in :6:4: 'required' label forbidden in proto3 field declarations");
    }

    @Test
    public void testSaveProtoDeviceProfileWithInvalidProtoSyntax() throws Exception {
        testSaveDeviceProfileWithInvalidProtoSchema("syntax = \"proto2\";\n" +
                "\n" +
                "package schemavalidation;\n" +
                "\n" +
                "message SchemaValidationTest {\n" +
                "   required int32 parameter = 1;\n" +
                "}", "[Transport Configuration] invalid schema syntax: proto2 for attributes proto schema provided! Only proto3 allowed!");
    }

    @Test
    public void testSaveProtoDeviceProfileOptionsNotSupported() throws Exception {
        testSaveDeviceProfileWithInvalidProtoSchema("syntax = \"proto3\";\n" +
                "\n" +
                "option java_package = \"com.test.schemavalidation\";\n" +
                "option java_multiple_files = true;\n" +
                "\n" +
                "package schemavalidation;\n" +
                "\n" +
                "message SchemaValidationTest {\n" +
                "   optional int32 parameter = 1;\n" +
                "}", "[Transport Configuration] invalid attributes proto schema provided! Schema options don't support!");
    }

    @Test
    public void testSaveProtoDeviceProfilePublicImportsNotSupported() throws Exception {
        testSaveDeviceProfileWithInvalidProtoSchema("syntax = \"proto3\";\n" +
                "\n" +
                "import public \"oldschema.proto\";\n" +
                "\n" +
                "package schemavalidation;\n" +
                "\n" +
                "message SchemaValidationTest {\n" +
                "   optional int32 parameter = 1;\n" +
                "}", "[Transport Configuration] invalid attributes proto schema provided! Schema public imports don't support!");
    }

    @Test
    public void testSaveProtoDeviceProfileImportsNotSupported() throws Exception {
        testSaveDeviceProfileWithInvalidProtoSchema("syntax = \"proto3\";\n" +
                "\n" +
                "import \"oldschema.proto\";\n" +
                "\n" +
                "package schemavalidation;\n" +
                "\n" +
                "message SchemaValidationTest {\n" +
                "   optional int32 parameter = 1;\n" +
                "}", "[Transport Configuration] invalid attributes proto schema provided! Schema imports don't support!");
    }

    @Test
    public void testSaveProtoDeviceProfileExtendDeclarationsNotSupported() throws Exception {
        testSaveDeviceProfileWithInvalidProtoSchema("syntax = \"proto3\";\n" +
                "\n" +
                "package schemavalidation;\n" +
                "\n" +
                "extend google.protobuf.MethodOptions {\n" +
                "  MyMessage my_method_option = 50007;\n" +
                "}", "[Transport Configuration] invalid attributes proto schema provided! Schema extend declarations don't support!");
    }

    @Test
    public void testSaveProtoDeviceProfileEnumOptionsNotSupported() throws Exception {
        testSaveDeviceProfileWithInvalidProtoSchema("syntax = \"proto3\";\n" +
                "\n" +
                "package schemavalidation;\n" +
                "\n" +
                "enum testEnum {\n" +
                "   option allow_alias = true;\n" +
                "   DEFAULT = 0;\n" +
                "   STARTED = 1;\n" +
                "   RUNNING = 2;\n" +
                "}\n" +
                "\n" +
                "message testMessage {\n" +
                "   optional int32 parameter = 1;\n" +
                "}", "[Transport Configuration] invalid attributes proto schema provided! Enum definitions options are not supported!");
    }

    @Test
    public void testSaveProtoDeviceProfileNoOneMessageTypeExists() throws Exception {
        testSaveDeviceProfileWithInvalidProtoSchema("syntax = \"proto3\";\n" +
                "\n" +
                "package schemavalidation;\n" +
                "\n" +
                "enum testEnum {\n" +
                "   DEFAULT = 0;\n" +
                "   STARTED = 1;\n" +
                "   RUNNING = 2;\n" +
                "}", "[Transport Configuration] invalid attributes proto schema provided! At least one Message definition should exists!");
    }

    @Test
    public void testSaveProtoDeviceProfileMessageTypeOptionsNotSupported() throws Exception {
        testSaveDeviceProfileWithInvalidProtoSchema("syntax = \"proto3\";\n" +
                "\n" +
                "package schemavalidation;\n" +
                "\n" +
                "message testMessage {\n" +
                "   option allow_alias = true;\n" +
                "   optional int32 parameter = 1;\n" +
                "}", "[Transport Configuration] invalid attributes proto schema provided! Message definition options don't support!");
    }

    @Test
    public void testSaveProtoDeviceProfileMessageTypeExtensionsNotSupported() throws Exception {
        testSaveDeviceProfileWithInvalidProtoSchema("syntax = \"proto3\";\n" +
                "\n" +
                "package schemavalidation;\n" +
                "\n" +
                "message TestMessage {\n" +
                "   extensions 100 to 199;\n" +
                "}", "[Transport Configuration] invalid attributes proto schema provided! Message definition extensions don't support!");
    }

    @Test
    public void testSaveProtoDeviceProfileMessageTypeReservedElementsNotSupported() throws Exception {
        testSaveDeviceProfileWithInvalidProtoSchema("syntax = \"proto3\";\n" +
                "\n" +
                "package schemavalidation;\n" +
                "\n" +
                "message Foo {\n" +
                "  reserved 2, 15, 9 to 11;\n" +
                "  reserved \"foo\", \"bar\";\n" +
                "}", "[Transport Configuration] invalid attributes proto schema provided! Message definition reserved elements don't support!");
    }

    @Test
    public void testSaveProtoDeviceProfileMessageTypeGroupsElementsNotSupported() throws Exception {
        testSaveDeviceProfileWithInvalidProtoSchema("syntax = \"proto3\";\n" +
                "\n" +
                "package schemavalidation;\n" +
                "\n" +
                "message TestMessage {\n" +
                "  repeated group Result = 1 {\n" +
                "    optional string url = 2;\n" +
                "    optional string title = 3;\n" +
                "    repeated string snippets = 4;\n" +
                "  }\n" +
                "}", "[Transport Configuration] invalid attributes proto schema provided! Message definition groups don't support!");
    }

    @Test
    public void testSaveProtoDeviceProfileOneOfsGroupsElementsNotSupported() throws Exception {
        testSaveDeviceProfileWithInvalidProtoSchema("syntax = \"proto3\";\n" +
                "\n" +
                "package schemavalidation;\n" +
                "\n" +
                "message SampleMessage {\n" +
                "  oneof test_oneof {\n" +
                "     string name = 1;\n" +
                "     group Result = 2 {\n" +
                "    \tstring url = 3;\n" +
                "    \tstring title = 4;\n" +
                "    \trepeated string snippets = 5;\n" +
                "     }\n" +
                "  }" +
                "}", "[Transport Configuration] invalid attributes proto schema provided! OneOf definition groups don't support!");
    }

    @Test
    public void testSaveProtoDeviceProfileWithMessageNestedTypes() throws Exception {
        String schema = "syntax = \"proto3\";\n" +
                "\n" +
                "package testnested;\n" +
                "\n" +
                "message Outer {\n" +
                "  message MiddleAA {\n" +
                "    message Inner {\n" +
                "      optional int64 ival = 1;\n" +
                "      optional bool  booly = 2;\n" +
                "    }\n" +
                "    Inner inner = 1;\n" +
                "  }\n" +
                "  message MiddleBB {\n" +
                "    message Inner {\n" +
                "      optional int32 ival = 1;\n" +
                "      optional bool  booly = 2;\n" +
                "    }\n" +
                "    Inner inner = 1;\n" +
                "  }\n" +
                "  MiddleAA middleAA = 1;\n" +
                "  MiddleBB middleBB = 2;\n" +
                "}";
        DynamicSchema dynamicSchema = getDynamicSchema(schema);
        assertNotNull(dynamicSchema);
        Set<String> messageTypes = dynamicSchema.getMessageTypes();
        assertEquals(5, messageTypes.size());
        assertTrue(messageTypes.contains("testnested.Outer"));
        assertTrue(messageTypes.contains("testnested.Outer.MiddleAA"));
        assertTrue(messageTypes.contains("testnested.Outer.MiddleAA.Inner"));
        assertTrue(messageTypes.contains("testnested.Outer.MiddleBB"));
        assertTrue(messageTypes.contains("testnested.Outer.MiddleBB.Inner"));

        DynamicMessage.Builder middleAAInnerMsgBuilder = dynamicSchema.newMessageBuilder("testnested.Outer.MiddleAA.Inner");
        Descriptors.Descriptor middleAAInnerMsgDescriptor = middleAAInnerMsgBuilder.getDescriptorForType();
        DynamicMessage middleAAInnerMsg = middleAAInnerMsgBuilder
                .setField(middleAAInnerMsgDescriptor.findFieldByName("ival"), 1L)
                .setField(middleAAInnerMsgDescriptor.findFieldByName("booly"), true)
                .build();

        DynamicMessage.Builder middleAAMsgBuilder = dynamicSchema.newMessageBuilder("testnested.Outer.MiddleAA");
        Descriptors.Descriptor middleAAMsgDescriptor = middleAAMsgBuilder.getDescriptorForType();
        DynamicMessage middleAAMsg = middleAAMsgBuilder
                .setField(middleAAMsgDescriptor.findFieldByName("inner"), middleAAInnerMsg)
                .build();

        DynamicMessage.Builder middleBBInnerMsgBuilder = dynamicSchema.newMessageBuilder("testnested.Outer.MiddleAA.Inner");
        Descriptors.Descriptor middleBBInnerMsgDescriptor = middleBBInnerMsgBuilder.getDescriptorForType();
        DynamicMessage middleBBInnerMsg = middleBBInnerMsgBuilder
                .setField(middleBBInnerMsgDescriptor.findFieldByName("ival"), 0L)
                .setField(middleBBInnerMsgDescriptor.findFieldByName("booly"), false)
                .build();

        DynamicMessage.Builder middleBBMsgBuilder = dynamicSchema.newMessageBuilder("testnested.Outer.MiddleBB");
        Descriptors.Descriptor middleBBMsgDescriptor = middleBBMsgBuilder.getDescriptorForType();
        DynamicMessage middleBBMsg = middleBBMsgBuilder
                .setField(middleBBMsgDescriptor.findFieldByName("inner"), middleBBInnerMsg)
                .build();


        DynamicMessage.Builder outerMsgBuilder = dynamicSchema.newMessageBuilder("testnested.Outer");
        Descriptors.Descriptor outerMsgBuilderDescriptor = outerMsgBuilder.getDescriptorForType();
        DynamicMessage outerMsg = outerMsgBuilder
                .setField(outerMsgBuilderDescriptor.findFieldByName("middleAA"), middleAAMsg)
                .setField(outerMsgBuilderDescriptor.findFieldByName("middleBB"), middleBBMsg)
                .build();

        assertEquals("{\n" +
                "  \"middleAA\": {\n" +
                "    \"inner\": {\n" +
                "      \"ival\": \"1\",\n" +
                "      \"booly\": true\n" +
                "    }\n" +
                "  },\n" +
                "  \"middleBB\": {\n" +
                "    \"inner\": {\n" +
                "      \"ival\": 0,\n" +
                "      \"booly\": false\n" +
                "    }\n" +
                "  }\n" +
                "}", dynamicMsgToJson(outerMsgBuilderDescriptor, outerMsg.toByteArray()));
    }

    @Test
    public void testSaveProtoDeviceProfileWithMessageOneOfs() throws Exception {
        String schema = "syntax = \"proto3\";\n" +
                "\n" +
                "package testoneofs;\n" +
                "\n" +
                "message SubMessage {\n" +
                "   repeated string name = 1;\n" +
                "}\n" +
                "\n" +
                "message SampleMessage {\n" +
                "  optional int32 id = 1;\n" +
                "  oneof testOneOf {\n" +
                "     string name = 4;\n" +
                "     SubMessage subMessage = 9;\n" +
                "  }\n" +
                "}";
        DynamicSchema dynamicSchema = getDynamicSchema(schema);
        assertNotNull(dynamicSchema);
        Set<String> messageTypes = dynamicSchema.getMessageTypes();
        assertEquals(2, messageTypes.size());
        assertTrue(messageTypes.contains("testoneofs.SubMessage"));
        assertTrue(messageTypes.contains("testoneofs.SampleMessage"));

        DynamicMessage.Builder sampleMsgBuilder = dynamicSchema.newMessageBuilder("testoneofs.SampleMessage");
        Descriptors.Descriptor sampleMsgDescriptor = sampleMsgBuilder.getDescriptorForType();
        assertNotNull(sampleMsgDescriptor);

        List<Descriptors.FieldDescriptor> fields = sampleMsgDescriptor.getFields();
        assertEquals(3, fields.size());
        DynamicMessage sampleMsg = sampleMsgBuilder
                .setField(sampleMsgDescriptor.findFieldByName("name"), "Bob")
                .build();
        assertEquals("{\n" + "  \"name\": \"Bob\"\n" + "}", dynamicMsgToJson(sampleMsgDescriptor, sampleMsg.toByteArray()));

        DynamicMessage.Builder subMsgBuilder = dynamicSchema.newMessageBuilder("testoneofs.SubMessage");
        Descriptors.Descriptor subMsgDescriptor = subMsgBuilder.getDescriptorForType();
        DynamicMessage subMsg = subMsgBuilder
                .addRepeatedField(subMsgDescriptor.findFieldByName("name"), "Alice")
                .addRepeatedField(subMsgDescriptor.findFieldByName("name"), "John")
                .build();

        DynamicMessage sampleMsgWithOneOfSubMessage = sampleMsgBuilder.setField(sampleMsgDescriptor.findFieldByName("subMessage"), subMsg).build();
        assertEquals("{\n" + "  \"subMessage\": {\n" + "    \"name\": [\"Alice\", \"John\"]\n" + "  }\n" + "}",
                dynamicMsgToJson(sampleMsgDescriptor, sampleMsgWithOneOfSubMessage.toByteArray()));
    }

    @Test
    public void testSaveProtoDeviceProfileWithInvalidTelemetrySchemaTsField() throws Exception {
        testSaveDeviceProfileWithInvalidProtoSchema("syntax =\"proto3\";\n" +
                "\n" +
                "package schemavalidation;\n" +
                "\n" +
                "message PostTelemetry {\n" +
                "  int64 ts = 1;\n" +
                "  Values values = 2;\n" +
                "  \n" +
                "  message Values {\n" +
                "    string key1 = 3;\n" +
                "    bool key2 = 4;\n" +
                "    double key3 = 5;\n" +
                "    int32 key4 = 6;\n" +
                "    JsonObject key5 = 7;\n" +
                "  }\n" +
                "  \n" +
                "  message JsonObject {\n" +
                "    optional int32 someNumber = 8;\n" +
                "    repeated int32 someArray = 9;\n" +
                "    NestedJsonObject someNestedObject = 10;\n" +
                "    message NestedJsonObject {\n" +
                "       optional string key = 11;\n" +
                "    }\n" +
                "  }\n" +
                "}", "[Transport Configuration] invalid telemetry proto schema provided! Field 'ts' has invalid label. Field 'ts' should have optional keyword!");
    }

    @Test
    public void testSaveProtoDeviceProfileWithInvalidTelemetrySchemaTsDateType() throws Exception {
        testSaveDeviceProfileWithInvalidProtoSchema("syntax =\"proto3\";\n" +
                "\n" +
                "package schemavalidation;\n" +
                "\n" +
                "message PostTelemetry {\n" +
                "  optional int32 ts = 1;\n" +
                "  Values values = 2;\n" +
                "  \n" +
                "  message Values {\n" +
                "    string key1 = 3;\n" +
                "    bool key2 = 4;\n" +
                "    double key3 = 5;\n" +
                "    int32 key4 = 6;\n" +
                "    JsonObject key5 = 7;\n" +
                "  }\n" +
                "  \n" +
                "  message JsonObject {\n" +
                "    optional int32 someNumber = 8;\n" +
                "  }\n" +
                "}", "[Transport Configuration] invalid telemetry proto schema provided! Field 'ts' has invalid data type. Only int64 type is supported!");
    }

    @Test
    public void testSaveProtoDeviceProfileWithInvalidTelemetrySchemaValuesDateType() throws Exception {
        testSaveDeviceProfileWithInvalidProtoSchema("syntax =\"proto3\";\n" +
                "\n" +
                "package schemavalidation;\n" +
                "\n" +
                "message PostTelemetry {\n" +
                "  optional int64 ts = 1;\n" +
                "  string values = 2;\n" +
                "  \n" +
                "}", "[Transport Configuration] invalid telemetry proto schema provided! Field 'values' has invalid data type. Only message type is supported!");
    }

    @Test
    public void testSaveProtoDeviceProfileWithInvalidRpcRequestSchemaMethodDateType() throws Exception {
        testSaveDeviceProfileWithInvalidRpcRequestProtoSchema("syntax =\"proto3\";\n" +
                "\n" +
                "package schemavalidation;\n" +
                "\n" +
                "message RpcRequestMsg {\n" +
                "  optional int32 method = 1;\n" +
                "  optional int32 requestId = 2;\n" +
                "  optional string params = 3;\n" +
                "  \n" +
                "}", "[Transport Configuration] invalid rpc request proto schema provided! Field 'method' has invalid data type. Only string type is supported!");
    }

    @Test
    public void testSaveProtoDeviceProfileWithInvalidRpcRequestSchemaRequestIdDateType() throws Exception {
        testSaveDeviceProfileWithInvalidRpcRequestProtoSchema("syntax =\"proto3\";\n" +
                "\n" +
                "package schemavalidation;\n" +
                "\n" +
                "message RpcRequestMsg {\n" +
                "  optional string method = 1;\n" +
                "  optional int64 requestId = 2;\n" +
                "  optional string params = 3;\n" +
                "  \n" +
                "}", "[Transport Configuration] invalid rpc request proto schema provided! Field 'requestId' has invalid data type. Only int32 type is supported!");
    }

    @Test
    public void testSaveProtoDeviceProfileWithInvalidRpcRequestSchemaMethodLabel() throws Exception {
        testSaveDeviceProfileWithInvalidRpcRequestProtoSchema("syntax =\"proto3\";\n" +
                "\n" +
                "package schemavalidation;\n" +
                "\n" +
                "message RpcRequestMsg {\n" +
                "  repeated string method = 1;\n" +
                "  optional int32 requestId = 2;\n" +
                "  optional string params = 3;\n" +
                "  \n" +
                "}", "[Transport Configuration] invalid rpc request proto schema provided! Field 'method' has invalid label!");
    }

    @Test
    public void testSaveProtoDeviceProfileWithInvalidRpcRequestSchemaRequestIdLabel() throws Exception {
        testSaveDeviceProfileWithInvalidRpcRequestProtoSchema("syntax =\"proto3\";\n" +
                "\n" +
                "package schemavalidation;\n" +
                "\n" +
                "message RpcRequestMsg {\n" +
                "  optional string method = 1;\n" +
                "  repeated int32 requestId = 2;\n" +
                "  optional string params = 3;\n" +
                "  \n" +
                "}", "[Transport Configuration] invalid rpc request proto schema provided! Field 'requestId' has invalid label!");
    }

    @Test
    public void testSaveProtoDeviceProfileWithInvalidRpcRequestSchemaParamsLabel() throws Exception {
        testSaveDeviceProfileWithInvalidRpcRequestProtoSchema("syntax =\"proto3\";\n" +
                "\n" +
                "package schemavalidation;\n" +
                "\n" +
                "message RpcRequestMsg {\n" +
                "  optional string method = 1;\n" +
                "  optional int32 requestId = 2;\n" +
                "  repeated string params = 3;\n" +
                "  \n" +
                "}", "[Transport Configuration] invalid rpc request proto schema provided! Field 'params' has invalid label!");
    }

    @Test
    public void testSaveProtoDeviceProfileWithInvalidRpcRequestSchemaFieldsCount() throws Exception {
        testSaveDeviceProfileWithInvalidRpcRequestProtoSchema("syntax =\"proto3\";\n" +
                "\n" +
                "package schemavalidation;\n" +
                "\n" +
                "message RpcRequestMsg {\n" +
                "  optional int32 requestId = 2;\n" +
                "  optional string params = 3;\n" +
                "  \n" +
                "}", "[Transport Configuration] invalid rpc request proto schema provided! RpcRequestMsg message should always contains 3 fields: method, requestId and params!");
    }

    @Test
    public void testSaveProtoDeviceProfileWithInvalidRpcRequestSchemaFieldMethodIsNoSet() throws Exception {
        testSaveDeviceProfileWithInvalidRpcRequestProtoSchema("syntax =\"proto3\";\n" +
                "\n" +
                "package schemavalidation;\n" +
                "\n" +
                "message RpcRequestMsg {\n" +
                "  optional string methodName = 1;\n" +
                "  optional int32 requestId = 2;\n" +
                "  optional string params = 3;\n" +
                "  \n" +
                "}", "[Transport Configuration] invalid rpc request proto schema provided! Failed to get field descriptor for field: method!");
    }

    @Test
    public void testSaveProtoDeviceProfileWithInvalidRpcRequestSchemaFieldRequestIdIsNotSet() throws Exception {
        testSaveDeviceProfileWithInvalidRpcRequestProtoSchema("syntax =\"proto3\";\n" +
                "\n" +
                "package schemavalidation;\n" +
                "\n" +
                "message RpcRequestMsg {\n" +
                "  optional string method = 1;\n" +
                "  optional int32 requestIdentifier = 2;\n" +
                "  optional string params = 3;\n" +
                "  \n" +
                "}", "[Transport Configuration] invalid rpc request proto schema provided! Failed to get field descriptor for field: requestId!");
    }

    @Test
    public void testSaveProtoDeviceProfileWithInvalidRpcRequestSchemaFieldParamsIsNotSet() throws Exception {
        testSaveDeviceProfileWithInvalidRpcRequestProtoSchema("syntax =\"proto3\";\n" +
                "\n" +
                "package schemavalidation;\n" +
                "\n" +
                "message RpcRequestMsg {\n" +
                "  optional string method = 1;\n" +
                "  optional int32 requestId = 2;\n" +
                "  optional string parameters = 3;\n" +
                "  \n" +
                "}", "[Transport Configuration] invalid rpc request proto schema provided! Failed to get field descriptor for field: params!");
    }

    @Test
    public void testSaveDeviceProfileWithSendAckOnValidationException() throws Exception {
        JsonTransportPayloadConfiguration jsonTransportPayloadConfiguration = new JsonTransportPayloadConfiguration();
        MqttDeviceProfileTransportConfiguration mqttDeviceProfileTransportConfiguration = this.createMqttDeviceProfileTransportConfiguration(jsonTransportPayloadConfiguration, true);
        DeviceProfile deviceProfile = this.createDeviceProfile("Device Profile", mqttDeviceProfileTransportConfiguration);
        DeviceProfile savedDeviceProfile = doPost("/api/deviceProfile", deviceProfile, DeviceProfile.class);
        Assert.assertNotNull(savedDeviceProfile);
        Assert.assertEquals(savedDeviceProfile.getTransportType(), DeviceTransportType.MQTT);
        Assert.assertTrue(savedDeviceProfile.getProfileData().getTransportConfiguration() instanceof MqttDeviceProfileTransportConfiguration);
        MqttDeviceProfileTransportConfiguration transportConfiguration = (MqttDeviceProfileTransportConfiguration) savedDeviceProfile.getProfileData().getTransportConfiguration();
        Assert.assertTrue(transportConfiguration.isSendAckOnValidationException());
        DeviceProfile foundDeviceProfile = doGet("/api/deviceProfile/" + savedDeviceProfile.getId().getId().toString(), DeviceProfile.class);
        Assert.assertEquals(savedDeviceProfile, foundDeviceProfile);
    }

    private DeviceProfile testSaveDeviceProfileWithProtoPayloadType(String schema) throws Exception {
        ProtoTransportPayloadConfiguration protoTransportPayloadConfiguration = this.createProtoTransportPayloadConfiguration(schema, schema, null, null);
        MqttDeviceProfileTransportConfiguration mqttDeviceProfileTransportConfiguration = this.createMqttDeviceProfileTransportConfiguration(protoTransportPayloadConfiguration, false);
        DeviceProfile deviceProfile = this.createDeviceProfile("Device Profile", mqttDeviceProfileTransportConfiguration);
        DeviceProfile savedDeviceProfile = doPost("/api/deviceProfile", deviceProfile, DeviceProfile.class);
        Assert.assertNotNull(savedDeviceProfile);
        DeviceProfile foundDeviceProfile = doGet("/api/deviceProfile/" + savedDeviceProfile.getId().getId().toString(), DeviceProfile.class);
        Assert.assertEquals(savedDeviceProfile, foundDeviceProfile);
        return savedDeviceProfile;
    }

    private void testSaveDeviceProfileWithInvalidProtoSchema(String schema, String errorMsg) throws Exception {
        ProtoTransportPayloadConfiguration protoTransportPayloadConfiguration = this.createProtoTransportPayloadConfiguration(schema, schema, null, null);
        MqttDeviceProfileTransportConfiguration mqttDeviceProfileTransportConfiguration = this.createMqttDeviceProfileTransportConfiguration(protoTransportPayloadConfiguration, false);
        DeviceProfile deviceProfile = this.createDeviceProfile("Device Profile", mqttDeviceProfileTransportConfiguration);

        Mockito.reset(tbClusterService, auditLogService);

        doPost("/api/deviceProfile", deviceProfile)
                .andExpect(status().isBadRequest())
                .andExpect(statusReason(containsString(errorMsg)));

        testNotifyEntityEqualsOneTimeServiceNeverError(deviceProfile, savedTenant.getId(),
                tenantAdmin.getId(), tenantAdmin.getEmail(), ActionType.ADDED, new DataValidationException(errorMsg));
    }

    private void testSaveDeviceProfileWithInvalidRpcRequestProtoSchema(String schema, String errorMsg) throws Exception {
        ProtoTransportPayloadConfiguration protoTransportPayloadConfiguration = this.createProtoTransportPayloadConfiguration(schema, schema, schema, null);
        MqttDeviceProfileTransportConfiguration mqttDeviceProfileTransportConfiguration = this.createMqttDeviceProfileTransportConfiguration(protoTransportPayloadConfiguration, false);
        DeviceProfile deviceProfile = this.createDeviceProfile("Device Profile", mqttDeviceProfileTransportConfiguration);

        Mockito.reset(tbClusterService, auditLogService);

        doPost("/api/deviceProfile", deviceProfile)
                .andExpect(status().isBadRequest())
                .andExpect(statusReason(containsString(errorMsg)));

        testNotifyEntityEqualsOneTimeServiceNeverError(deviceProfile, savedTenant.getId(),
                tenantAdmin.getId(), tenantAdmin.getEmail(), ActionType.ADDED, new DataValidationException(errorMsg));
    }

    private DynamicSchema getDynamicSchema(String schema) throws Exception {
        DeviceProfile deviceProfile = testSaveDeviceProfileWithProtoPayloadType(schema);
        DeviceProfileTransportConfiguration transportConfiguration = deviceProfile.getProfileData().getTransportConfiguration();
        assertTrue(transportConfiguration instanceof MqttDeviceProfileTransportConfiguration);
        MqttDeviceProfileTransportConfiguration mqttDeviceProfileTransportConfiguration = (MqttDeviceProfileTransportConfiguration) transportConfiguration;
        TransportPayloadTypeConfiguration transportPayloadTypeConfiguration = mqttDeviceProfileTransportConfiguration.getTransportPayloadTypeConfiguration();
        assertTrue(transportPayloadTypeConfiguration instanceof ProtoTransportPayloadConfiguration);
        ProtoTransportPayloadConfiguration protoTransportPayloadConfiguration = (ProtoTransportPayloadConfiguration) transportPayloadTypeConfiguration;
        ProtoFileElement protoFile = protoTransportPayloadConfiguration.getTransportProtoSchema(schema);
        return protoTransportPayloadConfiguration.getDynamicSchema(protoFile, ProtoTransportPayloadConfiguration.ATTRIBUTES_PROTO_SCHEMA);
    }

    private String dynamicMsgToJson(Descriptors.Descriptor descriptor, byte[] payload) throws InvalidProtocolBufferException {
        DynamicMessage dynamicMessage = DynamicMessage.parseFrom(descriptor, payload);
        return JsonFormat.printer().includingDefaultValueFields().print(dynamicMessage);
    }

}