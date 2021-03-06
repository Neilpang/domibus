package eu.domibus.ebms3.sender;

import com.google.common.base.Strings;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.configuration.security.ProxyAuthorizationPolicy;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.DispatchImpl;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.cxf.ws.policy.PolicyConstants;
import org.apache.neethi.Policy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Dispatch;
import javax.xml.ws.soap.SOAPBinding;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@Service
public class DispatchClientDefaultProvider implements DispatchClientProvider {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DispatchClientDefaultProvider.class);

    public static final String PMODE_KEY_CONTEXT_PROPERTY = "PMODE_KEY_CONTEXT_PROPERTY";
    public static final String ASYMMETRIC_SIG_ALGO_PROPERTY = "ASYMMETRIC_SIG_ALGO_PROPERTY";
    public static final String MESSAGE_ID = "MESSAGE_ID";
    public static final QName SERVICE_NAME = new QName("http://domibus.eu", "msh-dispatch-service");
    public static final QName PORT_NAME = new QName("http://domibus.eu", "msh-dispatch");

    @Autowired
    private TLSReader tlsReader;

    @Autowired
    @Qualifier("taskExecutor")
    private Executor executor;

    @Autowired
    @Qualifier("domibusProperties")
    private Properties domibusProperties;


    @Cacheable(value = "dispatchClient", key = "#endpoint + #pModeKey", condition = "#cacheable")
    @Override
    public Dispatch<SOAPMessage> getClient(String endpoint, String algorithm, Policy policy, final String pModeKey, boolean cacheable) {
        LOG.debug("Getting the dispatch client for endpoint [{}]" ,endpoint);

        final Dispatch<SOAPMessage> dispatch = createWSServiceDispatcher(endpoint);//service.createDispatch(PORT_NAME, SOAPMessage.class, javax.xml.ws.Service.Mode.MESSAGE);
        dispatch.getRequestContext().put(PolicyConstants.POLICY_OVERRIDE, policy);
        dispatch.getRequestContext().put(ASYMMETRIC_SIG_ALGO_PROPERTY, algorithm);
        dispatch.getRequestContext().put(PMODE_KEY_CONTEXT_PROPERTY, pModeKey);
        final Client client = ((DispatchImpl<SOAPMessage>) dispatch).getClient();
        final HTTPConduit httpConduit = (HTTPConduit) client.getConduit();
        final HTTPClientPolicy httpClientPolicy = httpConduit.getClient();
        httpConduit.setClient(httpClientPolicy);
        //ConnectionTimeOut - Specifies the amount of time, in milliseconds, that the consumer will attempt to establish a connection before it times out. 0 is infinite.
        int connectionTimeout = Integer.parseInt(domibusProperties.getProperty("domibus.dispatcher.connectionTimeout", "120000"));
        httpClientPolicy.setConnectionTimeout(connectionTimeout);
        //ReceiveTimeOut - Specifies the amount of time, in milliseconds, that the consumer will wait for a response before it times out. 0 is infinite.
        int receiveTimeout = Integer.parseInt(domibusProperties.getProperty("domibus.dispatcher.receiveTimeout", "120000"));
        httpClientPolicy.setReceiveTimeout(receiveTimeout);
        httpClientPolicy.setAllowChunking(Boolean.valueOf(domibusProperties.getProperty("domibus.dispatcher.allowChunking", "true")));
        httpClientPolicy.setChunkingThreshold(Integer.parseInt(domibusProperties.getProperty("domibus.dispatcher.chunkingThreshold", "104857600")));

        final TLSClientParameters params = tlsReader.getTlsClientParameters();
        if (params != null && endpoint.startsWith("https://")) {
            httpConduit.setTlsClientParameters(params);
        }
        final SOAPMessage result;

        String useProxy = domibusProperties.getProperty("domibus.proxy.enabled", "false");
        Boolean useProxyBool = Boolean.parseBoolean(useProxy);
        if (useProxyBool) {
            LOG.info("Usage of Proxy required");
            configureProxy(httpClientPolicy, httpConduit);
        } else {
            LOG.info("No proxy configured");
        }
        return dispatch;
    }

    protected Dispatch<SOAPMessage> createWSServiceDispatcher(String endpoint) {
        final javax.xml.ws.Service service = javax.xml.ws.Service.create(SERVICE_NAME);
        service.setExecutor(executor);
        service.addPort(PORT_NAME, SOAPBinding.SOAP12HTTP_BINDING, endpoint);
        final Dispatch<SOAPMessage> dispatch = service.createDispatch(PORT_NAME, SOAPMessage.class, javax.xml.ws.Service.Mode.MESSAGE);
        return dispatch;
    }

    protected void configureProxy(final HTTPClientPolicy httpClientPolicy, HTTPConduit httpConduit) {
        String httpProxyHost = domibusProperties.getProperty("domibus.proxy.http.host");
        String httpProxyPort = domibusProperties.getProperty("domibus.proxy.http.port");
        String httpProxyUser = domibusProperties.getProperty("domibus.proxy.user");
        String httpProxyPassword = domibusProperties.getProperty("domibus.proxy.password");
        String httpNonProxyHosts = domibusProperties.getProperty("domibus.proxy.nonProxyHosts");
        if (!Strings.isNullOrEmpty(httpProxyHost) && !Strings.isNullOrEmpty(httpProxyPort)) {
            httpClientPolicy.setProxyServer(httpProxyHost);
            httpClientPolicy.setProxyServerPort(Integer.valueOf(httpProxyPort));
            httpClientPolicy.setProxyServerType(org.apache.cxf.transports.http.configuration.ProxyServerType.HTTP);
        }
        if (!Strings.isNullOrEmpty(httpProxyHost)) {
            httpClientPolicy.setNonProxyHosts(httpNonProxyHosts);
        }
        if (!Strings.isNullOrEmpty(httpProxyUser) && !Strings.isNullOrEmpty(httpProxyPassword)) {
            ProxyAuthorizationPolicy policy = new ProxyAuthorizationPolicy();
            policy.setUserName(httpProxyUser);
            policy.setPassword(httpProxyPassword);
            httpConduit.setProxyAuthorization(policy);
        }
    }


}
