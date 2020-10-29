/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.cognizant.devops.platformcommons.dal.ai;

public class H2ORestApiUrls {
	
	private H2ORestApiUrls() {}
	public static final  String POST_FILE="/3/PostFile";
	public static final  String PARSE_DATA="/3/Parse";
	public static final String SPLIT_FRAME="/3/SplitFrame";
	public static final String BUILD_AUTOML="/99/AutoMLBuilder";
	public static final String TRAIN_WORD2VEC="/3/ModelBuilders/word2vec";
	public static final String TRANSFORM_WORD2VEC="/3/Word2VecTransform";
	public static final String LEADERBOARD_URL="/99/AutoML/";
	public static final String PREDICTION_URL="/3/Predictions/models/";
	public static final String  DATA_FRAME="/3/Frames/";
	public static final String DOWNLOAD_MOJO="/3/Models/";
	
	
}
