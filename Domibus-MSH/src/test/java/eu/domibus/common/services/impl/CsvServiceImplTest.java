package eu.domibus.common.services.impl;

import eu.domibus.api.csv.CsvException;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.NotificationStatus;
import eu.domibus.common.model.logging.MessageLogInfo;
import eu.domibus.ebms3.common.model.MessageType;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Tiago Miguel
 * @since 4.0
 */

@RunWith(JMockit.class)
public class CsvServiceImplTest {

    @Tested
    CsvServiceImpl csvService;

    @Test
    public void testExportToCsv_EmptyList() throws CsvException {
        // Given
        // When
        final String exportToCSV = csvService.exportToCSV(new ArrayList<>());

        // Then
        Assert.assertTrue(exportToCSV.isEmpty());
    }

    @Test
    public void testExportToCsv_NullList() throws CsvException {
        // Given
        // When
        final String exportToCSV = csvService.exportToCSV(null);

        // Then
        Assert.assertTrue(exportToCSV.isEmpty());
    }

    @Test
    public void testExportToCsv() throws CsvException {
        // Given
        Date date = new Date();
        List<MessageLogInfo> messageLogInfoList = getMessageList(MessageType.USER_MESSAGE, date);

        // When
        final String exportToCSV = csvService.exportToCSV(messageLogInfoList);

        // Then
        Assert.assertTrue(exportToCSV.contains("Conversation Id,From Party Id,To Party Id,Original Sender,Final Recipient,Ref To Message Id,Message Id,Message Status,Notification Status,Msh Role,Message Type,Deleted,Received,Send Attempts,Send Attempts Max,Next Attempt,Failed,Restored"));
        Assert.assertTrue(exportToCSV.contains("conversationId,fromPartyId,toPartyId,originalSender,finalRecipient,refToMessageId,messageId,ACKNOWLEDGED,NOTIFIED,RECEIVING,USER_MESSAGE,"+date+","+date+",1,5,"+date+","+date+","+date));
    }

    private List<MessageLogInfo> getMessageList(MessageType messageType, Date date) {
        List<MessageLogInfo> result = new ArrayList<>();
        MessageLogInfo messageLog = new MessageLogInfo("messageId", MessageStatus.ACKNOWLEDGED,
                NotificationStatus.NOTIFIED, MSHRole.RECEIVING, messageType, date, date, 1, 5, date,
                "conversationId", "fromPartyId", "toPartyId", "originalSender", "finalRecipient",
                "refToMessageId", date, date);
        result.add(messageLog);
        return result;
    }
}