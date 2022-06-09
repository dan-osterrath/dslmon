package net.packsam.dslmon.device;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import net.packsam.dslmon.device.jaxb.tr64.device.Service;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@WireMockTest
public class FritzBoxConnectorTest {

    @Test
    public void testReadTR64DeviceSpec(WireMockRuntimeInfo wmRuntimeInfo) throws IOException {
        var deviceConnection = new FritzBoxConnector("localhost", wmRuntimeInfo.getHttpPort());
        var root = deviceConnection.readTR64DeviceSpec();
        assertNotNull(root);

        var specVersion = root.getSpecVersion();
        assertNotNull(specVersion);
        assertEquals(1, specVersion.getMajor());
        assertEquals(0, specVersion.getMinor());

        var systemVersion = root.getSystemVersion();
        assertNotNull(systemVersion);
        assertEquals(185L, systemVersion.getHW());
        assertEquals(113, systemVersion.getMajor());
        assertEquals(7, systemVersion.getMinor());
        assertEquals(29, systemVersion.getPatch());
        assertEquals(92201L, systemVersion.getBuildnumber());
        assertEquals("113.07.29", systemVersion.getDisplay());

        var device = root.getDevice();
        assertNotNull(device);
        assertEquals("urn:dslforum-org:device:InternetGatewayDevice:1", device.getDeviceType());
        assertEquals("uuid:4ee007a5-9cd8-41ec-a908-499ae2ae059e", device.getUDN());

        var serviceList = device.getServiceList();
        assertNotNull(serviceList);
        assertEquals(23, serviceList.getService().size());

        var deviceList = device.getDeviceList();
        assertNotNull(deviceList);
        assertEquals(2, deviceList.getDevice().size());
    }

    @Test
    public void testReadIGDDeviceSpec(WireMockRuntimeInfo wmRuntimeInfo) throws IOException {
        var deviceConnection = new FritzBoxConnector("localhost", wmRuntimeInfo.getHttpPort());
        var root = deviceConnection.readIGDDeviceSpec();
        assertNotNull(root);

        var specVersion = root.getSpecVersion();
        assertNotNull(specVersion);
        assertEquals(1, specVersion.getMajor());
        assertEquals(0, specVersion.getMinor());

        var device = root.getDevice();
        assertNotNull(device);
        assertEquals("urn:schemas-upnp-org:device:InternetGatewayDevice:1", device.getDeviceType());
        assertEquals("uuid:4ee007a5-9cd8-41ec-a908-499ae2ae059e", device.getUDN());

        var serviceList = device.getServiceList();
        assertNotNull(serviceList);
        assertEquals(1, serviceList.getService().size());

        var deviceList = device.getDeviceList();
        assertNotNull(deviceList);
        assertEquals(1, deviceList.getDevice().size());
    }

    @Test
    public void testReadTR64ServiceSpec(WireMockRuntimeInfo wmRuntimeInfo) throws IOException {
        var deviceConnection = new FritzBoxConnector("localhost", wmRuntimeInfo.getHttpPort());

        var service = new Service();
        service.setServiceType("urn:dslforum-org:service:DeviceInfo:1");
        service.setServiceId("urn:DeviceInfo-com:serviceId:DeviceInfo1");
        service.setControlURL("/upnp/control/deviceinfo");
        service.setEventSubURL("/upnp/control/deviceinfo");
        service.setSCPDURL("/deviceinfoSCPD.xml");

        var scpd = deviceConnection.readTR64ServiceSpec(service);
        assertNotNull(scpd);

        var specVersion = scpd.getSpecVersion();
        assertNotNull(specVersion);
        assertEquals(1, specVersion.getMajor());
        assertEquals(0, specVersion.getMinor());

        var actionList = scpd.getActionList();
        assertNotNull(actionList);
        assertEquals(4, actionList.getAction().size());

        var serviceStateTable = scpd.getServiceStateTable();
        assertNotNull(serviceStateTable);
        assertEquals(13, serviceStateTable.getStateVariable().size());
    }

    @Test
    public void testReadIGDServiceSpec(WireMockRuntimeInfo wmRuntimeInfo) throws IOException {
        var deviceConnection = new FritzBoxConnector("localhost", wmRuntimeInfo.getHttpPort());

        var service = new net.packsam.dslmon.device.jaxb.igd.device.Service();
        service.setServiceType("urn:schemas-upnp-org:service:WANCommonInterfaceConfig:1");
        service.setServiceId("urn:upnp-org:serviceId:WANCommonIFC1");
        service.setControlURL("/igdupnp/control/WANCommonIFC1");
        service.setEventSubURL("/igdupnp/control/WANCommonIFC1");
        service.setSCPDURL("/igdicfgSCPD.xml");

        var scpd = deviceConnection.readIGDServiceSpec(service);
        assertNotNull(scpd);

        var specVersion = scpd.getSpecVersion();
        assertNotNull(specVersion);
        assertEquals(1, specVersion.getMajor());
        assertEquals(0, specVersion.getMinor());

        var actionList = scpd.getActionList();
        assertNotNull(actionList);
        assertEquals(8, actionList.getAction().size());

        var serviceStateTable = scpd.getServiceStateTable();
        assertNotNull(serviceStateTable);
        assertEquals(29, serviceStateTable.getStateVariable().size());
    }
}
