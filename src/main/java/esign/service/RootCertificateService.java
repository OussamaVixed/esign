package esign.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import com.azure.storage.blob.BlobClient;

import esign.util.KeyUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509Certificate;

@Service
public class RootCertificateService {

    @Autowired
    private BlobStorageService blobStorageService;

    private static final String ROOT_FOLDER = "root";
    private static final String ROOT_CERTIFICATE_NAME = "root_certificate.crt";
    private static final String ROOT_PRIVATE_KEY_NAME = "root_private.key";
    private X509Certificate rootCertificate;
    private PrivateKey rootPrivateKey;

    @PostConstruct
    public void init() throws Exception {
        this.rootCertificate = this.getOrCreateRootCertificate();
        this.rootPrivateKey = this.getOrCreateRootPrivateKey();
    }

    public X509Certificate getRootCertificate() {
        return this.rootCertificate;
    }

    public PrivateKey getRootPrivateKey() {
        return this.rootPrivateKey;
    }

    public PrivateKey getOrCreateRootPrivateKey() throws Exception {
        String path = ROOT_FOLDER + "/" + ROOT_PRIVATE_KEY_NAME;
        BlobClient blobClient = blobStorageService.getBlobContainerClient().getBlobClient(path);

        if (!blobClient.exists()) {
            throw new Exception("Root private key not found");
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        blobClient.download(outputStream);
        byte[] keyBytes = outputStream.toByteArray();
        return KeyUtils.loadPrivateKey(keyBytes);
    }

    public X509Certificate getOrCreateRootCertificate() throws Exception {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        String certPath = ROOT_FOLDER + "/" + ROOT_CERTIFICATE_NAME;
        String keyPath = ROOT_FOLDER + "/" + ROOT_PRIVATE_KEY_NAME;
        BlobClient certBlobClient = blobStorageService.getBlobContainerClient().getBlobClient(certPath);
        BlobClient keyBlobClient = blobStorageService.getBlobContainerClient().getBlobClient(keyPath);

        if (certBlobClient.exists() && keyBlobClient.exists()) {
            // Load the existing root certificate and private key from blob storage
            ByteArrayOutputStream certOutputStream = new ByteArrayOutputStream();
            certBlobClient.download(certOutputStream);
            byte[] certBytes = certOutputStream.toByteArray();

            ByteArrayOutputStream keyOutputStream = new ByteArrayOutputStream();
            keyBlobClient.download(keyOutputStream);
            byte[] keyBytes = keyOutputStream.toByteArray();

            this.rootPrivateKey = KeyUtils.loadPrivateKey(keyBytes);

            return CertificateUtils.loadCertificate(certBytes); 
        } else {
            // Generate and save a new root certificate and private key
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair rootKeyPair = keyPairGenerator.generateKeyPair();

            RootCertificateGenerator rootCertificateGenerator = new RootCertificateGenerator();
            X509Certificate rootCertificate = rootCertificateGenerator.generateRootCertificate("Root", rootKeyPair);

            // Convert the root certificate to bytes
            byte[] certBytes = CertificateUtils.convertCertificateToBytes(rootCertificate);

            // Convert the private key to bytes
            byte[] keyBytes = KeyUtils.convertPrivateKeyToBytes(rootKeyPair.getPrivate());

            // Upload the root certificate and private key to blob storage
            certBlobClient.upload(new ByteArrayInputStream(certBytes), certBytes.length);
            keyBlobClient.upload(new ByteArrayInputStream(keyBytes), keyBytes.length);

            this.rootPrivateKey = rootKeyPair.getPrivate();

            return rootCertificate;
        }
    }
}
