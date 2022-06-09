package net.packsam.dslmon.device;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import lombok.RequiredArgsConstructor;
import net.packsam.dslmon.device.jaxb.tr64.device.Root;
import net.packsam.dslmon.device.jaxb.tr64.device.Service;
import net.packsam.dslmon.device.jaxb.tr64.service.Scpd;
import org.apache.hc.client5.http.fluent.Request;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;

@RequiredArgsConstructor
public class FritzBoxConnector {

    private final String hostname;

    private final int port;

    public Root readTR64DeviceSpec() throws IOException {
        return readSpec("/tr64desc.xml", "/xsd/tr64/device_1_0.xsd", Root.class);
    }

    public net.packsam.dslmon.device.jaxb.igd.device.Root readIGDDeviceSpec() throws IOException {
        return readSpec("/igddesc.xml", "/xsd/igd/device_1_0.xsd", net.packsam.dslmon.device.jaxb.igd.device.Root.class);
    }

    public Scpd readTR64ServiceSpec(Service service) throws IOException {
        return readSpec(service.getSCPDURL(), "/xsd/tr64/service_1_0.xsd", Scpd.class);
    }

    public net.packsam.dslmon.device.jaxb.igd.service.Scpd readIGDServiceSpec(net.packsam.dslmon.device.jaxb.igd.device.Service service) throws IOException {
        return readSpec(service.getSCPDURL(), "/xsd/igd/service_1_0.xsd", net.packsam.dslmon.device.jaxb.igd.service.Scpd.class);
    }

    private <R> R readSpec(String specPath, String schemaPath, Class<R> rootClass) throws IOException {
        @SuppressWarnings("HttpUrlsUsage")
        var url = String.format("http://%s:%d%s", hostname, port, specPath);
        var content = Request.get(url).execute().returnContent();
        try (var is = content.asStream()) {
            var jaxbContext = JAXBContext.newInstance(rootClass);
            var unmarshaller = jaxbContext.createUnmarshaller();

            var schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            var schemaUrl = getClass().getResource(schemaPath);
            var schema = schemaFactory.newSchema(schemaUrl);
            unmarshaller.setSchema(schema);

            //noinspection unchecked
            return (R) unmarshaller.unmarshal(is);
        } catch (JAXBException e) {
            throw new IOException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    }
}
