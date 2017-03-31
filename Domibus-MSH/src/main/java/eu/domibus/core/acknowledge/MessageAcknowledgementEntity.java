package eu.domibus.core.acknowledge;

import eu.domibus.ebms3.common.model.AbstractBaseEntity;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

/**
 * @author migueti, Cosmin Baciu
 * @since 3.3
 */
@Entity
@Table(name = "TB_MESSAGE_ACKNW")
@NamedQueries({
        @NamedQuery(name = "MessageAcknowledgement.findMessageAcknowledgementByMessageId",
                query = "select messageAcknowledge from MessageAcknowledgementEntity messageAcknowledge where messageAcknowledge.messageId = :MESSAGE_ID")
})
public class  MessageAcknowledgementEntity extends AbstractBaseEntity {

    @Column(name = "MESSAGE_ID")
    private String messageId;

    @Column(name = "FROM_VALUE")
    private String from;

    @Column(name = "TO_VALUE")
    private String to;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATE_DATE")
    private Date createDate;

    @Column(name = "CREATE_USER")
    private String createUser;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ACKNOWLEDGE_DATE")
    private Date acknowledgeDate;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_MSG_ACKNOWLEDGE")
    private Set<MessageAcknowledgementProperty> properties;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public Set<MessageAcknowledgementProperty> getProperties() {
        return properties;
    }

    public void setProperties(Set<MessageAcknowledgementProperty> properties) {
        this.properties = properties;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getAcknowledgeDate() {
        return acknowledgeDate;
    }

    public void setAcknowledgeDate(Date acknowledgeDate) {
        this.acknowledgeDate = acknowledgeDate;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        MessageAcknowledgementEntity messageAcknowledgementEntity = (MessageAcknowledgementEntity) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(messageId, messageAcknowledgementEntity.getMessageId())
                .append(from, messageAcknowledgementEntity.getFrom())
                .append(to, messageAcknowledgementEntity.getTo())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(messageId)
                .append(from)
                .append(to)
                .toHashCode();
    }
}