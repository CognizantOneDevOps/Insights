package com.cognizant.devops.platformservice.grafanadashboard.service;

public interface DashboardService {

	public byte[] downloadPanel(String dashUrl, String title, String variables);

}
