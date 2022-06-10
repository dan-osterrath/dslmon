package net.packsam.dslmon.device;

import net.packsam.dslmon.device.jaxb.igd.device.Root;
import net.packsam.dslmon.device.jaxb.igd.device.Service;
import net.packsam.dslmon.device.jaxb.igd.service.Scpd;

import java.io.IOException;

public class IGDClient extends HTTPXMLClient {
	public IGDClient(String hostname, int port) {
		super(hostname, port);
	}

	public Root readDeviceSpec() throws IOException {
		return readXML("/igddesc.xml", "/xsd/igd/device_1_0.xsd", Root.class);
	}

	public Scpd readServiceSpec(Service service) throws IOException {
		return readXML(service.getSCPDURL(), "/xsd/igd/service_1_0.xsd", Scpd.class);
	}
}
