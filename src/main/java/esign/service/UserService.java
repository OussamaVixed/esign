package esign.service;

import esign.model.FileUpload;
import esign.model.Groups;
import esign.model.SignatureStatus;
import esign.model.User;
import esign.model.signature;
import esign.repository.UserRepository;
import esign.util.KeyUtils;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;
import esign.repository.GroupsRepository;
import esign.repository.SignatureRepository;
import esign.repository.SignatureStatusRepository;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.security.*;
import com.itextpdf.text.pdf.security.MakeSignature.CryptoStandard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
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
    private GroupsRepository groupRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private SignatureStatusRepository signatureStatusRepository;
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
        
        SignatureStatus signaturestatObj = new SignatureStatus();
        signaturestatObj.setSender(registeredUser.getUsername());
        	signaturestatObj.setFileid(new ArrayList<>());
        	signaturestatObj.setIssigned(new ArrayList<>());
        	signaturestatObj.setSigdate(new ArrayList<>());
        	signaturestatObj.setUsername2(new ArrayList<>());
        	signaturestatObj.setSigID(new ArrayList<>());
        	signaturestatObj.setRefused(new ArrayList<>());
        	signaturestatObj.setGroupnames(new ArrayList<>());


        signatureStatusRepository.save(signaturestatObj);
        
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

    

    public void signFile(String userId, String fileName , String Username) {
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
                signPdfFileWithPrivateKey(userId, fileName, fileData , Username); // Renamed method for PDF signing
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
    private void signPdfFileWithPrivateKey(String userId, String fileName, byte[] dataToSign, String Username) {
        try {
            // Get the blob client for the user's private key
            Date currentDate = new Date();
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
            appearance.setReason("Signed by " + Username + " at " + currentDate);
            appearance.setLocation("Oujda");
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

         
            String targetFile = filename + ".sig";
            
            String targetFile2 = filename.substring(0, filename.length() - 4) + "_signed.pdf";

           

            return signatureFiles.contains(targetFile) || signatureFiles.contains(targetFile2);
        }
        return false; // return false if the user does not exist
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
    @Autowired
    private MongoTemplate mongoTemplate;

    public List<FileUpload> getUserFiles1(String userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("OwnerUid").is(userId));
        return mongoTemplate.find(query, FileUpload.class);
    }
    public List<String> getTimeRemaining(List<Date> targetDates) {
        List<String> timeRemainingList = new ArrayList<>();
        Date currentDate = new Date();

        for (Date targetDate : targetDates) {
            long durationMillis = targetDate.getTime() - currentDate.getTime();
            if (durationMillis > 0) {
                timeRemainingList.add(formatDuration(durationMillis));
            } else {
                timeRemainingList.add("EXPIRED!");
            }
        }

        return timeRemainingList;
    }
    public boolean verifyPdfSignature(byte[] pdfData, String username) {
        try {
            // Load the PDF
            PdfReader reader = new PdfReader(new ByteArrayInputStream(pdfData));

            // Get the AcroFields
            AcroFields af = reader.getAcroFields();

            // Fetch the signature names
            List<String> names = af.getSignatureNames();
            
            User user1 = findByUsername(username);
            // Fetch userId from username (assuming you have a method for this)
            String userId = user1.getId();  // Implement this method

            // Fetch the certificate from blob storage
            String certBlobName = userId + "upload/" + userId + ".crt";
            BlobClient certBlobClient = blobStorageService.getBlobContainerClient().getBlobClient(certBlobName);

            ByteArrayOutputStream certOutputStream = new ByteArrayOutputStream();
            certBlobClient.download(certOutputStream);
            byte[] certBytes = certOutputStream.toByteArray();

            // Convert bytes to X509Certificate
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            X509Certificate signerCertificate = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(certBytes));

            // Loop over the signatures and verify
            for (String name : names) {
                PdfPKCS7 pkcs7 = af.verifySignature(name);

                // Retrieve the signer's certificate from the signature
                Certificate[] certificates = pkcs7.getCertificates();

                // Check if the public key of the signer's certificate matches
                // the public key contained in the signature
                if (pkcs7.verify()) {
                    // Verify if the certificate used for signing matches the provided certificate
                    if (signerCertificate.equals(certificates[0])) {
                        return true;
                    }
                }
            }
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to verify PDF signature.", e);
        }

        return false;
    }
    
    public static class MixedListResult {
        public List<String> mixedNames;
        public List<Boolean> mixedIsSigned;
        public List<Boolean> mixedIsRefused;
        public List<Date> mixedDates;

        public MixedListResult(List<String> mixedNames, List<Boolean> mixedIsSigned, List<Boolean> mixedIsRefused, List<Date> mixedDates) {
            this.mixedNames = mixedNames;
            this.mixedIsSigned = mixedIsSigned;
            this.mixedIsRefused = mixedIsRefused;
            this.mixedDates = mixedDates;
        }
    }

    public MixedListResult mixLists(List<String> usernames, List<String> groupnames,
                                           List<Boolean> isSigned, List<Boolean> isRefused, List<Date> dates) {
        List<String> mixedNames = new ArrayList<>();
        List<Boolean> mixedIsSigned = new ArrayList<>();
        List<Boolean> mixedIsRefused = new ArrayList<>();
        List<Date> mixedDates = new ArrayList<>();

        for (int i = 0; i < usernames.size(); i++) {
            if (groupnames.get(i).equals("0")) {
                mixedNames.add(usernames.get(i));
                mixedIsSigned.add(isSigned.get(i));
                mixedIsRefused.add(isRefused.get(i));
                mixedDates.add(dates.get(i));
                continue;
            }

            List<String> tempNames = new ArrayList<>();
            List<Boolean> tempSigned = new ArrayList<>();
            List<Boolean> tempRefused = new ArrayList<>();
            List<Date> tempDates = new ArrayList<>();
            String currentGroupName = groupnames.get(i);
            Date currentDate = dates.get(i);

            while (i < usernames.size() && groupnames.get(i).equals(currentGroupName) && dates.get(i).equals(currentDate)) {
                tempNames.add(usernames.get(i));
                tempSigned.add(isSigned.get(i));
                tempRefused.add(isRefused.get(i));
                tempDates.add(dates.get(i));
                i++;
            }
            i--;  // Adjust index

            if (tempNames.size() > 0) {
                mixedNames.add(currentGroupName);
                mixedIsSigned.add(!tempSigned.contains(false));
                mixedIsRefused.add(tempRefused.contains(true));
                mixedDates.add(currentDate);  // Use the current date as it is shared among all usernames in the group
            }
        }

        return new MixedListResult(mixedNames, mixedIsSigned, mixedIsRefused, mixedDates);
    }
    
    
  

}

