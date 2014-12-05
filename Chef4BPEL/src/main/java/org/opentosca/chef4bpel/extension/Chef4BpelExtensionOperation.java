package org.opentosca.chef4bpel.extension;

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
	
	@Override
	protected void runSync(ExtensionContext context, Element element) throws FaultException {
		element = BPELVariableInjectionUtil.replaceExtensionVariables(context, element);
		
		String xmlString = BPELVariableInjectionUtil.nodeToString(element);
		String jsonString = Chef4BpelExtensionUtil.transformToJson(element).toString(4);
		
		System.out.println("Chef Script XML:");
		System.out.println(xmlString);

		System.out.println("Chef Script JSON:");		
		System.out.println(jsonString);
						
		// TODO What to do with the response ?
		HttpResponseMessage responseMessage = HighLevelRestApi.Post("http://localhost/api/v1/invokers/chef-cookbooks/runs", jsonString, "application/json");

	}
	
}
