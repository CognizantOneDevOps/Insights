/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.cognizant.devops.platformservice.rest.dataTagging;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cognizant.devops.platformdal.entity.definition.EntityDefinition;
import com.cognizant.devops.platformdal.entity.definition.EntityDefinitionDAL;
import com.cognizant.devops.platformdal.hierarchy.details.HierarchyDetails;
import com.cognizant.devops.platformdal.hierarchy.details.HierarchyDetailsDAL;
import com.cognizant.devops.platformservice.rest.dataTagging.util.DataProcessorUtil;
import com.cognizant.devops.platformservice.rest.neo4j.GraphDBService;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@RestController
@RequestMapping("/admin/hierarchyDetails")
public class HierarchyDetailsService {
	static Logger log = Logger.getLogger(GraphDBService.class.getName());

	@RequestMapping(value = "/addHierarchyDetails", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public JsonObject addHierarchyDetails(@RequestParam int rowId, @RequestParam String level1,
			@RequestParam String level2, @RequestParam String level3, @RequestParam String level4,
			@RequestParam String level5, @RequestParam String level6, @RequestParam String hierarchyName) {

		HierarchyDetails hierarchyDetails = new HierarchyDetails();
		hierarchyDetails.setRowId(rowId);
		if (!level1.equals("undefined")) {
			hierarchyDetails.setLevel_1(level1);
		}
		if (!level2.equals("undefined")) {
			hierarchyDetails.setLevel_2(level2);
		}
		if (!level3.equals("undefined")) {
			hierarchyDetails.setLevel_3(level3);
		}
		if (!level4.equals("undefined")) {
			hierarchyDetails.setLevel_4(level4);
		}
		if (!level5.equals("undefined")) {
			hierarchyDetails.setLevel_5(level5);
		}
		if (!level6.equals("undefined")) {
			hierarchyDetails.setLevel_6(level6);
		}
		hierarchyDetails.setHierarchyName(hierarchyName);

		HierarchyDetailsDAL hierarchyDetailsDAL = new HierarchyDetailsDAL();
		boolean status = hierarchyDetailsDAL.addHierarchyDetails(hierarchyDetails);
		if (status) {
			return PlatformServiceUtil.buildSuccessResponse();
		} else {
			return PlatformServiceUtil.buildFailureResponse("Unable to add Entity Definition for the request");
		}
	}

	@RequestMapping(value = "/removeHierarchyDetails", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject removeHierarchyDetails(@RequestParam String hierarchyName) {
		HierarchyDetailsDAL hierarchyDetailsDAL = new HierarchyDetailsDAL();
		return PlatformServiceUtil
				.buildSuccessResponseWithData(hierarchyDetailsDAL.deleteHierarchyDetails(hierarchyName));
	}

	@RequestMapping(value = "/fetchHierarchyDetails", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject fetchHierarchyDetails(@RequestParam String hierarchyName) {
		HierarchyDetailsDAL hierarchyDetailsDAL = new HierarchyDetailsDAL();
		HierarchyDetails results = hierarchyDetailsDAL.fetchHierarchyDetails(hierarchyName);
		return PlatformServiceUtil.buildSuccessResponseWithData(results);
	}

	@RequestMapping(value = "/fetchHierarchyDetailsByLevel", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject fetchHierarchyDetailsByLevel(@RequestParam String levelName) {
		HierarchyDetailsDAL hierarchyDetailsDAL = new HierarchyDetailsDAL();
		List<HierarchyDetails> results = hierarchyDetailsDAL.fetchHierarchyDetailsByLevelName(levelName);
		return PlatformServiceUtil.buildSuccessResponseWithData(results);
	}

	@RequestMapping(value = "/fetchAllHierarchyDetails", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject fetchAllHierarchyDetails() {
		HierarchyDetailsDAL hierarchyDetailsDAL = new HierarchyDetailsDAL();
		List<HierarchyDetails> results = hierarchyDetailsDAL.fetchAllEntityData();
		return PlatformServiceUtil.buildSuccessResponseWithData(results);
	}

	/**
	 * Avoid instantiations inside loops - Created Gson object outside of loop
	 * @return
	 */
	@RequestMapping(value = "/getHierarchyDetails", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject getHierarchyDetails() {
		JsonObject entityDataJsonObj = new JsonObject();
		HierarchyDetailsDAL hierarchyDetailsDAL = new HierarchyDetailsDAL();
		List<HierarchyDetails> hierarchyDetailsList = hierarchyDetailsDAL.fetchAllEntityData();
		EntityDefinitionDAL entityDefinitionDAL = new EntityDefinitionDAL();
		List<EntityDefinition> headerDetailsList = entityDefinitionDAL.fetchAllEntityDefination();
		JsonArray headerArray = new JsonArray();
		int columnIndex = 0;
		if (headerDetailsList.size() != 0) {
			for (EntityDefinition headerDetails : headerDetailsList) {
				columnIndex++;
				headerArray.add(headerDetails.getEntityName());
			}
		}
		JsonArray hierarchyDetailsArray = new JsonArray();
		Gson gson = new Gson();
		if (hierarchyDetailsList.size() != 0) {
			for (HierarchyDetails hierarchyDetails : hierarchyDetailsList) {
				List<String> hierarchyLabels = buildHierarchyList(columnIndex, hierarchyDetails);
				JsonObject hierarchyDetailsJsonObj = new JsonObject();
				hierarchyDetailsJsonObj.add("record", gson.toJsonTree(hierarchyLabels));
				hierarchyDetailsJsonObj.add("rowId", gson.toJsonTree(hierarchyDetails.getRowId()));
				hierarchyDetailsArray.add(hierarchyDetailsJsonObj);
			}
		} else {
			HierarchyDetails hierarchyDetails = new HierarchyDetails();
			hierarchyDetails.setLevel_1("");
			hierarchyDetails.setLevel_2("");
			hierarchyDetails.setLevel_3("");
			hierarchyDetails.setLevel_4("");
			hierarchyDetails.setLevel_5("");
			hierarchyDetails.setLevel_6("");
			List<String> hierarchyLabels = buildHierarchyList(columnIndex, hierarchyDetails);
			JsonObject hierarchyDetailsJsonObj = new JsonObject();
			hierarchyDetailsJsonObj.add("record", gson.toJsonTree(hierarchyLabels));
			hierarchyDetailsArray.add(hierarchyDetailsJsonObj);
		}
		entityDataJsonObj.add("headers", gson.toJsonTree(headerArray));
		entityDataJsonObj.add("records", gson.toJsonTree(hierarchyDetailsArray));
		return entityDataJsonObj;
	}

	private List<String> buildHierarchyList(int totalColumns, HierarchyDetails hierarchyDetails) {
		List<String> hierarchyLabelsList = new ArrayList<String>();
		List<String> hierarchyLabelsListFinal = new ArrayList<String>();

		if (hierarchyDetails.getLevel_1() != null) {
			hierarchyLabelsList.add(hierarchyDetails.getLevel_1());
		}
		if (hierarchyDetails.getLevel_2() != null) {
			hierarchyLabelsList.add(hierarchyDetails.getLevel_2());
		}
		if (hierarchyDetails.getLevel_3() != null) {
			hierarchyLabelsList.add(hierarchyDetails.getLevel_3());
		}
		if (hierarchyDetails.getLevel_4() != null) {
			hierarchyLabelsList.add(hierarchyDetails.getLevel_4());
		}
		if (hierarchyDetails.getLevel_5() != null) {
			hierarchyLabelsList.add(hierarchyDetails.getLevel_5());
		}
		if (hierarchyDetails.getLevel_6() != null) {
			hierarchyLabelsList.add(hierarchyDetails.getLevel_6());
		}

		for (int i = 0; i < totalColumns; i++) {
			hierarchyLabelsListFinal.add(hierarchyLabelsList.get(i));
		}
		return hierarchyLabelsListFinal;
	}

	@RequestMapping(value = "/fetchDistinctHierarchyName", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject fetchEntityHierarchyName() {
		HierarchyDetailsDAL hierarchyDetailsDAL = new HierarchyDetailsDAL();
		List<String> hierarchyList = hierarchyDetailsDAL.fetchDistinctHierarchyName();
		return PlatformServiceUtil.buildSuccessResponseWithData(hierarchyList);
	}

	@RequestMapping(value = "/uploadHierarchyDetails", headers=("content-type=multipart/*"), method = RequestMethod.POST,
			produces = MediaType.APPLICATION_JSON_UTF8_VALUE,consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public @ResponseBody JsonObject uploadHierarchyDetails(@RequestParam("file") MultipartFile file) {
		boolean status =false;
		if (!file.isEmpty()) {
			status=DataProcessorUtil.getInstance().readData(file);
		}
		if (status) {
			return PlatformServiceUtil.buildSuccessResponse();
		} else {
			return PlatformServiceUtil.buildFailureResponse("Failed to upload data");
		}


	}

	@RequestMapping(value = "/getAllHierarchyDetails", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject getAllHierarchyDetails() {
		Gson gson = new Gson();
		HierarchyDetailsDAL hierarchyDetailsDAL = new HierarchyDetailsDAL();
		List<HierarchyDetails> hierarchyDetailsList = hierarchyDetailsDAL.fetchAllEntityData();
		JsonObject hierarchyDetailsJsonObj = new JsonObject();
		hierarchyDetailsJsonObj.add("details", gson.toJsonTree(hierarchyDetailsList));		
		return hierarchyDetailsJsonObj;

	}

}
