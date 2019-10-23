package com.cognizant.devops.platformservice.grafanadashboard.service;

import org.springframework.stereotype.Service;

import com.cognizant.devops.platformservice.grafanadashboard.util.DashboardUtil;

@Service
public class DashboardServiceImpl implements DashboardService{

	@Override
	public byte[] downloadPanel(String dashUrl, String title, String variables) {
		DashboardUtil dashboardUtil = new DashboardUtil();
		return dashboardUtil.getPanels(dashUrl, title, variables);	
	}

}
