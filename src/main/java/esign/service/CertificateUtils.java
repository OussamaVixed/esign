package esign.service;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class CertificateUtils {

    public static X509Certificate loadCertificate(byte[] certBytes) throws CertificateException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certBytes));
    }

    public static byte[] convertCertificateToBytes(X509Certificate certificate) throws IOException, CertificateException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            os.write(certificate.getEncoded());
            return os.toByteArray();
        }
    }
}
