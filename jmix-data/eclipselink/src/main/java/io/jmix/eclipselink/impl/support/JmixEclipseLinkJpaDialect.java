/*
 * Copyright 2019 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.eclipselink.impl.support;

import com.google.common.base.Preconditions;
import io.jmix.data.persistence.DbmsSpecifics;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.persistence.jpa.JpaEntityManager;
import org.eclipse.persistence.sessions.UnitOfWork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaDialect;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Component("eclipselink_JmixEclipseLinkJpaDialect")
public class JmixEclipseLinkJpaDialect extends EclipseLinkJpaDialect {

    private static final long serialVersionUID = 7560990917358283944L;

    private static final Logger log = LoggerFactory.getLogger(JmixEclipseLinkJpaDialect.class);

    protected Environment environment;
    private DbmsSpecifics dbmsSpecifics;

    public JmixEclipseLinkJpaDialect(Environment environment, DbmsSpecifics dbmsSpecifics) {
        this.environment = environment;
        this.dbmsSpecifics = dbmsSpecifics;
        setLazyDatabaseTransaction(true);
    }

    @Override
    public Object beginTransaction(EntityManager entityManager, TransactionDefinition definition) throws PersistenceException, SQLException, TransactionException {
        Object result = super.beginTransaction(entityManager, definition);
        Preconditions.checkState(result == null, "Transactional data should be null for EclipseLink dialect");

        // Read default timeout every time - may be somebody wants to change it on the fly
        int defaultTimeout = 0;
        String defaultTimeoutProp = environment.getProperty("jmix.default-query-timeout-sec");
        if (!"0".equals(defaultTimeoutProp) && !StringUtils.isBlank(defaultTimeoutProp)) {
            try {
                defaultTimeout = Integer.parseInt(defaultTimeoutProp);
            } catch (NumberFormatException e) {
                log.error("Invalid jmix.defaultQueryTimeoutSec value", e);
            }
        }

        int timeoutSec = 0;
        if (definition.getTimeout() != TransactionDefinition.TIMEOUT_DEFAULT)
            timeoutSec = definition.getTimeout();
        else if (defaultTimeout != 0)
            timeoutSec = defaultTimeout;

        if (timeoutSec != 0) {
            log.trace("Applying query timeout {} sec", timeoutSec);
            if (entityManager instanceof JpaEntityManager) {
                UnitOfWork unitOfWork = ((JpaEntityManager) entityManager).getUnitOfWork();
                if (unitOfWork != null) {
                    //setup delay in seconds on unit of work
                    unitOfWork.setQueryTimeoutDefault(timeoutSec);
                }
            }

            String s = dbmsSpecifics.getDbmsFeatures().getTransactionTimeoutStatement();
            if (s != null) {
                Connection connection = entityManager.unwrap(Connection.class);
                try (Statement statement = connection.createStatement()) {
                    statement.execute(String.format(s, timeoutSec * 1000));
                }
            }
        }

        return new JmixEclipseLinkTransactionData(entityManager);
    }

    @Override
    public void cleanupTransaction(Object transactionData) {
        if (transactionData instanceof JmixEclipseLinkTransactionData) {
            ((JmixEclipseLinkTransactionData) transactionData).clearEntityManager();
        }
    }

    protected static class JmixEclipseLinkTransactionData {
        protected EntityManager entityManager;

        public JmixEclipseLinkTransactionData(EntityManager entityManager) {
            this.entityManager = entityManager;
        }

        public void clearEntityManager() {
            entityManager.clear();
        }
    }
}