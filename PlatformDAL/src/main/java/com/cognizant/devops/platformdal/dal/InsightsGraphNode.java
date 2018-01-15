package com.cognizant.devops.platformdal.dal;

import java.util.List;
import java.util.Map;

public class InsightsGraphNode {

	private Map<String, Object> propertyMap;
	private InsightsRelationShip relation;
	private List<String> labels;
	
	public Map<String, Object> getPropertyMap() {
		return propertyMap;
	}
	
	public void setPropertyMap(Map<String, Object> propertyMap) {
		this.propertyMap = propertyMap;
	}
	
	public InsightsRelationShip getRelation() {
		return relation;
	}
	
	public void setRelation(InsightsRelationShip relation) {
		this.relation = relation;
	}
	
	public List<String> getLabels() {
		return labels;
	}
	
	public void setLabels(List<String> labels) {
		this.labels = labels;
	}
}
