/*******************************************************************************
 * Copyright 2020 Cognizant Technology Solutions
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
package com.cognizant.devops.platformcommons.core.enums;

public class FileDetailsEnum {

	public enum ReportTemplateFileType {
		HTML, JSON, CSS, WEBP
	}
	
	public enum ConfigurationFileType {
		JSON
	}

	public enum FileModule {
		CORRELATION(1), TRACEABILITY(1), TOOLDETAIL(1), AUDIT(1), DATAENRICHMENT(-1);

		private int value;

		FileModule(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}

		void setValue(int value) {
			this.value = value;
		}

	}

}
