package esign.service;

import com.azure.storage.blob.BlobClient;

import esign.util.KeyUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

@Service
public class UserCertificateService {

    @Autowired
    private BlobStorageService blobStorageService;

    @Autowired
    private RootCertificateService rootCertificateService;

    private static final String USER_CERTIFICATE_NAME_SUFFIX = ".crt";
    private static final String USER_PRIVATE_KEY_NAME_SUFFIX = ".key";

    public void generateAndUploadUserCertificate(String userId) throws Exception {
        // Generate user's key pair
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair userKeyPair = keyPairGenerator.generateKeyPair();

        // Get the root private key
        PrivateKey rootPrivateKey = rootCertificateService.getRootPrivateKey();

        // Generate user's certificate
        UserCertificateGenerator userCertificateGenerator = new UserCertificateGenerator();
        X509Certificate userCertificate = userCertificateGenerator.generateUserCertificate("Root", userId, userKeyPair, rootPrivateKey);

        // Convert the user's certificate to bytes
        byte[] certBytes = CertificateUtils.convertCertificateToBytes(userCertificate);

        // Get the blob client for the user's certificate
        String userFolder = userId + "upload";
        String certBlobName = userId + USER_CERTIFICATE_NAME_SUFFIX;
        BlobClient certBlobClient = blobStorageService.getBlobContainerClient().getBlobClient(userFolder + "/" + certBlobName);

        // Upload the user's certificate to blob storage
        certBlobClient.upload(new ByteArrayInputStream(certBytes), certBytes.length);

        // Convert the user's private key to bytes
        byte[] keyBytes = KeyUtils.convertPrivateKeyToBytes(userKeyPair.getPrivate());

        // Get the blob client for the user's private key
        String keyBlobName = userId + USER_PRIVATE_KEY_NAME_SUFFIX;
        BlobClient keyBlobClient = blobStorageService.getBlobContainerClient().getBlobClient(userFolder + "/" + keyBlobName);

        // Upload the user's private key to blob storage
        keyBlobClient.upload(new ByteArrayInputStream(keyBytes), keyBytes.length);
    }
}
