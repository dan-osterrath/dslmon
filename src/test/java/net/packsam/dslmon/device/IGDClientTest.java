package net.packsam.dslmon.device;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import net.packsam.dslmon.device.jaxb.igd.device.Service;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@WireMockTest
public class IGDClientTest {
	@Test
	public void testReadDeviceSpec(WireMockRuntimeInfo wmRuntimeInfo) throws IOException {
		var client = new IGDClient("localhost", wmRuntimeInfo.getHttpPort());
		var root = client.readDeviceSpec();
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
	public void testReadServiceSpec(WireMockRuntimeInfo wmRuntimeInfo) throws IOException {
		var client = new IGDClient("localhost", wmRuntimeInfo.getHttpPort());

		var service = new Service();
		service.setServiceType("urn:schemas-upnp-org:service:WANCommonInterfaceConfig:1");
		service.setServiceId("urn:upnp-org:serviceId:WANCommonIFC1");
		service.setControlURL("/igdupnp/control/WANCommonIFC1");
		service.setEventSubURL("/igdupnp/control/WANCommonIFC1");
		service.setSCPDURL("/igdicfgSCPD.xml");

		var scpd = client.readServiceSpec(service);
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
