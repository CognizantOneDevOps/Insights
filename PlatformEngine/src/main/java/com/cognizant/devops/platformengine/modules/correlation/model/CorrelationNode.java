package com.cognizant.devops.platformengine.modules.correlation.model;

import java.util.List;
/**
 * @author Vishal Ganjare (vganjare)
 *
 */
public class CorrelationNode {
	private String toolName;
	private List<String> fields;
	
	public String getToolName() {
		return toolName;
	}
	public void setToolName(String toolName) {
		this.toolName = toolName;
	}
	public List<String> getFields() {
		return fields;
	}
	public void setFields(List<String> fields) {
		this.fields = fields;
	}
}
