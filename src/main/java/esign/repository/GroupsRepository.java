package esign.repository;

import esign.model.Groups;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface GroupsRepository extends MongoRepository<Groups, String> {
    Groups findByGroupnameAndOwner(String groupname, String owner);
    List<Groups> findByOwner(String owner);
}
