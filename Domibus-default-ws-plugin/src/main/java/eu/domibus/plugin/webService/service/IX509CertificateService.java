package eu.domibus.plugin.webService.service;


import eu.domibus.plugin.webService.common.exception.AuthenticationException;

import java.security.cert.X509Certificate;

public interface IX509CertificateService {
    boolean isClientX509CertificateValid(final X509Certificate[] certificate) throws AuthenticationException;
}