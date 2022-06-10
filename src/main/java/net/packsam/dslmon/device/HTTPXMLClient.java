package net.packsam.dslmon.device;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.fluent.Request;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;

@RequiredArgsConstructor
public class HTTPXMLClient {
    private final String hostname;

    private final int port;

    protected <R> R readXML(String path, String schemaPath, Class<R> rootClass) throws IOException {
        @SuppressWarnings("HttpUrlsUsage")
        var url = String.format("http://%s:%d%s", hostname, port, path);
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
        } catch (JAXBException | SAXException e) {
            throw new IOException(e);
        }
    }

}
