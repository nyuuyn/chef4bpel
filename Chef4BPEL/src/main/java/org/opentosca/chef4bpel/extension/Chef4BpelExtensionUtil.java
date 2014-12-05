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
		 *		"access": "ssh",
		 * 		"ssh_port": 22,
		 *		"ssh_host": "1.2.3.4",
		 *		"ssh_user": "ubuntu",
		 *		"ssh_private_key": "......."
		 *	},
		 *	"executable": {
		 *		"name": "embedded",
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
		
		JSONObject parametersObj = new JSONObject();
		
		
		JSONArray recipeArray = new JSONArray();
		recipeArray.put("recipe[embedded]");
		
		// TODO read values from dom attr
		parametersObj.put("run_list", recipeArray);
		parametersObj.put("access", domElement.getAttribute("access"));
		parametersObj.put("ssh_port", domElement.getAttribute("SSHPort"));
		parametersObj.put("ssh_host", domElement.getAttribute("address"));
		parametersObj.put("ssh_user", domElement.getAttribute("SSHUser"));
		parametersObj.put("ssh_private_key",domElement.getAttribute("SSHPrivateKey"));
		
		mainJsonObj.put("parameters", parametersObj);
		
		JSONObject executableJsonObj = new JSONObject();
		
		JSONArray filesArray = new JSONArray();
		
		/* metadata.json file */
		JSONObject metadataJsonObj = new JSONObject();
		metadataJsonObj.put("path", "metadata.json");
		// content of dependencies
		JSONObject objectJson = new JSONObject();
		objectJson.put("name","embedded");
		
		// TODO add declared dependencies
		JSONObject depsObjectJson = new JSONObject(domElement.getAttribute("dependencies"));
		//depsObjectJson.put("mysql", ">= 0.0.0");
		
		objectJson.put("dependencies", depsObjectJson);
		
		JSONObject scriptObject = new JSONObject();
		scriptObject.put("path", "recipes/default.rb");
		scriptObject.put("text", domElement.getTextContent());
		
		filesArray.put(metadataJsonObj);
		filesArray.put(scriptObject);
		
		
		executableJsonObj.put("name", "embedded");
		executableJsonObj.put("files", filesArray);
		
		mainJsonObj.put("executable", executableJsonObj);
		
		return mainJsonObj;
	}
}
