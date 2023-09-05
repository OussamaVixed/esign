package esign.service;

import esign.model.SignatureStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SignatureService {

    @Autowired
    private MongoTemplate mongoTemplate;

    public void updateIssigned(String username, String filename) {
        // Define query to find the document
        Query query = new Query();
        query.addCriteria(Criteria.where("Username2").is(username));
        query.addCriteria(Criteria.where("fileid").is(filename));

        // Fetch the document
        SignatureStatus signatureStatus = mongoTemplate.findOne(query, SignatureStatus.class);

        if (signatureStatus != null) {
            // Get the existing lists
            List<String> username2List = signatureStatus.getUsername2();
            List<String> fileIdList = signatureStatus.getFileid();
            List<Boolean> isSignedList = signatureStatus.getIssigned();

            // Find the index where both username and filename match
            for (int i = 0; i < username2List.size(); i++) {
                if (username2List.get(i).equals(username) && fileIdList.get(i).equals(filename)) {
                    // Update the 'issigned' value to true at the found index
                    isSignedList.set(i, true);
                    break;
                }
            }

            // Prepare update
            Update update = new Update();
            update.set("issigned", isSignedList);

            // Update the document
            mongoTemplate.updateFirst(query, update, SignatureStatus.class);
        }
    }
}
