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

package com.cognizant.devops.platformdal.upshiftassessment;

import com.cognizant.devops.platformdal.core.BaseDAL;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;

public class UpshiftAssessmentConfigDAL extends BaseDAL {
    private static Logger log = LogManager.getLogger(UpshiftAssessmentConfigDAL.class);

    public int saveUpshiftAssessment(UpshiftAssessmentConfig upshiftAssessmentConfig) {
        int id = -1;
        try (Session session = getSessionObj()) {
            session.beginTransaction();
            id = (int) session.save(upshiftAssessmentConfig);
            session.getTransaction().commit();
            log.debug("Transaction ID == {}" , id);
            return id;
        } catch (Exception e) {
            return id;
        }
    }

    public UpshiftAssessmentConfig fetchUpshiftAssessmentDetailsByWorkflowId(String workflowId) {
        try (Session session = getSessionObj()) {
            Query<UpshiftAssessmentConfig> createQuery = session.createQuery(
                    "FROM UpshiftAssessmentConfig gd where gd.workflowConfig.workflowId = :workflowId", UpshiftAssessmentConfig.class);
            createQuery.setParameter("workflowId", workflowId);
            return createQuery.uniqueResult();
        } catch (Exception e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    public UpshiftAssessmentConfig fetchUpshiftAssessmentByUuid(String uuid) {
        try (Session session = getSessionObj()) {
            Query<UpshiftAssessmentConfig> createQuery = session.createQuery(
                    "FROM UpshiftAssessmentConfig gd where gd.upshiftUuid = :uuid", UpshiftAssessmentConfig.class);
            createQuery.setParameter("uuid", uuid);
            return createQuery.uniqueResult();
        } catch (Exception e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    public void updateUpshiftAssessmentConfig(UpshiftAssessmentConfig config) {
        try (Session session = getSessionObj()) {
            session.beginTransaction();
            session.update(config);
            session.getTransaction().commit();
        } catch (Exception e) {
            log.error(e.getMessage());
            throw e;
        }
    }
}
