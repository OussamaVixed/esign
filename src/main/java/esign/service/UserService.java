package esign.service;

import esign.model.User;
import esign.model.signature;
import esign.repository.UserRepository;
import esign.util.KeyUtils;
import java.security.cert.X509Certificate;
import java.sql.Date;

import esign.repository.SignatureRepository;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.security.*;
import com.itextpdf.text.pdf.security.MakeSignature.CryptoStandard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.azure.storage.blob.BlobClient;

@Service
public class UserService {
	@Autowired
	private SignatureRepository signatureRepository;
	@Autowired
    private CertificateUtils certificateUtils;
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
                if (!file.endsWith(".keep") && !file.endsWith("_signed.pdf") && !file.endsWith(".crt") && !file.endsWith(".key") && !file.endsWith(".sig")) {
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
            String blob1 = userFolder1 + fileBlobName;
            BlobClient fileBlobClient = blobStorageService.getBlobContainerClient().getBlobClient(blob1);
            System.out.println(blob1);

            // Download the file data from blob storage
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            fileBlobClient.download(outputStream);
            byte[] fileData = outputStream.toByteArray();

            // Check if the file is a PDF
            if (fileName.toLowerCase().endsWith(".pdf")) {
                // Sign the PDF file using the new method
                signPdfFileWithPrivateKey(userId, fileName, fileData); // Renamed method for PDF signing
            } else {
                // Sign the file using the old method
                signFileWithPrivateKey(userId, fileName, fileData);
            }
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
    private void signPdfFileWithPrivateKey(String userId, String fileName, byte[] dataToSign) {
        try {
            // Get the blob client for the user's private key
            String userFolder3 = userId + "upload";
            String keyBlobName1 = userId + ".key";
            BlobClient keyBlobClient = blobStorageService.getBlobContainerClient().getBlobClient(userFolder3 + "/" + keyBlobName1);
            System.out.println("User folder: " + userFolder3);
            System.out.println("Key blob name: " + keyBlobName1);
            System.out.println("Full key path: " + userFolder3 + "/" + keyBlobName1);

            // Download the user's private key from blob storage
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            keyBlobClient.download(outputStream);
            byte[] keyBytes = outputStream.toByteArray();

            // Convert bytes back to PrivateKey
            PrivateKey privateKey = KeyUtils.loadPrivateKey(keyBytes);

            // Load the PDF
            PdfReader reader = new PdfReader(new ByteArrayInputStream(dataToSign));
            ByteArrayOutputStream signedPdfOutput = new ByteArrayOutputStream();
            PdfStamper stamper = PdfStamper.createSignature(reader, signedPdfOutput, '\0');

            // Create the signature appearance
            PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
            appearance.setReason("Signed by " + userId);
            appearance.setLocation("Location");
            appearance.setVisibleSignature(new Rectangle(72, 732, 144, 780), 1, "Signature");

            // Configure the digital signature
            ExternalDigest digest = new BouncyCastleDigest();
            ExternalSignature signature = new PrivateKeySignature(privateKey, "SHA-256", "BC");

            // Create an instance of CertificateUtils to load the certificate chain
            CertificateUtils certificateUtils = new CertificateUtils();
            certificateUtils.setBlobStorageService(blobStorageService);

            X509Certificate[] certificates = certificateUtils.loadCertificateChain(userId);
            System.out.println("Loaded certificates: " + Arrays.toString(certificates)); // Debugging line

            MakeSignature.signDetached(appearance, digest, signature, certificates, null, null, null, 0, CryptoStandard.CMS);

            stamper.close();
            reader.close();
            String fileName1 = fileName.substring(0, fileName.length() - 4);
            // Save the signed PDF to Azure Blob Storage
            byte[] signedPdfData = signedPdfOutput.toByteArray();
            String sigBlobName = "/signatures" + fileName1 + "_signed.pdf";
            BlobClient sigBlobClient = blobStorageService.getBlobContainerClient().getBlobClient(userFolder3 + sigBlobName);
            System.out.println("Full signature path: " + userFolder3 + sigBlobName);
            sigBlobClient.upload(new ByteArrayInputStream(signedPdfData), signedPdfData.length);
        } catch (Exception e) {
            e.printStackTrace(); // Print the full stack trace
            throw new RuntimeException("Failed to sign the PDF. Cause: " + e.getMessage(), e);
        }
    }



    public boolean checkSignatureFileExists(String username, String filename) {
        User user = findByUsername(username);
        if (user != null) {
            String folderName = user.getId() + "upload";
            String signaturesFolder = folderName + "/signatures";

            List<String> signatureFiles = blobStorageService.listUserFiles(signaturesFolder);

            // Debug prints
            System.out.println("Signature Files: " + signatureFiles);
            String targetFile = filename + ".sig";
            
            String targetFile2 = filename.substring(0, filename.length() - 4) + "_signed.pdf";

            System.out.println("Looking for: " + targetFile + " or " + targetFile2);

            return signatureFiles.contains(targetFile) || signatureFiles.contains(targetFile2);
        }
        return false; // return false if the user does not exist
    }

    public class SignatureInfo {
        private String username2;
        private long duration; // in milliseconds
        private String fileName;
        private boolean signed;

        // Constructor
        public SignatureInfo(String username2, long duration, String fileName, boolean signed) {
            this.username2 = username2;
            this.duration = duration;
            this.fileName = fileName;
            this.signed = signed;
        }

        public String getUsername2() {
            return username2;
        }

        public long getDuration() {
            return duration;
        }

        public String getFileName() {
            return fileName;
        }

        public boolean isSigned() {
            return signed;
        }

        public void setSigned(boolean signed) {
            this.signed = signed;
        }
    }
    public class SignatureInfo2 {
        private String username1;
        private long duration; // in milliseconds
        private String fileName;
        private boolean signed;

        // Constructor
        public SignatureInfo2(String username1, long duration, String fileName, boolean signed) {
            this.username1 = username1;
            this.duration = duration;
            this.fileName = fileName;
            this.signed = signed;
        }

        public String getUsername1() {
            return username1;
        }

        public long getDuration() {
            return duration;
        }

        public String getFileName() {
            return fileName;
        }

        public boolean isSigned() {
            return signed;
        }

        public void setSigned(boolean signed) {
            this.signed = signed;
        }
    }
    

        
    public List<SignatureInfo> findSignaturesByUsername1(String username1) {
        List<signature> signatures = signatureRepository.findByUsername1(username1);

        List<SignatureInfo> result = new ArrayList<>();
        for (signature sig : signatures) {
            if (username1.equals(sig.getUsername1())) {
                long duration = sig.getExpiryDate().getTime() - sig.getIssuanceDate().getTime();
                boolean isSigned = checkSignatureFileExists(sig.getUsername2(), sig.getFileName());
                SignatureInfo signatureInfo = new SignatureInfo(sig.getUsername2(), duration, sig.getFileName(), isSigned);
                result.add(signatureInfo); // Pass isSigned to the constructor
                System.out.println(signatureInfo); // Print the SignatureInfo object
            }
        }

        System.out.println("Signatures by " + username1 + ": " + result); // Print the entire result list

        return result;
    }
    public List<SignatureInfo2> findSignaturesByUsername2(String username2) {
        List<signature> signatures = signatureRepository.findByUsername2(username2);

        List<SignatureInfo2> result2 = new ArrayList<>();
        for (signature sig : signatures) {
            if (username2.equals(sig.getUsername2())) {
                long duration = sig.getExpiryDate().getTime() - sig.getIssuanceDate().getTime();
                boolean isSigned = checkSignatureFileExists(sig.getUsername2(), sig.getFileName());
                System.out.println("is the file signed:"+ sig.getFileName() + isSigned);
                if (!isSigned) { // Only add if the file is not signed
                    SignatureInfo2 signatureInfo2 = new SignatureInfo2(sig.getUsername1(), duration, sig.getFileName(), isSigned);
                    result2.add(signatureInfo2);
                    System.out.println(signatureInfo2); // Print the SignatureInfo object
                }
            }
        }

        System.out.println("Signatures for " + username2 + ": " + result2); // Print the entire result list

        return result2;
    }


    public String formatDuration(long durationMillis) {
        long totalMinutes = durationMillis / (1000 * 60);
        long days = totalMinutes / (60 * 24);
        long hours = (totalMinutes % (60 * 24)) / 60;
        long minutes = totalMinutes % 60;

        StringBuilder formattedDuration = new StringBuilder();
        if (days > 0) {
            formattedDuration.append(days).append(":");
        }
        if (days > 0 || hours > 0) {
            formattedDuration.append(String.format("%02d", hours)).append(":");
        }
        formattedDuration.append(String.format("%02d", minutes));

        return formattedDuration.toString();
    }

    
}

