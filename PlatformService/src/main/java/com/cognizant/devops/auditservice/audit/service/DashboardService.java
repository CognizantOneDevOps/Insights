package com.cognizant.devops.auditservice.audit.service;

public interface DashboardService {

	public byte[] downloadPanel(String dashUrl, String title, String variables);

}
