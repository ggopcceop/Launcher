/*
 * Copyright (C) 2014 Kime
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
        try {
            InputStream inStream = this.getClass().getResourceAsStream("/g2.crt");
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) cf.generateCertificate(inStream);

            inStream.close();

            return new X509Certificate[]{cert};
        } catch (CertificateException ex) {
            Logger.getLogger(LauncherUtil.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (IOException ex) {
            Logger.getLogger(KimeTrustManager.class.getName()).log(Level.SEVERE, null, ex);
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
        try {
            xcs[0].verify(ca[0].getPublicKey());
        } catch (InvalidKeyException ex) {
            throw new CertificateException("Certificate not trusted", ex);
        } catch (NoSuchAlgorithmException ex) {
            throw new CertificateException("Certificate not trusted", ex);
        } catch (NoSuchProviderException ex) {
            throw new CertificateException("Certificate not trusted", ex);
        } catch (SignatureException ex) {
            throw new CertificateException("Certificate not trusted", ex);
        } catch (CertificateException ex) {
            throw new CertificateException("Certificate not trusted", ex);
        }
    }
}
