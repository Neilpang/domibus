package eu.domibus.ext.impl.v1.resource;

import eu.domibus.ext.api.v1.domain.MessageAcknowledgeDTO;
import eu.domibus.ext.api.v1.service.MessageAcknowledgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author baciu
 */
@RestController
@RequestMapping("v1/message/acknowledge")
public class MessageAcknowledgeResource {

    //the CustomAuthenticationInterceptor will be applied to this REST resource and it will perform the authentication

    @Autowired
    protected MessageAcknowledgeService messageAcknowledgeServiceExt;

    @RequestMapping(value = "/{messageId}", method = RequestMethod.PUT, produces = "application/json")
    public void acknowledgeMessage(@PathVariable String messageId) {
        messageAcknowledgeServiceExt.acknowledgeMessage(messageId);
    }

    @RequestMapping(value = "/{messageId}", method = RequestMethod.GET, produces = "application/json")
    public List<MessageAcknowledgeDTO> getMessagesAcknowledged(@PathVariable String messageId) {
        return messageAcknowledgeServiceExt.getAcknowledgedMessages(messageId);
    }
}
