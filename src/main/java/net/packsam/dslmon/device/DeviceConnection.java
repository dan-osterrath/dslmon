package net.packsam.dslmon.device;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import lombok.RequiredArgsConstructor;
import net.packsam.dslmon.device.jaxb.tr64.Root;
import org.apache.hc.client5.http.fluent.Request;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;

@RequiredArgsConstructor
public class DeviceConnection {

    private final String hostname;

    private final int port;

    public Root readTR64DeviceSpec() throws IOException {
        return readDeviceSpec("/tr64desc.xml", "/xsd/tr64_device_1_0.xsd", Root.class);
    }

    public net.packsam.dslmon.device.jaxb.igd.Root readIGDDeviceSpec() throws IOException {
        return readDeviceSpec("/igddesc.xml", "/xsd/igd_device_1_0.xsd", net.packsam.dslmon.device.jaxb.igd.Root.class);
    }

    private <R> R readDeviceSpec(String specPath, String schemaPath, Class<R> rootClass) throws IOException {
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
