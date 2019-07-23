package com.cognizant.devops.platformneo4jbackuputility.neo4j.tool;

import org.eclipse.collections.api.map.primitive.LongLongMap;
import org.eclipse.collections.api.map.primitive.MutableLongLongMap;
import org.eclipse.collections.impl.map.mutable.primitive.LongLongHashMap;
import org.neo4j.graphdb.*;
import org.neo4j.helpers.Exceptions;
import org.neo4j.helpers.collection.Iterables;
import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.helpers.collection.Pair;
import org.neo4j.io.fs.FileUtils;
import org.neo4j.kernel.impl.store.id.IdGeneratorFactory;
import org.neo4j.kernel.impl.store.id.IdType;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.neo4j.unsafe.batchinsert.*;
import org.neo4j.unsafe.batchinsert.internal.*;
import org.neo4j.values.storable.Value;
import org.neo4j.graphdb.factory.*;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StoreCopy {

    private static final Label[] NO_LABELS = new Label[0];
	//private static PrintWriter logs;
	private static Logger LOG = LogManager.getLogger("RollingFile");
	//private static Logger LOG_SKIPENTRIES = LogManager.getLogger("RollingFileSkipEntries");

	public static String getArgument(String[] args, int index, Properties properties, String key) {
        if (args.length > index) return args[index];
        return properties.getProperty(key);
    }

	public static Set<String> splitToSet(String value) {
        if (value == null || value.trim().isEmpty()) return emptySet();
        return new HashSet<>(asList(value.trim().split(", *")));
    }

    interface Flusher {
        void flush();
    }

	public static void copyStore(String sourceDir, String targetDir, Set<String> ignoreRelTypes,
			Set<String> ignoreProperties, Set<String> ignoreLabels, Set<String> deleteNodesWithLabels,
			boolean stableNodeIds) throws Exception {
        final File target = new File(targetDir);
        final File source = new File(sourceDir);
		LOG.debug("Inside copyStore");
        if (target.exists()) {
            // FileUtils.deleteRecursively(target);
			throw new IllegalArgumentException("Target Directory already exists " + target);
        }
        if (!source.exists()) throw new IllegalArgumentException("Source Database does not exist " + source);

        Pair<Long, Long> highestIds = getHighestNodeId(source);
		String pageCacheSize = System.getProperty("dbms.pagecache.memory", "2G");
		BatchInserter targetDb = BatchInserters.inserter(target,
				MapUtil.stringMap("dbms.pagecache.memory", pageCacheSize));
		BatchInserter sourceDb = BatchInserters.inserter(source, MapUtil.stringMap("dbms.pagecache.memory",
				System.getProperty("dbms.pagecache.memory.source", pageCacheSize)));
        Flusher flusher = getFlusher(sourceDb);

		LongLongMap copiedNodeIds = copyNodes(sourceDb, targetDb, ignoreProperties, ignoreLabels, deleteNodesWithLabels,
				highestIds.first(), flusher, stableNodeIds);
        copyRelationships(sourceDb, targetDb, ignoreRelTypes, ignoreProperties, copiedNodeIds, highestIds.other(), flusher);
        targetDb.shutdown();
        try {
            sourceDb.shutdown();
        } catch (Exception e) {
			LOG.debug("Noncritical error closing the source database:%n {} ", e.getMessage());
        }
		if (stableNodeIds)
			copyIndex(source, target);
    }

	private static Flusher getFlusher(BatchInserter db) {
        try {
            Field delegate = FileSystemClosingBatchInserter.class.getDeclaredField("delegate");
            delegate.setAccessible(true);
            db = (BatchInserter)delegate.get(db);
            Field field = BatchInserterImpl.class.getDeclaredField("recordAccess");
            field.setAccessible(true);
            final DirectRecordAccessSet recordAccessSet = (DirectRecordAccessSet) field.get(db);
            final Field cacheField = DirectRecordAccess.class.getDeclaredField("batch");
            cacheField.setAccessible(true);
            return new Flusher() {
                @Override public void flush() {
                    try {
                        ((Map) cacheField.get(recordAccessSet.getNodeRecords())).clear();
                        ((Map) cacheField.get(recordAccessSet.getRelRecords())).clear();
                        ((Map) cacheField.get(recordAccessSet.getPropertyRecords())).clear();
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Error clearing cache "+cacheField,e);
                    }
                }
            };
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException("Error accessing cache field ", e);
        }
    }

	private static Pair<Long, Long> getHighestNodeId(File source) {
        GraphDatabaseAPI api = (GraphDatabaseAPI) new GraphDatabaseFactory().newEmbeddedDatabase(source);
        IdGeneratorFactory idGenerators = api.getDependencyResolver().resolveDependency(IdGeneratorFactory.class);
        long highestNodeId = idGenerators.get(IdType.NODE).getHighestPossibleIdInUse();
        long highestRelId = idGenerators.get(IdType.RELATIONSHIP).getHighestPossibleIdInUse();
        api.shutdown();
        return Pair.of(highestNodeId, highestRelId);
    }

	private static void copyIndex(File source, File target) throws IOException {
        final File indexFile = new File(source, "index.db");
        if (indexFile.exists()) {
            FileUtils.copyFile(indexFile, new File(target, "index.db"));
        }
        final File indexDir = new File(source, "index");
        if (indexDir.exists()) {
            FileUtils.copyRecursively(indexDir, new File(target, "index"));
        }
    }

	private static void copyRelationships(BatchInserter sourceDb, BatchInserter targetDb, Set<String> ignoreRelTypes,
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
                if (e instanceof org.neo4j.kernel.impl.store.InvalidRecordException && e.getMessage().endsWith("not in use")) {
                    notFound++;
                } else {
                    addLog(rel, "copy Relationship: " + (relId - 1) + "-[:" + type + "]" + "->?", e.getMessage());
                }
            }
            if (relId % 10000 == 0) {
				//LOG.debug(".");
				//logs.flush();
            }
            if (relId % 500000 == 0) {
                flusher.flush();
				LOG.debug(" {} / {} ({}%%) unused {} removed {}%n", relId, highestRelId, percent(relId, highestRelId),
						notFound, removed);
            }
        }
        time = Math.max(1,(System.currentTimeMillis() - time)/1000);
		LOG.debug(" {} / {} ({}%%) unused {} removed {}%n", relId, highestRelId, percent(relId, highestRelId), notFound,
				removed);
    }

	private static int percent(Number part, Number total) {
        return (int) (100 * part.floatValue() / total.floatValue());
    }

	private static long firstNode(BatchInserter sourceDb, long highestNodeId) {
        long node = -1;
        while (++node <= highestNodeId) {
            if (sourceDb.nodeExists(node) && !sourceDb.getNodeProperties(node).isEmpty()) return node;
        }
        return -1;
    }

	private static void flushCache(BatchInserter sourceDb, long node) {
        Map<String, Object> nodeProperties = sourceDb.getNodeProperties(node);
        Iterator<Map.Entry<String, Object>> iterator = nodeProperties.entrySet().iterator();
        if (iterator.hasNext()) {
            Map.Entry<String, Object> firstProp = iterator.next();
            sourceDb.nodeHasProperty(node,firstProp.getKey());
            sourceDb.setNodeProperty(node, firstProp.getKey(), firstProp.getValue()); // force flush
			LOG.debug(" flush");
        }
    }

	private static boolean createRelationship(BatchInserter targetDb, BatchInserter sourceDb, BatchRelationship rel,
			Set<String> ignoreProperties, LongLongMap copiedNodeIds) {
        long startNodeId = copiedNodeIds.get(rel.getStartNode());
        long endNodeId = copiedNodeIds.get(rel.getEndNode());
        if (startNodeId == -1L || endNodeId == -1L) return false;
        final RelationshipType type = rel.getType();
        try {
            Map<String, Object> props = getProperties(sourceDb.getRelationshipProperties(rel.getId()), ignoreProperties);
			//            if (props.isEmpty()) props = Collections.<String,Object>singletonMap("old_id",rel.getId()); else props.put("old_id",rel.getId());
            targetDb.createRelationship(startNodeId, endNodeId, type, props);
            return true;
        } catch (Exception e) {
            addLog(rel, "create Relationship: " + startNodeId + "-[:" + type + "]" + "->" + endNodeId, e.getMessage());
            return false;
        }
    }

	private static LongLongMap copyNodes(BatchInserter sourceDb, BatchInserter targetDb, Set<String> ignoreProperties,
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
                    if (labelInSet(sourceDb.getNodeLabels(node),deleteNodesWithLabels)) {
                        removed ++;
                    } else {
                        long newNodeId=node;
                        if (stableNodeIds) {
                            targetDb.createNode(node, getProperties(sourceDb.getNodeProperties(node), ignoreProperties), labelsArray(sourceDb, node, ignoreLabels));
                        } else {
                            newNodeId = targetDb.createNode(getProperties(sourceDb.getNodeProperties(node), ignoreProperties), labelsArray(sourceDb, node, ignoreLabels));
                        }
                        copiedNodes.put(node,newNodeId);
                    }
                } else {
                    notFound++;
                }
            } catch (Exception e) {
                if (e instanceof org.neo4j.kernel.impl.store.InvalidRecordException && e.getMessage().endsWith("not in use")) {
                    notFound++;
                } else addLog(node, e.getMessage());
            }
            node++;
            if (node % 10000 == 0) {
				//LOG.debug("node 10000 ");
            }
            if (node % 500000 == 0) {
                flusher.flush();
				//logs.flush();
				LOG.debug(" {} / {} ({}%%) unused {} removed {} %n", node, highestNodeId, percent(node, highestNodeId),
						notFound, removed);
            }
        }
        time = Math.max(1,(System.currentTimeMillis() - time)/1000);
		LOG.debug(
				"%n copying of {} node records took {} seconds ({} rec/s). Unused Records {} ({}%%). Removed Records {} ({}%%).%n",
                node, time, node/time, notFound, percent(notFound,node),removed, percent(removed,node));
        return copiedNodes;
    }

	private static boolean labelInSet(Iterable<Label> nodeLabels, Set<String> labelSet) {
        if (labelSet == null || labelSet.isEmpty()) return false;
        for (Label nodeLabel : nodeLabels) {
            if (labelSet.contains(nodeLabel.name())) return true;
        }
        return false;
    }

	private static Label[] labelsArray(BatchInserter db, long node, Set<String> ignoreLabels) {
        Collection<Label> labels = Iterables.asCollection(db.getNodeLabels(node));
        if (labels.isEmpty()) return NO_LABELS;
        if (!ignoreLabels.isEmpty()) {
            labels.removeIf(label -> ignoreLabels.contains(label.name()));
        }
        return labels.toArray(new Label[labels.size()]);
    }

	private static Map<String, Object> getProperties(Map<String, Object> pc, Set<String> ignoreProperties) {
        if (pc.isEmpty()) return Collections.emptyMap();
        if (!ignoreProperties.isEmpty()) {
            pc.keySet().removeAll(ignoreProperties);
        }
        return pc;
    }

	private static void addLog(BatchRelationship rel, String property, String message) {
		LOG.debug("{}.{} {}%n", rel, property, message);
    }

	private static void addLog(long node, String message) {
		LOG.debug("Node: {} {} ", node, message);
    }

	private static void addLog(PropertyContainer pc, String property, String message) {
		LOG.debug("{}.{} {}", pc, property, message);
    }
}


