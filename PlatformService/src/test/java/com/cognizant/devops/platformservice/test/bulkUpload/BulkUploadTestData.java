package com.cognizant.devops.platformservice.test.bulkUpload;

import java.io.File;

public class BulkUploadTestData {

	String toolJson = "[{ \"toolName\":\"GIT\",\"label\":\"SCM:GIT:DATA\"}]";

	File file = new File("GIT.csv");
	String toolName = "GIT";
	String label = "SCM:GIT:DATA";

	File fileNull = new File("JENKINS.csv");
	String toolNameNull = null;
	String labelNull = null;

}
