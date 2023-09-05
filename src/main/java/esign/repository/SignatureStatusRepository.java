package esign.repository;

import esign.model.SignatureStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SignatureStatusRepository extends MongoRepository<SignatureStatus, String> {
    SignatureStatus findBySender(String sender);
}
