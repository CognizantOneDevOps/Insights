package com.cognizant.devops.platformdal.ComponentConfig;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "component_configuration")
public class ComponentConfiguration {
	@Id
	@Column(name = "ID", unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	
	@Column(name="component_type",nullable=false)
	private String componentType;
	
	@Column(name="os_version",nullable=false)
	private String osVersion;
	
	@Column(name="unique_key",nullable=false)
	private String uniqueKey;
	
	@Column(name="update_date",nullable=false)
	private java.util.Date updateDate;
	
	@Column(name="component_version",nullable=false)
	private String componentVersion;
	
	@Column(name="component_status",nullable=false)
	private String componentStatus;
	
	@Column(name="label1")
	private String label1;
	
	@Column(name="json1",length = 10000)
	private String json1;
	
	@Column(name="label2")
	private String label2;
	
	@Column(name="json2",length = 10000)
	private String json2;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getComponentType() {
		return componentType;
	}

	public void setComponentType(String componentType) {
		this.componentType = componentType;
	}

	public String getOsVersion() {
		return osVersion;
	}

	public void setOsVersion(String osVersion) {
		this.osVersion = osVersion;
	}

	public String getUniqueKey() {
		return uniqueKey;
	}

	public void setUniqueKey(String uniqueKey) {
		this.uniqueKey = uniqueKey;
	}

	public java.util.Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(java.util.Date updateDate) {
		this.updateDate = updateDate;
	}

	public String getComponentVersion() {
		return componentVersion;
	}

	public void setComponentVersion(String componentVersion) {
		this.componentVersion = componentVersion;
	}

	public String getComponentStatus() {
		return componentStatus;
	}

	public void setComponentStatus(String componentStatus) {
		this.componentStatus = componentStatus;
	}

	public String getLabel1() {
		return label1;
	}

	public void setLabel1(String label1) {
		this.label1 = label1;
	}

	public String getJson1() {
		return json1;
	}

	public void setJson1(String json1) {
		this.json1 = json1;
	}

	public String getLabel2() {
		return label2;
	}

	public void setLabel2(String label2) {
		this.label2 = label2;
	}

	public String getJson2() {
		return json2;
	}

	public void setJson2(String json2) {
		this.json2 = json2;
	}

	@Override
	public String toString() {
		return "ComponentConfiguration [id=" + id + ", componentType=" + componentType + ", osVersion=" + osVersion
				+ ", uniqueKey=" + uniqueKey + ", updateDate=" + updateDate + ", componentVersion=" + componentVersion
				+ ", componentStatus=" + componentStatus + ", label1=" + label1 + ", json1=" + json1 + ", label2="
				+ label2 + ", json2=" + json2 + "]";
	}

	
}
