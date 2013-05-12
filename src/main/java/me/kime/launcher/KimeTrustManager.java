package me.kime.launcher;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.X509TrustManager;

/**
 *
 * @author kime
 */
public class KimeTrustManager implements X509TrustManager {

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        try (InputStream inStream = this.getClass().getResourceAsStream("/g2.crt")) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) cf.generateCertificate(inStream);
            return new X509Certificate[]{cert};
        } catch (IOException | CertificateException ex) {
            Logger.getLogger(LauncherUtil.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public void checkClientTrusted(X509Certificate[] xcs, String string)
            throws CertificateException {
        throw new CertificateException("no trusted Certificate");
    }

    @Override
    public void checkServerTrusted(X509Certificate[] xcs, String string)
            throws CertificateException {
        X509Certificate[] ca = getAcceptedIssuers();
        for (X509Certificate cert : xcs) {
            try {
                cert.verify(ca[0].getPublicKey());
            } catch (NoSuchAlgorithmException | InvalidKeyException |
                    NoSuchProviderException | SignatureException ex) {
                throw new CertificateException("Certificate not trusted", ex);
            }
        }
    }
}