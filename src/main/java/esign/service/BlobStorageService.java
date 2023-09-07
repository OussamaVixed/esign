package esign.service;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.security.PdfPKCS7;

import esign.model.FileUpload;
import esign.model.User;
import esign.repository.FileUploadRepository;
import esign.repository.UserRepository;

import com.azure.core.http.rest.PagedIterable;
import com.azure.storage.blob.BlobClient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class BlobStorageService {
	@Autowired
    private UserRepository userRepository;
    private BlobContainerClient blobContainerClient;
    @Autowired
    private FileUploadRepository fileUploadRepository;

    public BlobStorageService(@Value("${azure.storage.connection-string}") String connectionString,
                              @Value("${azure.storage.blob-container}") String blobContainer) {
        this.blobContainerClient = new BlobContainerClientBuilder()
            .connectionString(connectionString)
            .containerName(blobContainer)
            .buildClient();
    }

    public void createUserFolder(String folderName) {
        BlobClient blobClient = blobContainerClient.getBlobClient(folderName + "/.keep");
        blobClient.upload(new ByteArrayInputStream(new byte[0]), 0);
    }
    public void uploadFile(String userId, MultipartFile file) {
        try {
            String blobName = userId + "upload/" + file.getOriginalFilename();
            BlobClient blobClient = blobContainerClient.getBlobClient(blobName);
            blobClient.upload(file.getInputStream(), file.getSize());
            System.out.println("fuile is " + blobName); // Debug print

            // Create and populate the FileUpload object
            FileUpload fileUpload = new FileUpload();
            fileUpload.setFileName(file.getOriginalFilename());
            fileUpload.setIssuanceDate(new Date()); // Current date
            fileUpload.setOwnerUid(userId);
            
            // Save the FileUpload object to the MongoDB
            fileUploadRepository.save(fileUpload);
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to blob storage.", e);
        }
    }
    public BlobContainerClient getBlobContainerClient() {
        return this.blobContainerClient;
    }
    
    public List<String> listUserFiles(String userId) {
        List<String> fileNames = new ArrayList<>();
        String folderName = userId;
        

        
        PagedIterable<BlobItem> blobs = blobContainerClient.listBlobs();
        for (BlobItem blobItem : blobs) {
            String blobName = blobItem.getName();
            
           
            
            if (blobName.startsWith(folderName)) {
                String fileName = blobName.replace(folderName, "");
                fileNames.add(fileName);
                
                
            }
        }
        System.out.println("Total files found: " + fileNames.size());
        return fileNames;
    }
    public void transferFile(String senderUserId, String receiverUserId, String fileName) {
        String senderBlobPath = senderUserId + "upload" + fileName;
        String receiverBlobPath = receiverUserId + "upload" + fileName;
        BlobClient senderBlobClient = blobContainerClient.getBlobClient(senderBlobPath);
        BlobClient receiverBlobClient = blobContainerClient.getBlobClient(receiverBlobPath);
        
        System.out.println("Debug: Transferring file from " + senderBlobPath + " to " + receiverBlobPath);

        if (!senderBlobClient.exists()) {
            throw new RuntimeException("File does not exist in sender's folder.");
        }
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        senderBlobClient.download(outputStream);
        byte[] fileContent = outputStream.toByteArray();
        
        ByteArrayInputStream inputStream = new ByteArrayInputStream(fileContent);
        receiverBlobClient.upload(inputStream, fileContent.length);
       
        System.out.println("Debug: File transferred successfully.");
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public byte[] downloadFile(String username, String filename) {
        User user = findByUsername(username);
        if (user != null) {
            String folderName = user.getId() + "upload";
            System.out.println("Folder Name: " + folderName); // Debug print for folderName

            String blobName = folderName + filename;
            System.out.println("Blob Name: " + blobName); // Debug print for blobName

            BlobClient blobClient = blobContainerClient.getBlobClient(blobName);

            if (!blobClient.exists()) {
                throw new RuntimeException("File does not exist in user's folder.");
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            blobClient.download(outputStream);

            return outputStream.toByteArray();
        } else {
            throw new RuntimeException("User not found.");
        }
    }


    public byte[] downloadsignedFile(String username, String filename) {
        User user = findByUsername(username);
        if (user != null) {
            String folderName = user.getId() + "upload" + "/signatures";
            System.out.println("Folder Name: " + folderName); // Debug print for folderName
            
            String newFilename = filename.substring(0, filename.length() - 4) + "_signed.pdf";
            String blobName = folderName + newFilename;
            System.out.println("Blob Name: " + blobName); // Debug print for blobName

            BlobClient blobClient = blobContainerClient.getBlobClient(blobName);

            if (!blobClient.exists()) {
                throw new RuntimeException("File does not exist in user's folder.");
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            blobClient.download(outputStream);

            return outputStream.toByteArray();
        } else {
            throw new RuntimeException("User not found.");
        }
    }
    public void simpleUploadFile(String userId, MultipartFile file) {
        try {
            String blobName = userId + "upload/" + file.getOriginalFilename();
            BlobClient blobClient = blobContainerClient.getBlobClient(blobName);
            blobClient.upload(file.getInputStream(), file.getSize());

            System.out.println("File uploaded to: " + blobName);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to blob storage.", e);
        }
    }
    public void simpleDeleteFile(String userId, String fileName) {
        try {
            String blobName = userId + "upload" + fileName;
            BlobClient blobClient = blobContainerClient.getBlobClient(blobName);
            
            if (blobClient.exists()) {
                blobClient.delete();
                System.out.println("File deleted: " + blobName);
            } else {
                System.out.println("File does not exist: " + blobName);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file from blob storage.", e);
        }
    }
    public String getSignerUsernameFromPdfBlob(String userId, String blobName) {
        String signerUsername = null;

        try {
            // Get BlobClient to download PDF from Azure Blob Storage
            BlobClient blobClient = blobContainerClient.getBlobClient(userId + "upload" + blobName);

            // Download PDF to a ByteArrayOutputStream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            blobClient.download(outputStream);

            // Read PDF from the downloaded bytes
            PdfReader reader = new PdfReader(new ByteArrayInputStream(outputStream.toByteArray()));
            AcroFields af = reader.getAcroFields();
            List<String> names = af.getSignatureNames();

            for (String name : names) {
                PdfPKCS7 pkcs7 = af.verifySignature(name);
                String reason = pkcs7.getReason();

                if (reason != null && reason.matches("^Signed by .* at .*")) {
                    // Extract the username from the reason string
                    int startIndex = reason.indexOf("Signed by ") + "Signed by ".length();
                    int endIndex = reason.indexOf(" at ");
                    if (startIndex != -1 && endIndex != -1) {
                        signerUsername = reason.substring(startIndex, endIndex);
                        break;
                    }
                }
            }

            reader.close();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to extract signer's username from the PDF. Cause: " + e.getMessage(), e);
        }

        return signerUsername;
    }


    public String getSignatureDateFromPdfBlob(String userId, String blobName) {
        String signatureDate = null;

        try {
            // Get BlobClient to download PDF from Azure Blob Storage
            BlobClient blobClient = blobContainerClient.getBlobClient(userId + "upload" + blobName);
            System.out.println("Downloading PDF from Blob Storage: " + blobName);

            // Download PDF to a ByteArrayOutputStream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            blobClient.download(outputStream);
            System.out.println("Downloaded PDF successfully.");

            // Read PDF from the downloaded bytes
            PdfReader reader = new PdfReader(new ByteArrayInputStream(outputStream.toByteArray()));
            AcroFields af = reader.getAcroFields();
            List<String> names = af.getSignatureNames();

            for (String name : names) {
                PdfPKCS7 pkcs7 = af.verifySignature(name);
                String reason = pkcs7.getReason();
                System.out.println("Signature Reason: " + reason);

                if (reason != null && reason.contains("Signed by ")) {
                    // Extract the date portion from the reason string
                    int startIndex = reason.indexOf(" at ");
                    if (startIndex != -1) {
                        signatureDate = reason.substring(startIndex + 4);
                        System.out.println("Extracted Signature Date: " + signatureDate);
                        break;
                    }
                }
            }

            reader.close();
            System.out.println("Closed PDF reader.");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to extract signature date from the PDF. Cause: " + e.getMessage(), e);
        }

        return signatureDate;
    }



}
