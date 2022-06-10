package net.packsam.dslmon.device;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.Node;
import jakarta.xml.soap.SOAPException;
import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.ContentType;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.validation.SchemaFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

	protected InputStream callSOAPAction(String servicePath, String serviceType, String actionName, String actionBody) throws IOException {
		var soapAction = String.format("%s#%s", serviceType, actionName);
		var url = String.format("http://%s:%d%s", hostname, port, servicePath);
		var content = Request.post(url)
				.addHeader("Content-Type", "text/xml; charset=utf-8")
				.addHeader("SOAPAction", soapAction)
				.bodyString(actionBody, ContentType.TEXT_XML)
				.execute()
				.returnContent();

		return content.asStream();
	}

	protected String createSOAPBody(String serviceType, String actionName, Map<String, Object> arguments) throws IOException {
		try {
			var messageFactory = MessageFactory.newInstance();
			var soapMessage = messageFactory.createMessage();
			var soapPart = soapMessage.getSOAPPart();
			var soapEnvelope = soapPart.getEnvelope();
			var soapBody = soapEnvelope.getBody();
			soapEnvelope.setEncodingStyle("http://schemas.xmlsoap.org/soap/encoding/");
			var soapBodyElement = soapBody.addBodyElement(soapEnvelope.createName(actionName, "u", serviceType));
			if (arguments != null) {
				for (var argumentName : arguments.keySet()) {
					var soapString = valueToSOAPString(arguments.get(argumentName));
					soapBodyElement.addChildElement(argumentName).addTextNode(soapString);
				}
			}
			var baos = new ByteArrayOutputStream();
			soapMessage.writeTo(baos);
			return baos.toString(StandardCharsets.UTF_8);
		} catch (SOAPException e) {
			throw new IOException(e);
		}
	}

	protected Map<String, String> parseSOAPBody(InputStream is, String serviceType, String actionName) throws IOException {
		try {
			var messageFactory = MessageFactory.newInstance();
			var soapMessage = messageFactory.createMessage(null, is);

			if (soapMessage == null) {
				return null;
			}

			var soapBody = soapMessage.getSOAPBody();
			var nodes = soapBody.getChildElements(new QName(serviceType, actionName + "Response", "u")); // new QName(serviceType, actionName, "u")
			if (!nodes.hasNext()) {
				return null;
			}

			var responseNode = nodes.next();
			var argumentNodes = responseNode.getChildNodes();
			var arguments = new HashMap<String, String>();
			for (var i = 0; i < argumentNodes.getLength(); i++) {
				var argumentNode = argumentNodes.item(i);
				if (argumentNode.getNodeType() != Node.ELEMENT_NODE) {
					continue;
				}
				arguments.put(argumentNode.getNodeName(), argumentNode.getTextContent());
			}
			return arguments;
		} catch (SOAPException e) {
			throw new IOException(e);
		}
	}

	private String valueToSOAPString(Object value) {
		if (value == null) {
			return "";
		}

		return switch (value) {
			case String s -> s;
			case Integer i -> String.valueOf(i);
			case Boolean b -> b ? "1" : "0";
			case ZonedDateTime zdt -> DateTimeFormatter.ISO_DATE_TIME.format(zdt);
			case UUID uuid -> uuid.toString();
			default -> throw new RuntimeException("Unsupported value type: " + value.getClass());
		};
	}

}
