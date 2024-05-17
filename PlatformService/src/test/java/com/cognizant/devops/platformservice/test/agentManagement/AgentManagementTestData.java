/*******************************************************************************
 *  * Copyright 2017 Cognizant Technology Solutions
 *  * 
 *  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  * use this file except in compliance with the License.  You may obtain a copy
 *  * of the License at
 *  * 
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  * 
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 *  * License for the specific language governing permissions and limitations under
 *  * the License.
 *******************************************************************************/
package com.cognizant.devops.platformservice.test.agentManagement;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.util.FileCopyUtils;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.outcome.InsightsTools;
import com.cognizant.devops.platformdal.outcome.OutComeConfigDAL;

public class AgentManagementTestData extends AbstractTestNGSpringContextTests {
	ClassLoader classLoader = ClassLoader.getSystemClassLoader();
	private static final Logger log = LogManager.getLogger(AgentManagementTestData.class);

	String osversion = "Windows";
	String typeAgent = "agent";
	String typeROI = "ROIAgent";

	OutComeConfigDAL outComeConfigDAL = new OutComeConfigDAL();

	String trackingDetails = "";
	String ROIAgentId = "ROI_testing";

	Date updateDate = Timestamp.valueOf(LocalDateTime.now());

	String agentIdTrackingDetails = "git_testng_trackingDetails";
	String agentId = "git_testng";
	String toolCategory = "SCM";
	String oldOfflineAgentPath = "";
	String version = "v9.1";
	String gitTool = "git";
	String updateVersion = "v9.4";
	String agentIdNotExist = "git_exception";
	String ROITool = "newrelic";
	String agentIdForUpdate = "git_update_testng";
	File offlineAgentFolder;
	String webhookAgentId = "webhook_git_testng";

	public void prepareOfflineAgent(String version) throws IOException, InsightsCustomException {
		String folderPath = new File(ApplicationConfigProvider.getInstance().getAgentDetails().getOfflineAgentPath()
				+ File.separator + version).getCanonicalPath();
		File offlineAgentFolder = new File(folderPath);
		if (offlineAgentFolder.exists()) {
			FileUtils.deleteDirectory(offlineAgentFolder);
		}
		offlineAgentFolder.mkdirs();
		String packageURL = ApplicationConfigProvider.getInstance().getAgentDetails().getRepoUrl();
		if (packageURL == null || packageURL.length() == 0) {
			throw new InsightsCustomException("Repo URL not configured in server configuration!");
		}
		packageURL = packageURL + "/" + version + "/" + "agents.zip";
		URL zipFileUrl = new URL(packageURL);
		File zip = File.createTempFile("agents_", ".zip", offlineAgentFolder);
		URLConnection conn;
		if (ApplicationConfigProvider.getInstance().getProxyConfiguration().isEnableProxy()) {
			SocketAddress addr = new InetSocketAddress(
					ApplicationConfigProvider.getInstance().getProxyConfiguration().getProxyHost(),
					ApplicationConfigProvider.getInstance().getProxyConfiguration().getProxyPort());
			Proxy proxy = new Proxy(Proxy.Type.HTTP, addr);
			conn = zipFileUrl.openConnection(proxy);
		} else {
			conn = zipFileUrl.openConnection();
		}
		try (InputStream in = new BufferedInputStream(conn.getInputStream(), 1024);
				OutputStream out = new BufferedOutputStream(new FileOutputStream(zip))) {
			copyInputStream(in, out);
		} catch (IOException e) {
			if (zip.exists()) {
				Path zipPath = Paths.get(zip.getPath());
				Files.delete(zipPath);
			}
			FileUtils.deleteDirectory(offlineAgentFolder);
			throw new IOException("Failed to download Package");
		}
		unzip(zip.getPath(), offlineAgentFolder.getPath());
		Files.delete(Paths.get(zip.toString()));
	}

	private void copyInputStream(InputStream in, OutputStream out) throws IOException {
		FileCopyUtils.copy(in, out);
		in.close();
		out.close();
	}

	public void unzip(String zipFilePath, String destDirectory) throws IOException {
		String destDirPath = new File(destDirectory).getCanonicalPath();
		File destDir = new File(destDirPath);
		if (!destDir.exists()) {
			destDir.mkdir();
		}
		try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(new File(zipFilePath).getCanonicalPath()))) {
			ZipEntry entry = zipIn.getNextEntry();
			// iterates over entries in the zip file
			while (entry != null) {
				String entryName = entry.getName();
				entryName = entryName.replaceFirst("agents/", "");
				String filePath = new File(destDirectory + File.separator + entryName).getCanonicalPath();
				if (!entry.isDirectory()) {
					// if the entry is a file, extracts it
					extractFile(zipIn, filePath);
				} else {
					// if the entry is a directory, make the directory
					File dir = new File(filePath);
					dir.mkdirs();
				}
				zipIn.closeEntry();
				entry = zipIn.getNextEntry();
			}
		} catch (Exception e) {
			log.error("Error while unzipping {}", e.getMessage());
		}
	}

	private void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
		try (BufferedOutputStream bos = new BufferedOutputStream(
				new FileOutputStream(new File(filePath).getCanonicalPath()));) {

			byte[] bytesIn = new byte[4096];
			int read = 0;
			while ((read = zipIn.read(bytesIn)) != -1) {
				bos.write(bytesIn, 0, read);
			}
		} catch (Exception e) {
			log.error("Error while extracting File {}", e.getMessage());
		}
	}

	public void prepareROIAgentToolData() {
		InsightsTools newtool = new InsightsTools();
		newtool.setCategory("ROI");
		newtool.setToolName("NEWRELIC");
		newtool.setToolConfigJson("{}");
		newtool.setIsActive(Boolean.TRUE);
		newtool.setAgentCommunicationQueue("NEWRELIC_MILESTONE_EXECUTION");
		outComeConfigDAL.saveInsightsTools(newtool);
	}

}
