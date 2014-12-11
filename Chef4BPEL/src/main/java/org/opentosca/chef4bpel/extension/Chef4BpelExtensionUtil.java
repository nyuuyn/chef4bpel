package org.opentosca.chef4bpel.extension;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Element;

public class Chef4BpelExtensionUtil {
	
	public static JSONObject transformToJson(Element domElement) {
		
		/*-
		 * Example:
		 * 
		 *{
		 *	"parameters": {
		 *		"run_list": [ "recipe[embedded]" ],
		 *		"invoker_config" :{
		 *			"access": "ssh",
		 * 			"ssh_port": 22,
		 *			"ssh_host": "1.2.3.4",
		 *			"ssh_user": "ubuntu",
		 *			"ssh_private_key": "......."
		 *		}
		 *	},
		 *	"executable": {
		 *		"files": [
		 *			{ "path": "metadata.json", 
		 *			  "object": { "name": "embedded", "dependencies": { "mysql": ">= 0.0.0" } } 
		 *			},
		 *			{ "path": "recipes/default.rb", "text": "include_recipe \"mysql::server\"\n" }
		 *  		]
		 *	}
		 *}  
		 */
		
		JSONObject mainJsonObj = new JSONObject();
		
		/*
		 * Generating according to this schema:
		 *"parameters": {
		 *		"run_list": [ "recipe[embedded]" ],
		 *		"invoker_config" :{
		 *			"access": "ssh",
		 * 			"ssh_port": 22,
		 *			"ssh_host": "1.2.3.4",
		 *			"ssh_user": "ubuntu",
		 *			"ssh_private_key": "......."
		 *		}
		 *	} 
		 */
		
		JSONObject parametersObj = new JSONObject();
				
		JSONArray recipeArray = new JSONArray();
		recipeArray.put("recipe[embedded]");		
		parametersObj.put("run_list", recipeArray);
		
		JSONObject invokerConfigObject = new JSONObject();		
		invokerConfigObject.put("access", domElement.getAttribute("access"));
		invokerConfigObject.put("ssh_port", domElement.getAttribute("SSHPort"));
		invokerConfigObject.put("ssh_host", domElement.getAttribute("address"));
		invokerConfigObject.put("ssh_user", domElement.getAttribute("SSHUser"));
		invokerConfigObject.put("ssh_private_key",domElement.getAttribute("SSHPrivateKey"));
		
		parametersObj.put("invoker_config", invokerConfigObject);
		
		mainJsonObj.put("parameters", parametersObj);
		
		/*
		 *"executable": {
		 *		"files": [
		 *			{ "path": "metadata.json", 
		 *			  "object": { "name": "embedded", "dependencies": { "mysql": ">= 0.0.0" } } 
		 *			},
		 *			{ "path": "recipes/default.rb", "text": "include_recipe \"mysql::server\"\n" }
		 *  		]
		 *	} 
		 */
		
		JSONObject executableJsonObj = new JSONObject();
		
		JSONArray filesArray = new JSONArray();
		
		/* metadata.json file */
		JSONObject metadataJsonObj = new JSONObject();
		metadataJsonObj.put("path", "metadata.json");

		JSONObject objectJson = new JSONObject();				
		objectJson.put("name", "embedded");
		// content of dependencies
		JSONObject depsObjectJson = new JSONObject(domElement.getAttribute("dependencies"));
		objectJson.put("dependencies", depsObjectJson);
		
		metadataJsonObj.put("object", objectJson);
		
		JSONObject scriptObject = new JSONObject();
		scriptObject.put("path", "recipes/default.rb");
		scriptObject.put("text", domElement.getTextContent());
		
		filesArray.put(metadataJsonObj);
		filesArray.put(scriptObject);
				
		executableJsonObj.put("files", filesArray);
		
		mainJsonObj.put("executable", executableJsonObj);
		
		return mainJsonObj;
	}
}
