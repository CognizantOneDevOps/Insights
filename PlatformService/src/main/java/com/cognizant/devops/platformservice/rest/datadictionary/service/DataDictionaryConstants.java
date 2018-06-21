package com.cognizant.devops.platformservice.rest.datadictionary.service;

public interface DataDictionaryConstants {

	String GET_TOOL_PROPERTIES_QUERY = "MATCH(n:__CategoryName__:__ToolName__:DATA) return keys(n) limit 1";
	String GET_TOOLS_RELATIONSHIP_QUERY = "MATCH (n:__StartToolCategory__:__StartToolName__)-[r]->"
			+ "(m:__EndToolCategory__:__EndToolName__) return type(r),r LIMIT 1";
	String UUID = "uuid";
	String EXEC_ID = "execId";
}
