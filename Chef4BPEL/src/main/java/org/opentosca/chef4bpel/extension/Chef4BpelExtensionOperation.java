package org.opentosca.chef4bpel.extension;

import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.runtime.extension.AbstractSyncExtensionOperation;
import org.apache.ode.bpel.runtime.extension.ExtensionContext;
import org.json.JSONObject;
import org.json.XML;
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
	
	@Override
	protected void runSync(ExtensionContext context, Element element) throws FaultException {
		element = BPELVariableInjectionUtil.replaceExtensionVariables(context, element);
		
		String xmlString = BPELVariableInjectionUtil.nodeToString(element);
		
		System.out.println("Chef Script XML:");
		System.out.println(xmlString);

		// TODO Transformation into JSON good enough?
		/*XMLSerializer serializer = new XMLSerializer();
		JSONArray json = (JSONArray) serializer.read(xmlString);*/  
		JSONObject json = XML.toJSONObject(xmlString);
		
		System.out.println("Chef Script JSON:");
		System.out.println(json.toString());
		

		// TODO Send to Script Server: we need some kind of address here
		// TODO What to do with the response ?
	}
	
}
