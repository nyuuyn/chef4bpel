package org.opentosca.chef4bpel.toscaproperty;

import java.io.StringReader;
import java.util.Map;

import javax.activation.MailcapCommandMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.o.OScope.Variable;
import org.apache.ode.bpel.runtime.extension.ExtensionContext;
import org.opentosca.chef4bpel.extension.Chef4BpelExtensionOperation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import de.unistuttgart.iaas.bpel.util.BPELVariableInjectionUtil;

/**
 * Copyright 2014 IAAS University of Stuttgart <br>
 * <br>
 * 
 * @author Kalman Kepes - nyuuyn@googlemail.com
 *
 */
public class ToscaPropertyInjectionUtil {

	public static String injectToscaProperty(ExtensionContext bpelContext, String chef4bpel) {
		/* determine the configuration */
		// fetch containerAPI, instanceDataAPI and a possible serviceInstance
		// variable
		String containerAPIURL = null;
		String instanceDataAPIURL = null;
		String serviceInstanceIDURL = null;

		try {
			Map<String, Variable> visibleVariables = bpelContext.getVisibleVariables();

			for (String bpelVarName : visibleVariables.keySet()) {
				// instanceDataAPIUrl, OpenTOSCAContainerAPIServiceInstanceID

				if (bpelVarName.contains(Constants.InstanceDataAPIURLVarName)) {
					instanceDataAPIURL = BPELVariableInjectionUtil
							.nodeToString(bpelContext.readVariable(visibleVariables.get(bpelVarName)));
				} else if (bpelVarName.contains(Constants.ServiceInstanceURLVarName)) {
					serviceInstanceIDURL = BPELVariableInjectionUtil
							.nodeToString(bpelContext.readVariable(visibleVariables.get(bpelVarName)));
				} else if (bpelVarName.contains(Constants.PlanBuilderBuildPlanInputMessageVarName)) {
					System.out.println("Found possible input message element");
				}

			}

			// calculate some containerAPIURL
			if (instanceDataAPIURL != null & serviceInstanceIDURL != null) {
				containerAPIURL = ToscaPropertyInjectionUtil.getContainerAPIURL(instanceDataAPIURL,
						serviceInstanceIDURL);
			} else if (instanceDataAPIURL != null) {
				containerAPIURL = ToscaPropertyInjectionUtil.getContainerAPIURL(instanceDataAPIURL);

			} else if (serviceInstanceIDURL != null) {
				containerAPIURL = ToscaPropertyInjectionUtil.getContainerAPIURL(serviceInstanceIDURL);
			}

		} catch (FaultException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// if no containerAPI url was determined until now -> use the one in
		// config.properties
		if (containerAPIURL == null) {
			System.out.println("No containerAPI was found in bpelContext, falling back to URL in configuration");
			containerAPIURL = Chef4BpelExtensionOperation.getConfiguration().getProperty("opentosca.location");
		}

		System.out.println("InstanceDataAPIUrl: " + instanceDataAPIURL);
		System.out.println("ServiceInstanceUrl: " + serviceInstanceIDURL);
		System.out.println("containerAPIUrl: " + containerAPIURL);
		
		// determine Template Mode (No InstanceData available) or Instance Mode
		if(serviceInstanceIDURL != null){
			System.out.println("Using ServiceInstanceURL " + serviceInstanceIDURL);
			return ToscaPropertyInjectionUtil.injectToscaPropertiesInstanceDataAPI(bpelContext, chef4bpel, getInstanceDataAPIURL(serviceInstanceIDURL));
		}

		if(instanceDataAPIURL != null){
			System.out.println("Using instanceDataAPI " + instanceDataAPIURL);
			return ToscaPropertyInjectionUtil.injectToscaPropertiesInstanceDataAPI(bpelContext, chef4bpel,
					instanceDataAPIURL);
		}
		
		if(containerAPIURL != null){
			System.out.println("Using containerAPI " + containerAPIURL);
			return ToscaPropertyInjectionUtil.injectToscaPropertiesContainerAPI(bpelContext, chef4bpel, containerAPIURL);
		}

		return null;
	}

	
	private static String injectToscaPropertiesInstanceDataAPI(ExtensionContext bpelContext, String chef4bpel,
			String instanceDataAPIURL) {

	
		// Find and replace referenced BPEL-Variables
		int startIndex = chef4bpel.indexOf("$TOSCAProperty[");
		if (startIndex != -1) {
			while (startIndex != -1) {
				int endIndex = startIndex;
				while (chef4bpel.charAt(endIndex) != ']') {
					endIndex++;
				}

				// Extract name of referenced variable
				String variableName = chef4bpel.substring(startIndex + 15, endIndex);

				System.out.println("Found following TOSCAProperty reference: " + variableName);
				String propertyValue = null;
				switch (variableName.split("\\.").length) {
				case 2:
					propertyValue = ContainerAPIClient.getProperty(instanceDataAPIURL, variableName.split("\\.")[0],
							variableName.split("\\.")[1]);
					break;
				case 3:
					propertyValue = ContainerAPIClient.getProperty(instanceDataAPIURL, variableName.split("\\.")[0],
							variableName.split("\\.")[1], variableName.split("\\.")[2]);
					break;
				default:
					System.out.println("TOSCAProperty Key is malformed!");
					break;
				}

				if (propertyValue != null) {
					// Replace variable-reference with corresponding content
					chef4bpel = chef4bpel.replace("$TOSCAProperty[" + variableName + "]", propertyValue);
				} else{
					chef4bpel = chef4bpel.replace("$TOSCAProperty["+variableName + "]", "");
				}

				System.out.println("The full chef4bpel script as string: \n" + chef4bpel + "\n");
				startIndex = chef4bpel.indexOf("$TOSCAProperty[");
			}
			return chef4bpel;
		} else {

			// If no referenced properties are found, return original code
			return chef4bpel;
		}


	}

	private static String injectToscaPropertiesContainerAPI(ExtensionContext bpelContext, String chef4bpel,
			String containerAPIUrl) {

		// Find and replace referenced BPEL-Variables
		int startIndex = chef4bpel.indexOf("$TOSCAProperty[");
		if (startIndex != -1) {
			while (startIndex != -1) {
				int endIndex = startIndex;
				while (chef4bpel.charAt(endIndex) != ']') {
					endIndex++;
				}

				// Extract name of referenced variable
				String variableName = chef4bpel.substring(startIndex + 15, endIndex);

				System.out.println("Found following TOSCAProperty reference: " + variableName);
				String propertyValue = null;
				switch (variableName.split("\\.").length) {
				case 2:
					propertyValue = ContainerAPIClient.getProperty(containerAPIUrl, variableName.split("\\.")[0],
							variableName.split("\\.")[1]);
					break;
				case 3:
					propertyValue = ContainerAPIClient.getProperty(containerAPIUrl, variableName.split("\\.")[0],
							variableName.split("\\.")[1], variableName.split("\\.")[2]);
					break;
				default:
					System.out.println("TOSCAProperty Key is malformed!");
					break;
				}

				if (propertyValue != null) {
					// Replace variable-reference with corresponding content
					chef4bpel = chef4bpel.replace("$TOSCAProperty[" + variableName + "]", propertyValue);
				} else{
					chef4bpel = chef4bpel.replace("$TOSCAProperty["+variableName + "]", "");
				}

				System.out.println("The full chef4bpel script as string: \n" + chef4bpel + "\n");
				startIndex = chef4bpel.indexOf("$TOSCAProperty[");
			}
			return chef4bpel;
		} else {

			// If no referenced properties are found, return original code
			return chef4bpel;
		}

	}

	private static String getInstanceDataAPIURL(String url) {
		return getSubStringSplit(url, "containerapi/instancedata");
	}

	private static String getContainerAPIURL(String instanceDataAPIURL, String serviceInstanceIDURL) {

		if (instanceDataAPIURL.contains("containerapi") & serviceInstanceIDURL.contains("containerapi")) {
			if (instanceDataAPIURL.split("containerapi")[0].equals(serviceInstanceIDURL.split("containerapi")[0])) {

				System.out.println("Given URL's point to same containerapi: ");
				System.out.println("Url1: " + instanceDataAPIURL);
				System.out.println("Url2: " + serviceInstanceIDURL);
				return ToscaPropertyInjectionUtil.getContainerAPIURL(serviceInstanceIDURL);
			}
		}

		if (instanceDataAPIURL.contains("containerapi")) {
			return ToscaPropertyInjectionUtil.getContainerAPIURL(instanceDataAPIURL);
		} else if (serviceInstanceIDURL.contains("containerapi")) {
			return ToscaPropertyInjectionUtil.getContainerAPIURL(serviceInstanceIDURL);
		}

		return null;
	}

	private static String getContainerAPIURL(String url) {
		return getSubStringSplit(url, "containerapi");
	}

	private static String getSubStringSplit(String string, String split) {
		if (string.contains(split)) {
			return string.split(split)[0] + split;
		} else {
			return null;
		}
	}

}
