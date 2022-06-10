package net.packsam.dslmon.device;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import net.packsam.dslmon.device.jaxb.tr64.device.Service;
import net.packsam.dslmon.device.jaxb.tr64.service.Action;
import net.packsam.dslmon.device.jaxb.tr64.service.Argument;
import net.packsam.dslmon.device.jaxb.tr64.service.ArgumentList;
import net.packsam.dslmon.device.jaxb.tr64.service.Direction;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@WireMockTest
public class TR64ClientTest {

	@Test
	public void testReadDeviceSpec(WireMockRuntimeInfo wmRuntimeInfo) throws IOException {
		var client = new TR64Client("localhost", wmRuntimeInfo.getHttpPort());
		var root = client.readDeviceSpec();
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
	public void testReadServiceSpec(WireMockRuntimeInfo wmRuntimeInfo) throws IOException {
		var client = new TR64Client("localhost", wmRuntimeInfo.getHttpPort());

		var service = new Service();
		service.setServiceType("urn:dslforum-org:service:DeviceInfo:1");
		service.setServiceId("urn:DeviceInfo-com:serviceId:DeviceInfo1");
		service.setControlURL("/upnp/control/deviceinfo");
		service.setEventSubURL("/upnp/control/deviceinfo");
		service.setSCPDURL("/deviceinfoSCPD.xml");

		var scpd = client.readServiceSpec(service);
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
	@Disabled("has to be wiremocked")
	public void testCallAction() throws IOException {
		var tr64Client = new TR64Client("fritz.box", 49000);

		var service = new Service();
		service.setServiceType("urn:dslforum-org:service:DeviceInfo:1");
		service.setServiceId("urn:DeviceInfo-com:serviceId:DeviceInfo1");
		service.setControlURL("/upnp/control/deviceinfo");
		service.setEventSubURL("/upnp/control/deviceinfo");
		service.setSCPDURL("/deviceinfoSCPD.xml");

		var argument = new Argument();
		argument.setName("NewSecurityPort");
		argument.setDirection(Direction.OUT);
		argument.setRelatedStateVariable("SecurityPort");

		var argumentList = new ArgumentList();
		argumentList.getArgument().add(argument);

		var action = new Action();
		action.setName("GetSecurityPort");
		action.setArgumentList(argumentList);

		var response = tr64Client.callAction(service, action, null);
		System.out.println(response);
	}

}
