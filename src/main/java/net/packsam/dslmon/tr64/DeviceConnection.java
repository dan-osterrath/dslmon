package net.packsam.dslmon.tr64;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import lombok.RequiredArgsConstructor;
import net.packsam.dslmon.tr64.jaxb.device.Device;
import net.packsam.dslmon.tr64.jaxb.device.Root;
import org.apache.hc.client5.http.fluent.Request;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;

@RequiredArgsConstructor
public class DeviceConnection {

    private final String hostname;

    private final int port;

    public Device readDeviceSpec() throws IOException {
        @SuppressWarnings("HttpUrlsUsage")
        var url = String.format("http://%s:%d/tr64desc.xml", hostname, port);
        var content = Request.get(url).execute().returnContent();
        try (var is = content.asStream()) {
            var jaxbContext = JAXBContext.newInstance(Root.class);
            var unmarshaller = jaxbContext.createUnmarshaller();

            var schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            var schemaUrl = getClass().getResource("/xsd/tr64_device_1_0.xsd");
            var schema = schemaFactory.newSchema(schemaUrl);
            unmarshaller.setSchema(schema);

            var root = (Root) unmarshaller.unmarshal(is);
            return root.getDevice();
        } catch (JAXBException e) {
            throw new IOException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    }
}
