package esign.repository;

import esign.model.signature;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface SignatureRepository extends MongoRepository<signature, String> {
    List<signature> findByUsername1(String username1);
    
    @Query("{ 'username2': { $in: [?0] } }")
    List<signature> findByUsername2Containing(String username2);
}
