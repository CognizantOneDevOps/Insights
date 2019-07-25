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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.collections.api.map.primitive.LongLongMap;
import org.eclipse.collections.api.map.primitive.MutableLongLongMap;
import org.eclipse.collections.impl.map.mutable.primitive.LongLongHashMap;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.helpers.collection.Iterables;
import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.helpers.collection.Pair;
import org.neo4j.io.fs.FileUtils;
import org.neo4j.kernel.impl.store.id.IdGeneratorFactory;
import org.neo4j.kernel.impl.store.id.IdType;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserters;
import org.neo4j.unsafe.batchinsert.BatchRelationship;

import com.cognizant.devops.platformneo4jbackuputility.utils.InsightsUtils;

public class Neo4jDataCopyService {

	private static final Label[] NO_LABELS = new Label[0];

	private static Logger LOG = LogManager.getLogger(Neo4jDataCopyService.class);

	public void startCopy(String sourceDir, String targetDir) throws Exception {

		Set<String> ignoreRelTypes = InsightsUtils.splitToSet(InsightsUtils.readProperty("rel_types_to_ignore"));
		Set<String> ignoreProperties = InsightsUtils.splitToSet(InsightsUtils.readProperty("properties_to_ignore"));
		Set<String> ignoreLabels = InsightsUtils.splitToSet(InsightsUtils.readProperty("labels_to_ignore"));
		Set<String> deleteNodesWithLabels = InsightsUtils.splitToSet(InsightsUtils.readProperty("labels_to_delete"));
		String keepNodeIdsParam = InsightsUtils.readProperty("keep_node_ids");
		boolean stableNodeIds = !("false".equalsIgnoreCase(keepNodeIdsParam));

		LOG.debug(
				"Copying from {} to {} ingoring rel-types {} ignoring properties {} ignoring labels {} removing nodes with labels {} keep node ids {} ",
				sourceDir, targetDir, ignoreRelTypes, ignoreProperties, ignoreLabels, deleteNodesWithLabels,
				stableNodeIds);

		final File target = new File(targetDir);
		final File source = new File(sourceDir);
		LOG.debug("Inside copyStore");
		if (target.exists()) {
			throw new IllegalArgumentException("Target Directory already exists " + target);
		}
		if (!source.exists()) {
			throw new IllegalArgumentException("Source Database does not exist " + source);
		}

		Pair<Long, Long> highestIds = getHighestNodeId(source);
		String pageCacheSize = System.getProperty("dbms.pagecache.memory", "2G");
		BatchInserter targetDb = BatchInserters.inserter(target,
				MapUtil.stringMap("dbms.pagecache.memory", pageCacheSize));

		BatchInserter sourceDb = BatchInserters.inserter(source, MapUtil.stringMap("dbms.pagecache.memory",
				System.getProperty("dbms.pagecache.memory.source", pageCacheSize)));
		Flusher flusher = new FlusherImpl(sourceDb);

		LongLongMap copiedNodeIds = transferDataNodes(sourceDb, targetDb, ignoreProperties, ignoreLabels,
				deleteNodesWithLabels, highestIds.first(), flusher, stableNodeIds);

		transferDataRelationships(sourceDb, targetDb, ignoreRelTypes, ignoreProperties, copiedNodeIds,
				highestIds.other(), flusher);

		targetDb.shutdown();
		try {
			sourceDb.shutdown();
		} catch (Exception e) {
			LOG.debug("Noncritical error closing the source database:%n {} ", e.getMessage());
		}
		if (stableNodeIds)
			transferDataIndex(source, target);
	}

	private Pair<Long, Long> getHighestNodeId(File source) {
		GraphDatabaseAPI api = (GraphDatabaseAPI) new GraphDatabaseFactory().newEmbeddedDatabase(source);
		IdGeneratorFactory idGenerators = api.getDependencyResolver().resolveDependency(IdGeneratorFactory.class);
		long highestNodeId = idGenerators.get(IdType.NODE).getHighestPossibleIdInUse();
		long highestRelId = idGenerators.get(IdType.RELATIONSHIP).getHighestPossibleIdInUse();
		api.shutdown();
		return Pair.of(highestNodeId, highestRelId);
	}

	private void transferDataIndex(File source, File target) throws IOException {
		LOG.debug(" Started copy Index ");
		final File indexFile = new File(source, "index.db");
		LOG.debug(" Index file exists  ==== " + indexFile.exists() + " path " + indexFile.getPath());
		if (indexFile.exists()) {
			FileUtils.copyFile(indexFile, new File(target, "index.db"));
		}
		final File indexDir = new File(source, "index");
		LOG.debug(" Index Directory exists  ==== " + indexFile.exists() + " path " + indexFile.getPath());
		if (indexDir.exists()) {
			FileUtils.copyRecursively(indexDir, new File(target, "index"));
		}
		LOG.debug("copy Index completed ");
	}

	private void transferDataRelationships(BatchInserter sourceDb, BatchInserter targetDb, Set<String> ignoreRelTypes,
			Set<String> ignoreProperties, LongLongMap copiedNodeIds, long highestRelId, Flusher flusher) {
		long time = System.currentTimeMillis();
		long relId = 0;
		long notFound = 0;
		long removed = 0;
		while (relId <= highestRelId) {
			BatchRelationship rel = null;
			String type = null;
			try {
				rel = sourceDb.getRelationshipById(relId++);
				type = rel.getType().name();
				if (!ignoreRelTypes.contains(type)) {
					if (!createRelationship(targetDb, sourceDb, rel, ignoreProperties, copiedNodeIds)) {
						removed++;
					}
				} else {
					removed++;
				}
			} catch (Exception e) {
				if (e instanceof org.neo4j.kernel.impl.store.InvalidRecordException
						&& e.getMessage().endsWith("not in use")) {
					notFound++;
				} else {
					addRelationshipLog(rel, "copy Relationship: " + (relId - 1) + "-[:" + type + "]" + "->?",
							e.getMessage(), Boolean.TRUE);
				}
			}
			/*if (relId % 10000 == 0) {
				LOG.debug(".");
			}*/
			if (relId % 500000 == 0) {
				flusher.flush();
				LOG.debug(" {} / {} ({}%) unused {} removed {} ", relId, highestRelId, percent(relId, highestRelId),
						notFound, removed);
			}
		}
		time = Math.max(1, (System.currentTimeMillis() - time) / 1000);
		LOG.debug(" {} / {} ({}%) unused {} removed {}", relId, highestRelId, percent(relId, highestRelId), notFound,
				removed);
	}

	private int percent(Number part, Number total) {
		return (int) (100 * part.floatValue() / total.floatValue());
	}

	private boolean createRelationship(BatchInserter targetDb, BatchInserter sourceDb, BatchRelationship rel,
			Set<String> ignoreProperties, LongLongMap copiedNodeIds) {
		long startNodeId = copiedNodeIds.get(rel.getStartNode());
		long endNodeId = copiedNodeIds.get(rel.getEndNode());
		if (startNodeId == -1L || endNodeId == -1L)
			return false;
		final RelationshipType type = rel.getType();
		try {
			Map<String, Object> props = filterProperties(sourceDb.getRelationshipProperties(rel.getId()),
					ignoreProperties);
			targetDb.createRelationship(startNodeId, endNodeId, type, props);
			return true;
		} catch (Exception e) {
			addRelationshipLog(rel, "create Relationship: " + startNodeId + "-[:" + type + "]" + "->" + endNodeId,
					e.getMessage(), Boolean.TRUE);
			return false;
		}
	}

	private LongLongMap transferDataNodes(BatchInserter sourceDb, BatchInserter targetDb, Set<String> ignoreProperties,
			Set<String> ignoreLabels, Set<String> deleteNodesWithLabels, long highestNodeId, Flusher flusher,
			boolean stableNodeIds) {
		MutableLongLongMap copiedNodes = stableNodeIds ? new LongLongHashMap() : new LongLongHashMap(10_000_000);
		long time = System.currentTimeMillis();
		long node = 0;
		long notFound = 0;
		long removed = 0;
		while (node <= highestNodeId) {
			try {
				if (sourceDb.nodeExists(node)) {
					if (labelInSet(sourceDb.getNodeLabels(node), deleteNodesWithLabels)) {
						removed++;
					} else {
						long newNodeId = node;
						if (stableNodeIds) {
							targetDb.createNode(node,
									filterProperties(sourceDb.getNodeProperties(node), ignoreProperties),
									filterLabelsArray(sourceDb, node, ignoreLabels));
						} else {
							newNodeId = targetDb.createNode(
									filterProperties(sourceDb.getNodeProperties(node), ignoreProperties),
									filterLabelsArray(sourceDb, node, ignoreLabels));
						}
						copiedNodes.put(node, newNodeId);
					}
				} else {
					notFound++;
				}
			} catch (Exception e) {
				if (e instanceof org.neo4j.kernel.impl.store.InvalidRecordException
						&& e.getMessage().endsWith("not in use")) {
					notFound++;
					//addNodeLog(node, e.getMessage(), Boolean.FALSE);
				} else {
					addNodeLog(node, e.getMessage(), Boolean.TRUE);
				}
			}
			node++;
			if (node % 10000 == 0) {
				//LOG.debug("node 10000 ");
			}
			if (node % 500000 == 0) {
				flusher.flush();
				//logs.flush();
				LOG.debug(" {} / {} ({}%) unused {} removed {} ", node, highestNodeId, percent(node, highestNodeId),
						notFound, removed);
			}
		}
		time = Math.max(1, (System.currentTimeMillis() - time) / 1000);
		LOG.debug(
				"copying of {} node records took {} seconds ({} rec/s). Unused Records {} ({}%). Removed Records {} ({}%).",
				node, time, node / time, notFound, percent(notFound, node), removed, percent(removed, node));
		return copiedNodes;
	}

	private boolean labelInSet(Iterable<Label> nodeLabels, Set<String> labelSet) {
		if (labelSet == null || labelSet.isEmpty())
			return false;
		for (Label nodeLabel : nodeLabels) {
			if (labelSet.contains(nodeLabel.name()))
				return true;
		}
		return false;
	}

	private Label[] filterLabelsArray(BatchInserter db, long node, Set<String> ignoreLabels) {
		Collection<Label> labels = Iterables.asCollection(db.getNodeLabels(node));
		if (labels.isEmpty())
			return NO_LABELS;
		if (!ignoreLabels.isEmpty()) {
			labels.removeIf(label -> ignoreLabels.contains(label.name()));
		}
		return labels.toArray(new Label[labels.size()]);
	}

	private Map<String, Object> filterProperties(Map<String, Object> pc, Set<String> ignoreProperties) {
		if (pc.isEmpty())
			return Collections.emptyMap();
		if (!ignoreProperties.isEmpty()) {
			pc.keySet().removeAll(ignoreProperties);
		}
		return pc;
	}

	private void addRelationshipLog(BatchRelationship rel, String property, String message, boolean isDebug) {
		if (isDebug) {
			LOG.debug("{}.{} {}%n", rel, property, message);
		} else {
			LOG.error("{}.{} {}%n", rel, property, message);
		}
	}

	private void addNodeLog(long node, String message, boolean isDebug) {
		if (isDebug) {
			LOG.debug("Node: {} {} ", node, message);
		} else {
			LOG.error("Node has some problem: {} {} ", node, message);
		}
	}
}
