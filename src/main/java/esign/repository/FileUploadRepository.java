package esign.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import esign.model.FileUpload;

public interface FileUploadRepository extends MongoRepository<FileUpload, String> {
}
