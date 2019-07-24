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
package com.cognizant.devops.platformneo4jbackuputility.neo4j.tool;

import java.lang.reflect.Field;
import java.util.Map;

import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.internal.BatchInserterImpl;
import org.neo4j.unsafe.batchinsert.internal.DirectRecordAccess;
import org.neo4j.unsafe.batchinsert.internal.DirectRecordAccessSet;
import org.neo4j.unsafe.batchinsert.internal.FileSystemClosingBatchInserter;

public class FlusherImpl implements Flusher {
	DirectRecordAccessSet recordAccessSet;
	Field cacheField;

	public FlusherImpl(BatchInserter db) {
		try {
			Field delegate = FileSystemClosingBatchInserter.class.getDeclaredField("delegate");
			delegate.setAccessible(true);
			db = (BatchInserter) delegate.get(db);
			Field field = BatchInserterImpl.class.getDeclaredField("recordAccess");
			field.setAccessible(true);
			recordAccessSet = (DirectRecordAccessSet) field.get(db);
			cacheField = DirectRecordAccess.class.getDeclaredField("batch");
			cacheField.setAccessible(true);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException("Error accessing cache field ", e);
		}
	}

	@Override
	public void flush() {
		try {
			((Map) cacheField.get(recordAccessSet.getNodeRecords())).clear();
			((Map) cacheField.get(recordAccessSet.getRelRecords())).clear();
			((Map) cacheField.get(recordAccessSet.getPropertyRecords())).clear();
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Error clearing cache " + cacheField, e);
		}
	}
}
