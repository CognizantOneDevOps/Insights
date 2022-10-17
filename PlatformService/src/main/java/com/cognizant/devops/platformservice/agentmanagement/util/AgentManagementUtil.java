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
package com.cognizant.devops.platformservice.agentmanagement.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.FileCopyUtils;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.AgentCommonConstant;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class AgentManagementUtil {

	private static final AgentManagementUtil agentManagementUtil = new AgentManagementUtil();
	private static Logger log = LogManager.getLogger(AgentManagementUtil.class);

	private AgentManagementUtil() {
	}

	public static AgentManagementUtil getInstance() {
		return agentManagementUtil;
	}

	public Path getAgentZipFolder(final Path sourceFolderPath, Path zipPath) throws IOException {
		try (final ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath.toFile()))) {
			Files.walkFileTree(sourceFolderPath, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					zos.putNextEntry(new ZipEntry(sourceFolderPath.relativize(file).toString().replace("\\", "/")));
					Files.copy(file, zos);
					zos.closeEntry();
					return FileVisitResult.CONTINUE;
				}
			});
		}
		return zipPath;
	}

	private void copyInputStream(InputStream in, OutputStream out) throws IOException {
		FileCopyUtils.copy(in, out);
		in.close();
		out.close();
	}

	private boolean buildDirectory(File file) {
		return file.exists() || file.mkdirs();
	}

	public JsonObject convertFileToJSON(File configFile) throws IOException {
		JsonObject jsonObject = new JsonObject();
		try {
			JsonElement jsonElement = JsonUtils.parseReader(new FileReader(configFile));
			jsonObject = jsonElement.getAsJsonObject();
		} catch (FileNotFoundException e) {
			log.error(e);
			throw new IOException("No Such file found -- " + configFile);
		}
		return jsonObject;
	}

	public String getAgentPackageFromGithub(URL zipFileUrl, File targetDir, String version) throws IOException {
		String message = "";
		if (targetDir.exists()) {
			throw new IOException(version + " - Package already exist!");
		} else {
			targetDir.mkdirs();
			File zip = File.createTempFile("agents_", ".zip", targetDir);
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
				FileUtils.deleteDirectory(targetDir);
				throw new IOException("Failed to download Package");
			}

			String result = unzipAgentsPackage(zip, targetDir);
			if (result.equalsIgnoreCase(PlatformServiceConstants.SUCCESS)) {
				message = "Downloaded agents package - " + version;
			}
		}
		return message;
	}

	private String unzipAgentsPackage(File zip, File targetDir) throws IOException {
		String message = "";
		if (!zip.exists()) {
			throw new IOException(zip.getAbsolutePath() + AgentCommonConstant.DOES_NOT_EXIST);
		}
		if (!buildDirectory(targetDir)) {
			throw new IOException("Could not create directory" + targetDir);
		}
		try (ZipFile zipFile = new ZipFile(zip);) {
			for (Enumeration entries = zipFile.entries(); entries.hasMoreElements();) {
				ZipEntry entry = (ZipEntry) entries.nextElement();
				String entryName = entry.getName();
				entryName = entryName.replaceFirst("agents/", "");
				File file = new File(targetDir, File.separator + entryName);
				if (!buildDirectory(file.getParentFile())) {
					throw new IOException(AgentCommonConstant.COULD_NOT_CREATE_DIR + file.getParentFile());
				}
				if (!entry.isDirectory()) {
					copyInputStream(zipFile.getInputStream(entry),
							new BufferedOutputStream(new FileOutputStream(file)));

				} else {
					if (!buildDirectory(file)) {
						throw new IOException(AgentCommonConstant.COULD_NOT_CREATE_DIR + file);
					}
				}

			}
			message = PlatformServiceConstants.SUCCESS;
		}
		if (zip.exists()) {
			Path path = Paths.get(zip.getPath());
			Files.delete(path);
		}
		return message;
	}
}
