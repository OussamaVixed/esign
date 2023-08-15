package esign.service;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.models.BlobItem;

import esign.model.User;
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
import java.util.List;

@Service
public class BlobStorageService {
	@Autowired
    private UserRepository userRepository;
    private BlobContainerClient blobContainerClient;

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





}
