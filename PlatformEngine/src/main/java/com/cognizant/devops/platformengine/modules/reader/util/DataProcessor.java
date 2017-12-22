package com.cognizant.devops.platformengine.modules.reader.util;

import java.io.Serializable;

public class DataProcessor implements Serializable {
	
/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

private String level1;
	
	private String level2;
	
	private String level3;

	private String level4;
	
	private String ToolName;
	
	private String property	;
	
	private String value;

	public String getLevel1() {
		return level1;
	}
	public void setLevel1(String level1) {
		this.level1 = level1;
	}
	public String getLevel2() {
		return level2;
	}
	public void setLevel2(String level2) {
		this.level2 = level2;
	}
	public String getLevel3() {
		return level3;
	}
	public void setLevel3(String level3) {
		this.level3 = level3;
	}
	public String getLevel4() {
		return level4;
	}
	public void setLevel4(String level4) {
		this.level4 = level4;
	}
	public String getToolName() {
		return ToolName;
	}
	public void setToolName(String toolName) {
		ToolName = toolName;
	}
	public String getProperty() {
		return property;
	}
	public void setProperty(String property) {
		this.property = property;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	

}
