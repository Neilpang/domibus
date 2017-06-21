package eu.domibus.web.rest.ro;

import eu.domibus.api.routing.RoutingCriteria;

import java.util.List;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
public class MessageFilterRO {

    private int entityId;

    private int index;

    private List<RoutingCriteria> routingCriterias;

    private String backendName;

    public int getEntityId() {
        return entityId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public List<RoutingCriteria> getRoutingCriterias() {
        return routingCriterias;
    }

    public void setRoutingCriterias(List<RoutingCriteria> routingCriterias) {
        this.routingCriterias = routingCriterias;
    }

    public String getBackendName() {
        return backendName;
    }

    public void setBackendName(String backendName) {
        this.backendName = backendName;
    }
}