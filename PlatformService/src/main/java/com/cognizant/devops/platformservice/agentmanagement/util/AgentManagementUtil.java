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
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class AgentManagementUtil {

	private static final AgentManagementUtil agentManagementUtil = new AgentManagementUtil();

	private AgentManagementUtil() {

	}

	public static AgentManagementUtil getInstance() {
		return agentManagementUtil;
	}

	public  JsonObject getAgentConfigfile(URL filePath, File targetDir) throws IOException  {

		if (!targetDir.exists()) {
			targetDir.mkdirs();
		}
		InputStream in = new BufferedInputStream(filePath.openStream(), 1024);
		File zip = File.createTempFile("agent_", ".zip", targetDir);
		OutputStream out = new BufferedOutputStream(new FileOutputStream(zip));
		copyInputStream(in, out);
		out.close();
		return getAgentConfigFile(zip, targetDir);
	}

	public  JsonObject getAgentConfigFile(File zip, File targetDir) throws IOException {

		if (!zip.exists()) {
			throw new IOException(zip.getAbsolutePath() + " does not exist");
		}
		if (!buildDirectory(targetDir)) {
			throw new IOException("Could not create directory: " + targetDir);
		}
		String filePath=null;

		try(ZipFile zipFile = new ZipFile(zip);){

			for (Enumeration entries = zipFile.entries(); entries.hasMoreElements();) {
				ZipEntry entry = (ZipEntry) entries.nextElement();
				File file = new File(targetDir, File.separator + entry.getName());

				if (!buildDirectory(file.getParentFile())) {
					throw new IOException("Could not create directory: " + file.getParentFile());
				}

				if (!entry.isDirectory()) {
					copyInputStream(zipFile.getInputStream(entry), new BufferedOutputStream(new FileOutputStream(file)));
					if(entry.getName().endsWith("config.json")){
						filePath = entry.getName();
					}

				} else {
					if (!buildDirectory(file)) {
						throw new IOException("Could not create directory: " + file);
					}
				}
			}
		}
		JsonObject jsonObject = readJsonFile(targetDir, filePath);
		return jsonObject;
	}

	private  JsonObject readJsonFile(File targetDir, String filePath) throws FileNotFoundException {
		JsonParser  parser = new JsonParser();
		Object obj = parser.parse(new FileReader(targetDir+"/"+filePath));
		JsonObject jsonObject = (JsonObject)obj;
		return jsonObject;
	}

	public  void copyInputStream(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int len = in.read(buffer);
		while (len >= 0) {
			out.write(buffer, 0, len);
			len = in.read(buffer);
		}
		in.close();
		out.close();
	}

	public  boolean buildDirectory(File file) {
		return file.exists() || file.mkdirs();
	}

	public   Path getAgentZipFolder(final Path sourceFolderPath, Path zipPath) throws Exception {
		final ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath.toFile()));
		Files.walkFileTree(sourceFolderPath, new SimpleFileVisitor<Path>() {
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				zos.putNextEntry(new ZipEntry(sourceFolderPath.relativize(file).toString()));
				Files.copy(file, zos);
				zos.closeEntry();
				return FileVisitResult.CONTINUE;
			}
		});
		zos.close();
		return zipPath;
	}

	

}
