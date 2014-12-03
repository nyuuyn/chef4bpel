package org.opentosca.chef4bpel.extension;

import org.apache.ode.bpel.runtime.extension.AbstractExtensionBundle;

/**
 * 
 * Copyright 2014 IAAS University of Stuttgart <br>
 * <br>
 * 
 * @author Kalman Kepes - nyuuyn@googlemail.com
 *
 */
public class Chef4BpelExtensionBundle extends AbstractExtensionBundle {
	
	private static final String chef4BpelNamespace = "http://www.opentosca.org/bpel/chef4bpel";

	@Override
	public String getNamespaceURI() {
		return Chef4BpelExtensionBundle.chef4BpelNamespace;
	}

	@Override
	public void registerExtensionActivities() {
		super.registerExtensionOperation("Chef4BPELScript", Chef4BpelExtensionOperation.class);				
	}
	
}
