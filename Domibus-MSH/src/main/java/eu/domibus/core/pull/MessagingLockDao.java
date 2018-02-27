package eu.domibus.core.pull;

import eu.domibus.ebms3.common.model.MessagingLock;

import java.util.List;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public interface MessagingLockDao {

    String getNextPullMessageToProcess(String messageType, String initiator, String mpc, int maxResult);

    String getNextPullMessageToProcess(String messageType, String initiator, String mpc, List<Integer> lockedIds);

    void save(MessagingLock messagingLock);

    void delete(String messageId);

}
