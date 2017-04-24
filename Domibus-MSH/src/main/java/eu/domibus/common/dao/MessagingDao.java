/*
 * Copyright 2015 e-CODEX Project
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 * http://ec.europa.eu/idabc/eupl5
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.domibus.common.dao;

import eu.domibus.common.MessageStatus;
import eu.domibus.common.NotificationStatus;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.PartInfo;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.springframework.util.StringUtils.hasLength;

/**
 * @author Christian Koch, Stefan Mueller, Federico Martini
 * @since 3.0
 */
@Repository
public class MessagingDao extends BasicDao<Messaging> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessagingDao.class);

    public MessagingDao() {
        super(Messaging.class);
    }

    public UserMessage findUserMessageByMessageId(final String messageId) {

        final TypedQuery<UserMessage> query = this.em.createNamedQuery("Messaging.findUserMessageByMessageId", UserMessage.class);
        query.setParameter("MESSAGE_ID", messageId);

        return DataAccessUtils.singleResult(query.getResultList());
    }

    public Messaging findMessageByMessageId(final String messageId) {
        try {
            final TypedQuery<Messaging> query = em.createNamedQuery("Messaging.findMessageByMessageId", Messaging.class);
            query.setParameter("MESSAGE_ID", messageId);
            return query.getSingleResult();
        } catch (NoResultException nrEx) {
            LOG.debug("Could not find any message for message id[" + messageId + "]", nrEx);
            return null;
        }
    }

    /**
     * Clears the payloads data for the message with the given messageId.
     *
     * @param messageId
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void clearPayloadData(String messageId) {
        Query payloadsQuery = em.createNamedQuery("Messaging.findPartInfosForMessage");
        payloadsQuery.setParameter("MESSAGE_ID", messageId);
        List<PartInfo> results = payloadsQuery.getResultList();
        if (results.isEmpty()) {
            return;
        }
        List<PartInfo> databasePayloads = new ArrayList<>();

        for (PartInfo result : results) {
            if (hasLength(result.getFileName())) {
                new File(result.getFileName()).delete();
            } else {
                databasePayloads.add(result);
            }
        }
        if (!databasePayloads.isEmpty()) {
            final Query emptyQuery = em.createNamedQuery("Messaging.emptyPayloads");
            emptyQuery.setParameter("PARTINFOS", databasePayloads);
            emptyQuery.executeUpdate();
        }
        LOG.businessInfo(DomibusMessageCode.BUS_MESSAGE_PAYLOAD_DATA_CLEARED, messageId);
    }

}
