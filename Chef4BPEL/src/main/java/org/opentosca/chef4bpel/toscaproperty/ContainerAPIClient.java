package org.opentosca.chef4bpel.toscaproperty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.opentosca.bpel4restlight.rest.HighLevelRestApi;
import org.opentosca.bpel4restlight.rest.HttpResponseMessage;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * @author kepeskn
 *
 */
public class ContainerAPIClient {
	private static final XPathFactory xpathFactory = XPathFactory.newInstance();

	public static String getProperty(String url, String entityTemplateId, String propertyName) {
		for (String entryDefUrl : getEntryDefinitions(url)) {
			for (String serviceTemplateId : getServiceTemplateIdsFromDefinitionsUrl(entryDefUrl)) {
				String propertyValue = getProperty(url, serviceTemplateId, entityTemplateId, propertyName);
				if (propertyValue != null) {
					return propertyValue;
				}
			}
		}

		return null;

	}

	public static String getProperty(String url, String serviceTemplateId, String entityTemplateId,
			String propertyName) {
		if (url.contains("containerapi/instancedata")) {
			return getPropertyInstanceDataAPI(url, serviceTemplateId, entityTemplateId, propertyName);
		} else if (url.contains("containerapi")) {
			return getPropertyContainerAPI(url, serviceTemplateId, entityTemplateId, propertyName);
		}

		return null;

	}

	private static String getPropertyInstanceDataAPI(String url, String serviceTemplateId, String entityTemplateId,
			String propertyName) {

		String serviceInstanceResourceUrl = getServiceInstance(url, serviceTemplateId);

		HttpResponseMessage serviceInstanceResourceResponse = HighLevelRestApi.Get(serviceInstanceResourceUrl,
				"application/xml");

		InputSource source = new InputSource(new StringReader(serviceInstanceResourceResponse.getResponseBody()));
		XPath serviceInstanceResourceXpath = xpathFactory.newXPath();

		List<String> nodeInstanceUrls = new ArrayList<String>();

		try {
			NodeList nodeInstancesList = (NodeList) serviceInstanceResourceXpath.evaluate(
					"/*[local-name()='ServiceInstance']/*[local-name()='nodeInstances']/*[local-name()='nodeInstance']",
					source, XPathConstants.NODESET);

			for (int nodeInstancesIndex = 0; nodeInstancesIndex < nodeInstancesList.getLength(); nodeInstancesIndex++) {
				nodeInstanceUrls.add(nodeInstancesList.item(nodeInstancesIndex).getAttributes().getNamedItem("href")
						.getTextContent());
			}

		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (String nodeInstanceUrl : nodeInstanceUrls) {
			HttpResponseMessage nodeInstanceResourceResponse = HighLevelRestApi.Get(nodeInstanceUrl, "application/xml");

			source = new InputSource(new StringReader(nodeInstanceResourceResponse.getResponseBody()));
			XPath nodeInstanceXpath = xpathFactory.newXPath();

			try {
				NodeList nodeInstance = (NodeList) nodeInstanceXpath.evaluate(
						"/*[local-name()='NodeInstance' and @nodeTemplateID='" + entityTemplateId + "']", source,
						XPathConstants.NODESET);

				if (nodeInstance.getLength() == 1) {
					String nodeInstancePropertiesResourceUrl = (nodeInstanceUrl.endsWith("/"))
							? nodeInstanceUrl + "Properties" : nodeInstanceUrl + "/Properties";

					HttpResponseMessage nodeInstancePropertiesResourceResponse = HighLevelRestApi
							.Get(nodeInstancePropertiesResourceUrl, "application/xml");

					source = new InputSource(new StringReader(nodeInstanceResourceResponse.getResponseBody()));
					XPath nodeInstancePropertiesResourceXpath = xpathFactory.newXPath();

					NodeList nodeInstanceProperty = (NodeList) nodeInstancePropertiesResourceXpath
							.evaluate("/*/*[local-name()='" + propertyName + "']", source, XPathConstants.NODESET);

					if (nodeInstanceProperty.getLength() == 1) {
						return nodeInstanceProperty.item(0).getTextContent();
					}

				}
			} catch (XPathExpressionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return null;
	}

	private static String getServiceInstance(String url, String serviceTemplateId) {
		String serviceInstancesResourceUrl = (url.endsWith("/")) ? url + "serviceInstances" : url + "/serviceInstances";

		HttpResponseMessage serviceInstancesResourceResponse = HighLevelRestApi.Get(serviceInstancesResourceUrl,
				"application/xml");

		InputSource source = new InputSource(new StringReader(serviceInstancesResourceResponse.getResponseBody()));
		XPath serviceInstancesXpath = xpathFactory.newXPath();

		List<String> serviceInstanceUrls = new ArrayList<String>();
		try {
			NodeList serviceInstancesList = (NodeList) serviceInstancesXpath.evaluate(
					"/*[local-name()='ServiceInstanceList']/*[local-name()='serviceinstances']/*[local-name()='link']",
					source, XPathConstants.NODESET);

			for (int index = 0; index < serviceInstancesList.getLength(); index++) {
				serviceInstanceUrls
						.add(serviceInstancesList.item(index).getAttributes().getNamedItem("href").getTextContent());
			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (String serviceInstanceUrl : serviceInstanceUrls) {
			HttpResponseMessage serviceInstanceResourceResponse = HighLevelRestApi.Get(serviceInstanceUrl,
					"application/xml");

			source = new InputSource(new StringReader(serviceInstanceResourceResponse.getResponseBody()));
			XPath serviceInstanceXpath = xpathFactory.newXPath();

			try {
				NodeList serviceInstance = (NodeList) serviceInstanceXpath.evaluate(
						"/*[local-name='ServiceInstance' and @serviceTemplateID='" + serviceTemplateId + "']", source,
						XPathConstants.NODESET);

				if (serviceInstance.getLength() == 1) {
					return serviceInstanceUrl;
				}
			} catch (XPathExpressionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return null;
	}

	private static String getPropertyContainerAPI(String url, String serviceTemplateId, String entityTemplateId,
			String propertyName) {

		List<String> entryDefinitionUrls = getEntryDefinitions(url);

		for (String entryDefinitionUrl : entryDefinitionUrls) {
			HttpResponseMessage serviceTemplateResourceResponse = HighLevelRestApi.Get(entryDefinitionUrl,
					"application/octet-stream");

			InputSource source = new InputSource(new StringReader(serviceTemplateResourceResponse.getResponseBody()));
			XPath entryDefinitionResourceXpath = xpathFactory.newXPath();

			try {
				NodeList propertyElementList = (NodeList) entryDefinitionResourceXpath.evaluate(
						"/*[local-name()='Definitions']/*[local-name()='ServiceTemplate' and @id='" + serviceTemplateId
								+ "']/*[local-name()='TopologyTemplate']/*[local-name()='NodeTemplate']/*[local-name()='Properties']/*/*[local-name='"
								+ propertyName + "']",
						source, XPathConstants.NODESET);

				if (propertyElementList.getLength() != 1) {
					System.out.println("No NodeTemplate in Definitions at " + entryDefinitionUrl
							+ " does contain property " + propertyName);
				} else {
					return propertyElementList.item(0).getTextContent();
				}

			} catch (XPathExpressionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			source = new InputSource(new StringReader(serviceTemplateResourceResponse.getResponseBody()));
			entryDefinitionResourceXpath = xpathFactory.newXPath();
			try {
				NodeList propertyElementList = (NodeList) entryDefinitionResourceXpath.evaluate(
						"/*[local-name()='Definitions']/*[local-name()='ServiceTemplate' and @id='" + serviceTemplateId
								+ "']/*[local-name()='TopologyTemplate']/*[local-name()='RelationshipTemplate']/*[local-name()='Properties']/*/*[local-name='"
								+ propertyName + "']",
						source, XPathConstants.NODESET);

				if (propertyElementList.getLength() != 1) {
					System.out.println("No RelationshipTemplate in Definitions at " + entryDefinitionUrl
							+ " does contain property " + propertyName);
				} else {
					return propertyElementList.item(0).getTextContent();
				}

			} catch (XPathExpressionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return null;
	}

	private static List<String> getServiceTemplateIdsFromDefinitionsUrl(String defUrl) {

		HttpResponseMessage definitionsResourceResponse = HighLevelRestApi.Get(defUrl, "application/octet-stream");

		InputSource source = new InputSource(new StringReader(definitionsResourceResponse.getResponseBody()));

		XPath definitionsResourceXpath = xpathFactory.newXPath();

		List<String> serviceTemplateIds = new ArrayList<String>();

		try {
			NodeList serviceTemplateList = (NodeList) definitionsResourceXpath.evaluate(
					"/*[local-name='Definitions']/*[local-name()='ServiceTemplate']", source, XPathConstants.NODESET);

			for (int index = 0; index < serviceTemplateList.getLength(); index++) {
				Node serviceTemplate = serviceTemplateList.item(index);

				serviceTemplateIds.add(serviceTemplate.getAttributes().getNamedItem("id").getTextContent());
			}

		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return serviceTemplateIds;
	}

	private static List<String> getEntryDefinitions(String url) {

		String csarsResourceUrl = (url.endsWith("/")) ? url + "CSARs" : url + "/CSARs";

		HttpResponseMessage csarsResourceResponse = HighLevelRestApi.Get(csarsResourceUrl, "application/xml");

		InputSource source = new InputSource(new StringReader(csarsResourceResponse.getResponseBody()));
		XPath csarsResourceResponseXpath = xpathFactory.newXPath();

		// Fetch csars
		List<String> csarUrls = new ArrayList<String>();
		try {

			NodeList referencesList = (NodeList) csarsResourceResponseXpath.evaluate("/References/Reference", source,
					XPathConstants.NODESET);

			for (int index = 0; index < referencesList.getLength(); index++) {
				Node reference = referencesList.item(index);
				if (!reference.getAttributes().getNamedItem("title").getTextContent().equals("Self")) {
					csarUrls.add(reference.getAttributes().getNamedItem("href").getTextContent());
				}
			}

		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Fetch entry-definitions for each csar
		List<String> entryDefsUrls = new ArrayList<String>();

		for (String csarUrl : csarUrls) {
			String csarToscaMetaResourceUrl = (csarUrl.endsWith("/")) ? csarUrl + "Content/TOSCA-Metadata/TOSCA.meta"
					: csarUrl + "/Content/TOSCA-Metadata/TOSCA.meta";

			HttpResponseMessage csarToscaMetaResourceResponse = HighLevelRestApi.Get(csarToscaMetaResourceUrl,
					"application/octet-stream");

			String entryDefinitionsPath = null;

			BufferedReader reader = new BufferedReader(
					new StringReader(csarToscaMetaResourceResponse.getResponseBody()));
			try {
				String line = reader.readLine();
				while (line != null) {
					if (line.contains("Entry-Definitions:")) {
						entryDefinitionsPath = line.split(":")[1].trim();
					} else {
						line = reader.readLine();
					}
				}
				reader.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (entryDefinitionsPath != null) {
				entryDefsUrls.add((csarUrl.endsWith("/")) ? csarUrl + "Content/" + entryDefinitionsPath
						: csarUrl + "/Content/" + entryDefinitionsPath);
			}

		}

		return entryDefsUrls;
	}

}