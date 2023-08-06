package esign.service;

import esign.model.User;
import esign.repository.UserRepository;
import esign.util.KeyUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.azure.storage.blob.BlobClient;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BlobStorageService blobStorageService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserCertificateService userCertificateService;

    public User register(User user) throws Exception {
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        User registeredUser = userRepository.save(user);

        // Create a virtual directory for the registered user
        String folderName = registeredUser.getId() + "upload";
        blobStorageService.createUserFolder(folderName);
        
        // Create a virtual sub-directory for the signatures
        String signatureFolderName = folderName + "/signatures";
        blobStorageService.createUserFolder(signatureFolderName);

        // Generate and upload a certificate for the registered user
        String userId = registeredUser.getId().toString();
        userCertificateService.generateAndUploadUserCertificate(userId);

        return registeredUser;
    }

    public User authenticate(String username, String password) {
        User user = userRepository.findByUsername(username);

        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            return user;
        }

        return null;
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public List<String> getUserFiles(String username) {
        User user = findByUsername(username);
        if (user != null) {
        	String folderName = user.getId() + "upload";
        	System.out.println(folderName);

            List<String> userFiles = blobStorageService.listUserFiles(folderName);
            List<String> filteredFiles = new ArrayList<>();

            for (String file : userFiles) {
                if (!file.endsWith(".keep") && !file.endsWith(".crt") && !file.endsWith(".key") && !file.endsWith(".sig")) {
                    filteredFiles.add(file);
                }
            }

            return filteredFiles;
        }
        return new ArrayList<>(); // return an empty list if the user does not exist
    }

    

    public void signFile(String userId, String fileName) {
        try {
            // Get the blob client for the user's private key
            String userFolder1 = userId + "upload";
            String fileBlobName = fileName;
            String blob1 = userFolder1 + fileBlobName ;
            BlobClient fileBlobClient = blobStorageService.getBlobContainerClient().getBlobClient(blob1);
            System.out.println(blob1);
            // Download the file data from blob storage
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            fileBlobClient.download(outputStream);
            byte[] fileData = outputStream.toByteArray();
            
            // Sign the file data
            signFileWithPrivateKey(userId, fileName, fileData);
            
        } catch (Exception e) {
        	e.printStackTrace();
            throw new RuntimeException("Failed to sign the data.", e);
        }
    }

    private void signFileWithPrivateKey(String userId, String fileName, byte[] dataToSign) {
        try {
            // Get the blob client for the user's private key
            String userFolder2 = userId + "upload";
            String keyBlobName = userId + ".key";
            BlobClient keyBlobClient = blobStorageService.getBlobContainerClient().getBlobClient(userFolder2 + "/" + keyBlobName);
            System.out.println(userFolder2);
            System.out.println(keyBlobName);
            System.out.println(userFolder2 + "/" + keyBlobName);
            
            // Download the user's private key from blob storage
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            keyBlobClient.download(outputStream);
            byte[] keyBytes = outputStream.toByteArray();

            // Convert bytes back to PrivateKey
            PrivateKey privateKey = KeyUtils.loadPrivateKey(keyBytes);

            // Initialize a Signature object with the private key and SHA256withRSA algorithm
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);

            // Add data to the Signature object
            signature.update(dataToSign);

            // Generate a digital signature
            byte[] signedData = signature.sign();

            // Save the signature to Azure Blob Storage
            String sigBlobName = "/signatures" + fileName + ".sig";
            BlobClient sigBlobClient = blobStorageService.getBlobContainerClient().getBlobClient(userFolder2 + sigBlobName);
            System.out.println(userFolder2 + sigBlobName);
            sigBlobClient.upload(new ByteArrayInputStream(signedData), signedData.length);

        } catch (Exception e) {
            throw new RuntimeException("Failed to sign the data.", e);
        }
    }
    public boolean checkSignatureFileExists(String username, String filename) {
        User user = findByUsername(username);
        if (user != null) {
        	String folderName = user.getId() + "upload";
        	String signaturesFolder = folderName + "/signatures";

            List<String> signatureFiles = blobStorageService.listUserFiles(signaturesFolder);

            String targetFile = filename + ".sig";

            return signatureFiles.contains(targetFile);
        }
        return false; // return false if the user does not exist
    }


}
