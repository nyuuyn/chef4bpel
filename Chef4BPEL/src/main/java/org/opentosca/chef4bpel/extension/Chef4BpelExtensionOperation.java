package org.opentosca.chef4bpel.extension;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.runtime.extension.AbstractSyncExtensionOperation;
import org.apache.ode.bpel.runtime.extension.ExtensionContext;
import org.json.JSONObject;
import org.json.XML;
import org.opentosca.bpel4restlight.rest.HighLevelRestApi;
import org.opentosca.bpel4restlight.rest.HttpResponseMessage;
import org.w3c.dom.Element;

import de.unistuttgart.iaas.bpel.util.BPELVariableInjectionUtil;

/**
 * Copyright 2014 IAAS University of Stuttgart <br>
 * <br>
 * 
 * @author Kalman Kepes - nyuuyn@googlemail.com
 *
 */
public class Chef4BpelExtensionOperation extends AbstractSyncExtensionOperation {
	
	private Properties configuration = null;
	
	
	@Override
	protected void runSync(ExtensionContext context, Element element) throws FaultException {
		try {
			this.loadConfiguartion();
		} catch (IOException e) {
			System.err.println("Couldn't load configuration file");
			e.printStackTrace();
		}
		
		element = BPELVariableInjectionUtil.replaceExtensionVariables(context, element);
		
		String xmlString = BPELVariableInjectionUtil.nodeToString(element);
		String jsonString = Chef4BpelExtensionUtil.transformToJson(element).toString(4);
		
		System.out.println("Chef Script XML:");
		System.out.println(xmlString);
		
		System.out.println("Chef Script JSON:");
		System.out.println(jsonString);
		
		// TODO What to do with the response ?
		HttpResponseMessage responseMessage = HighLevelRestApi.Post(configuration.getProperty("clartigr.location"), jsonString, "application/json");
		
	}
	
	private void loadConfiguartion() throws IOException {
		Properties prop = new Properties();
		String propFileName = "config.properties";
		
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
		
		if (inputStream != null) {
			prop.load(inputStream);
		} else {
			throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
		}
		
		this.configuration = prop;
	}
	
}
