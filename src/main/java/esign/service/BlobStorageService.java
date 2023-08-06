package esign.service;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import com.azure.core.http.rest.PagedIterable;
import com.azure.storage.blob.BlobClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class BlobStorageService {

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
        
        System.out.println("Folder name: " + folderName);
        
        PagedIterable<BlobItem> blobs = blobContainerClient.listBlobs();
        for (BlobItem blobItem : blobs) {
            String blobName = blobItem.getName();
            
            System.out.println("Blob name: " + blobName);
            
            if (blobName.startsWith(folderName)) {
                String fileName = blobName.replace(folderName, "");
                fileNames.add(fileName);
                
                System.out.println("File added: " + fileName);
            }
        }
        System.out.println("Total files found: " + fileNames.size());
        return fileNames;
    }




}
