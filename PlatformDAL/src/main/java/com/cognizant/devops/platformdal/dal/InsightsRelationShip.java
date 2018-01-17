package com.cognizant.devops.platformdal.dal;

import java.util.Map;

public class InsightsRelationShip {

	private String name;
	private Map<String, Object> propertyMap;
	private InsightsGraphNode startNode;
	private InsightsGraphNode endNode;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Map<String, Object> getPropertyMap() {
		return propertyMap;
	}
	public void setPropertyMap(Map<String, Object> propertyMap) {
		this.propertyMap = propertyMap;
	}
	public InsightsGraphNode getStartNode() {
		return startNode;
	}
	public void setStartNode(InsightsGraphNode startNode) {
		this.startNode = startNode;
	}
	public InsightsGraphNode getEndNode() {
		return endNode;
	}
	public void setEndNode(InsightsGraphNode endNode) {
		this.endNode = endNode;
	}
	
}
