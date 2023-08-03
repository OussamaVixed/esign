package esign.repository;

import esign.model.Certificate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

public interface CertificateRepository extends MongoRepository<Certificate, String> {
    Certificate findByUserId(String userId);
}
