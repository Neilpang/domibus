package eu.domibus.api.usermessage;

import eu.domibus.api.usermessage.domain.UserMessage;

import java.util.Date;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public interface UserMessageService {

    String getFinalRecipient(final String messageId);

    List<String> getFailedMessages(String finalRecipient);

    Long getFailedMessageElapsedTime(String messageId);

    void restoreFailedMessage(String messageId);

    List<String> restoreFailedMessagesDuringPeriod(Date begin, Date end, String finalRecipient);

    void deleteFailedMessage(String messageId);

    void delete(List<String> messageIds);

    void deleteMessage(String messageId);

    void scheduleSending(String messageId);

    void scheduleSending(String messageId, Long delay);

    /**
     * Gets a User Message based on the {@code messageId}
     * @param messageId User Message Identifier
     * @return User Message {@link UserMessage}
     */
    UserMessage getMessage(String messageId);
}
