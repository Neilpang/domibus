package eu.domibus.pki;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.security.cert.X509Certificate;
import java.util.List;


@Service
public class CRLServiceImpl implements CRLService {

    private static final Log LOG = LogFactory.getLog(CRLServiceImpl.class);

    @Autowired
    protected CRLUtil crlUtil;

    @Cacheable(value = "crlByCert", key = "#cert.subjectX500Principal.name")
    @Override
    public boolean isCertificateRevoked(X509Certificate cert) throws DomibusCRLException {
        List<String> crlDistributionPoints = crlUtil.getCrlDistributionPoints(cert);
        for (String crlDistributionPointUrl : crlDistributionPoints) {
            if(isCertificateRevoked(cert, crlDistributionPointUrl)) {
                return true;
            }
        }
        return false;
    }

    protected boolean isCertificateRevoked(X509Certificate cert, String crlDistributionPointURL) {
        X509CRL crl = crlUtil.downloadCRL(crlDistributionPointURL);
        if (crl.isRevoked(cert)) {
            LOG.debug("The pki is revoked by CRL: " + crlDistributionPointURL);
            return true;
        }
        return false;
    }

    @Cacheable(value = "crlByUrl", key = "#serialString.concat('-').concat(#crlDistributionPointURL)")
    @Override
    public boolean isCertificateRevoked(String serialString, String crlDistributionPointURL) {
        X509CRL crl = crlUtil.downloadCRL(crlDistributionPointURL);

        if (crl.getRevokedCertificates() == null) {
            LOG.debug("The CRL is null for the given pki");
            return false;
        }

        BigInteger certificateSerial = crlUtil.parseCertificateSerial(serialString);
        for (X509CRLEntry entry : crl.getRevokedCertificates()) {
            if (certificateSerial.equals(entry.getSerialNumber())) {
                LOG.debug("The pki is revoked by CRL: " + crlDistributionPointURL);
                return true;
            }
        }
        return false;
    }









}
