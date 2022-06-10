package net.packsam.dslmon.device;

import net.packsam.dslmon.device.jaxb.tr64.device.Root;
import net.packsam.dslmon.device.jaxb.tr64.device.Service;
import net.packsam.dslmon.device.jaxb.tr64.service.Scpd;

import java.io.IOException;

public class TR64Client extends HTTPXMLClient {
    public TR64Client(String hostname, int port) {
        super(hostname, port);
    }

    public Root readDeviceSpec() throws IOException {
        return readXML("/tr64desc.xml", "/xsd/tr64/device_1_0.xsd", Root.class);
    }

    public Scpd readServiceSpec(Service service) throws IOException {
        return readXML(service.getSCPDURL(), "/xsd/tr64/service_1_0.xsd", Scpd.class);
    }
}
