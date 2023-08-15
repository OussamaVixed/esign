package esign.repository;

import esign.model.signature;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface SignatureRepository extends MongoRepository<signature, String> {
    List<signature> findByUsername1(String username1);
    List<signature> findByUsername2(String username2);
}
