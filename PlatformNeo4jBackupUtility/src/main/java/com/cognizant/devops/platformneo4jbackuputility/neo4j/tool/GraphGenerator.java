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

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;
import java.util.Arrays;

/**
 * @author mh
 * @since 12.08.11
 */
public class GraphGenerator {
    public static final int MILLION = 1000 * 1000;

    public static void main(String[] args) {
        final GraphDatabaseService gdb = new GraphDatabaseFactory().newEmbeddedDatabase(new File("target/data"));
        createDatabase(gdb);
        gdb.shutdown();
    }
    public static void createDatabase(GraphDatabaseService graphdb) {
        int [] largeArray = new int[5000];
        Arrays.fill(largeArray, 101);
        long cpuTime = System.currentTimeMillis();
        Transaction tx = graphdb.beginTx();
        try {
            Node last = null, node = null;
            for (int i = 0; i < MILLION; i++) {
                node = graphdb.createNode();
                if (last != null) {
                    final Relationship rel = last.createRelationshipTo(node, Rels.values()[i % Rels.size()]);
                    rel.setProperty("array",largeArray);
                }
                last = node;
                if ((i % 100) == 0) {
                    System.out.print(".");
                    if ((i % 10000) == 0) {
                        tx.success();
                        tx.close();
                        System.out.println(" " + i);
                        tx = graphdb.beginTx();
                    }
                }
            }
        } finally {
            tx.success();
            tx.close();
        }
        System.out.println();
        long delta = (System.currentTimeMillis() - cpuTime);
        System.out.println("create-db delta = " + delta);
    }
}
