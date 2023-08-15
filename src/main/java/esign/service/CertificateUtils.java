package esign.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.azure.storage.blob.BlobClient;

@Service
public class CertificateUtils {
    @Autowired
    private BlobStorageService blobStorageService;

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

    public X509Certificate[] loadCertificateChain(String userId) {
        try {
            String userFolder = userId + "upload";
            String certBlobName = userId + ".crt"; // Using the expected certificate name
            BlobClient certBlobClient = blobStorageService.getBlobContainerClient().getBlobClient(userFolder + "/" + certBlobName);
            System.out.println(userFolder);
            System.out.println(certBlobName);
            System.out.println(userFolder + "/" + certBlobName);
            // Download the user's certificate from blob storage
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            certBlobClient.download(outputStream);
            byte[] certBytes = outputStream.toByteArray();

            // Convert bytes back to X509Certificate
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            X509Certificate userCertificate = (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(certBytes));

            return new X509Certificate[] { userCertificate };
        } catch (Exception e) {
            throw new RuntimeException("Failed to load the certificate chain.", e);
        }
    }
    public void setBlobStorageService(BlobStorageService blobStorageService) {
        this.blobStorageService = blobStorageService;
    }
}
