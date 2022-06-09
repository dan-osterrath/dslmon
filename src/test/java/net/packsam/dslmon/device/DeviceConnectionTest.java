package net.packsam.dslmon.device;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@WireMockTest
public class DeviceConnectionTest {

    @Test
    public void testReadTR64DeviceSpec(WireMockRuntimeInfo wmRuntimeInfo) throws IOException {
        var deviceConnection = new DeviceConnection("localhost", wmRuntimeInfo.getHttpPort());
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
        var deviceConnection = new DeviceConnection("localhost", wmRuntimeInfo.getHttpPort());
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
}
