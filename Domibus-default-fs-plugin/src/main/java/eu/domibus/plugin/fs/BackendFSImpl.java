package eu.domibus.plugin.fs;

import static eu.domibus.common.MessageStatus.*;

import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import eu.domibus.common.MessageReceiveFailureEvent;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageNotFoundException;
import eu.domibus.plugin.AbstractBackendConnector;
import eu.domibus.plugin.fs.ebms3.CollaborationInfo;
import eu.domibus.plugin.fs.exception.FSPluginException;
import eu.domibus.plugin.transformer.MessageRetrievalTransformer;
import eu.domibus.plugin.transformer.MessageSubmissionTransformer;

import org.springframework.beans.factory.annotation.Autowired;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.tika.mime.MimeTypeException;

import eu.domibus.common.MessageStatus;
import eu.domibus.common.MessageStatusChangeEvent;
import eu.domibus.plugin.fs.exception.FSSetUpException;

import javax.activation.DataHandler;

/**
 * File system backend integration plugin.
 *
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
public class BackendFSImpl extends AbstractBackendConnector<FSMessage, FSMessage> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(BackendFSImpl.class);
    
    private static final Set<MessageStatus> SENDING_MESSAGE_STATUSES = EnumSet.of(
            READY_TO_SEND, SEND_ENQUEUED, SEND_IN_PROGRESS, WAITING_FOR_RECEIPT,
            WAITING_FOR_RETRY, SEND_ATTEMPT_FAILED, ACKNOWLEDGED, ACKNOWLEDGED_WITH_WARNING,
            SEND_FAILURE
    );
    
    // receiving statuses should be REJECTED, RECEIVED_WITH_WARNINGS, DOWNLOADED, DELETED, RECEIVED

    @Autowired
    private FSMessageTransformer defaultTransformer;
    
    @Autowired
    private FSFilesManager fsFilesManager;
    
    @Autowired
    private FSPluginProperties fsPluginProperties;
    
    private final Map<String, Pattern> domainPatternCache = new HashMap<>();

    /**
     * Creates a new <code>BackendFSImpl</code>.
     *
     * @param name Connector name
     */
    public BackendFSImpl(String name) {
        super(name);
    }

    /**
     * The implementations of the transformer classes are responsible for
     * transformation between the native backend formats and
     * eu.domibus.plugin.Submission.
     *
     * @return MessageSubmissionTransformer
     */
    @Override
    public MessageSubmissionTransformer<FSMessage> getMessageSubmissionTransformer() {
        return this.defaultTransformer;
    }

    /**
     * The implementations of the transformer classes are responsible for
     * transformation between the native backend formats and
     * eu.domibus.plugin.Submission.
     *
     * @return MessageRetrievalTransformer
     */
    @Override
    public MessageRetrievalTransformer<FSMessage> getMessageRetrievalTransformer() {
        return this.defaultTransformer;
    }

    @Override
    public void deliverMessage(String messageId) {
        LOG.debug("Delivering File System Message {}", messageId);
        FSMessage fsMessage;

        // Download message
        try {
            fsMessage = downloadMessage(messageId, null);
        } catch (MessageNotFoundException e) {
            throw new FSPluginException("Unable to download message " + messageId, e);
        }

        // Persist message
        String domain = resolveDomain(fsMessage);
        try (FileObject rootDir = fsFilesManager.setUpFileSystem(domain);
                FileObject incomingFolder = fsFilesManager.getEnsureChildFolder(rootDir, FSFilesManager.INCOMING_FOLDER)) {
            
            boolean multiplePayloads = fsMessage.getDataHandlers().size() > 1;

            for (Map.Entry<String, DataHandler> entry : fsMessage.getDataHandlers().entrySet()) {
                DataHandler dataHandler = entry.getValue();
                String contentId  = entry.getKey();
                String fileName = getFileName(multiplePayloads, messageId, contentId, dataHandler);

                try (FileObject fileObject = incomingFolder.resolveFile(fileName);
                     FileContent fileContent = fileObject.getContent()) {
                    dataHandler.writeTo(fileContent.getOutputStream());
                }
            }
        } catch (IOException | FSSetUpException ex) {
            throw new FSPluginException("An error occurred persisting downloaded message " + messageId, ex);
        }
    }

    private String getFileName(boolean multiplePayloads, String messageId, String contentId, DataHandler dataHandler) {
        String fileName = messageId;
        if (multiplePayloads) {
            fileName += "_" + contentId.replaceFirst("cid:", "");
        }
        fileName += getFileNameExtension(dataHandler);
        return fileName;
    }

    private String getFileNameExtension(DataHandler dataHandler) {
        String extension = "";
        try {
            String mimeType = dataHandler.getContentType();
            extension = FSMimeTypeHelper.getExtension(mimeType);
        } catch (MimeTypeException ex) {
            LOG.warn("Error parsing MIME type", ex);
        }
        return extension;
    }

    private String resolveDomain(FSMessage fsMessage) {
        CollaborationInfo collaborationInfo = fsMessage.getMetadata().getCollaborationInfo();
        String service = collaborationInfo.getService().getValue();
        String action = collaborationInfo.getAction();
        return resolveDomain(service, action);
    }

    String resolveDomain(String service, String action) {
        String serviceAction = service + "#" + action;
        List<String> domains = fsPluginProperties.getDomains();
        for (String domain : domains) {
            Pattern domainExpressionPattern = getDomainPattern(domain);
            if (domainExpressionPattern != null) {
                boolean domainMatches = domainExpressionPattern.matcher(serviceAction).matches();
                if (domainMatches) {
                    return domain;
                }
            }
        }
        return null;
    }
    
    private Pattern getDomainPattern(String domain) {
        if (domainPatternCache.containsKey(domain)) {
            return domainPatternCache.get(domain);
        } else {
            String domainExpression = fsPluginProperties.getExpression(domain);
            Pattern domainExpressionPattern = null;
            if (StringUtils.isNotEmpty(domainExpression)) {
                try {
                    domainExpressionPattern = Pattern.compile(domainExpression);
                } catch (PatternSyntaxException e) {
                    LOG.warn("Invalid domain expression for " + domain, e);
                }
            }

            // domainExpressionPattern may be null, we should still cache null and return it
            domainPatternCache.put(domain, domainExpressionPattern);
            return domainExpressionPattern;
        }
    }

    @Override
    public void messageReceiveFailed(MessageReceiveFailureEvent messageReceiveFailureEvent) {
        LOG.debug("TODO: messageReceiveFailed implementation");
    }

    @Override
    public void messageSendFailed(String messageId) {
        LOG.debug("TODO: messageSendFailed implementation");
    }

    @Override
    public void messageSendSuccess(String messageId) {
        LOG.debug("TODO: messageSendSuccess implementation");
    }

    @Override
    public void messageStatusChanged(MessageStatusChangeEvent event) {
        LOG.debug("Message {} changed status from {} to {}", event.getMessageId(), event.getFromStatus(), event.getToStatus());
        
        if (isSendingEvent(event)) {
            boolean fileRenamed = renameMessageFile(null, event.getMessageId(), event.getToStatus());
            if (!fileRenamed) {
                for (String domain : fsPluginProperties.getDomains()) {
                    fileRenamed = renameMessageFile(domain, event.getMessageId(), event.getToStatus());
                    if (fileRenamed) {
                        break;
                    }
                }
            }
        }
    }
    
    private boolean isSendingEvent(MessageStatusChangeEvent event) {
        return SENDING_MESSAGE_STATUSES.contains(event.getToStatus());
    }
    
    private boolean renameMessageFile(String domain, String messageId, MessageStatus status) {
        try (FileObject rootDir = fsFilesManager.setUpFileSystem(domain);
                FileObject outgoingFolder = fsFilesManager.getEnsureChildFolder(rootDir, FSFilesManager.OUTGOING_FOLDER)) {
            
            FileObject[] files = fsFilesManager.findAllDescendantFiles(outgoingFolder);
            
            FileObject targetFile = null;
            for (FileObject file : files) {
                String baseName = file.getName().getBaseName();
                if (FSFileNameHelper.isMessageRelated(baseName, messageId)) {
                    targetFile = file;
                    break;
                }
            }
            
            if (targetFile != null) {
                String baseName = targetFile.getName().getBaseName();
                String newName = FSFileNameHelper.deriveFileName(baseName, status);
                fsFilesManager.renameFile(targetFile, newName);
                
                fsFilesManager.closeAll(files);
                return true;
            } else {
                fsFilesManager.closeAll(files);
            }
        } catch (FileSystemException ex) {
            LOG.error("Error renaming file", ex);
        } catch (FSSetUpException ex) {
            LOG.error("Error setting up folders for domain: " + domain, ex);
        }
        
        return false;
    }

}