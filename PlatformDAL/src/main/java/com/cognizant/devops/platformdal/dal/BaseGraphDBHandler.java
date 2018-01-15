package com.cognizant.devops.platformdal.dal;

import java.util.List;

import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;

public interface BaseGraphDBHandler{

    public StatementResult read(String query) throws DataDeleteException;
    public List<InsightsGraphNode> getNodes(String query) throws DataDeleteException;
    public List<InsightsGraphNode> getNodeWithRelationship(String query) throws DataDeleteException;
    public void write(String query) throws DataDeleteException;
    public void writeBulk(List<String> queries) throws DataDeleteException;
    public StatementResult execute(Transaction tx,String query);	
    public void delete(String query);
    public Session getSession();
    public void closeSession(Session session);
}
