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
    public void updateRefused(String username, String filename) {
        System.out.println("Debug: Entering updateRefused method with username: " + username + " and filename: " + filename);
        
        // Define query to find the document
        Query query = new Query();
        query.addCriteria(Criteria.where("Username2").is(username));
        query.addCriteria(Criteria.where("fileid").is(filename));
        
        System.out.println("Debug: Query: " + query.toString());
        
        // Fetch the document
        SignatureStatus signatureStatus = mongoTemplate.findOne(query, SignatureStatus.class);

        if (signatureStatus != null) {
            System.out.println("Debug: Found matching document: " + signatureStatus.toString());
            
            // Get the existing lists
            List<String> username2List = signatureStatus.getUsername2();
            List<String> fileIdList = signatureStatus.getFileid();
            List<Boolean> RefusedList = signatureStatus.getRefused();
            
            System.out.println("Debug: Existing username2List: " + username2List);
            System.out.println("Debug: Existing fileIdList: " + fileIdList);
            System.out.println("Debug: Existing RefusedList: " + RefusedList);
            
            // Find the index where both username and filename match
            for (int i = 0; i < username2List.size(); i++) {
                System.out.println("Debug: Checking index " + i + ": username=" + username2List.get(i) + ", fileId=" + fileIdList.get(i));
                if (username2List.get(i).equals(username) && fileIdList.get(i).equals(filename)) {
                    // Update the 'issigned' value to true at the found index
                    RefusedList.set(i, true);
                    System.out.println("Debug: Updated 'refused' at index " + i);
                    break;
                }
            }


            // Prepare update
            Update update = new Update();
            update.set("refused", RefusedList);
            
            System.out.println("Debug: Prepared update: " + update.toString());

            // Update the document
            mongoTemplate.updateFirst(query, update, SignatureStatus.class);
            
            System.out.println("Debug: Document updated successfully.");
        } else {
            System.out.println("Debug: No matching document found.");
        }
        
        System.out.println("Debug: Exiting updateRefused method");
    }
    public boolean getRefused(String username, String filename) {
        
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
            List<Boolean> RefusedList = signatureStatus.getRefused();
 
            // Find the index where both username and filename match
            for (int i = 0; i < username2List.size(); i++) {
                if (username2List.get(i).equals(username) && fileIdList.get(i).equals(filename) && RefusedList.get(i).equals(true)) {
                    return true;
                }
                else if (username2List.get(i).equals(username) && fileIdList.get(i).equals(filename) && RefusedList.get(i).equals(false)) {
                	return false;
            }
      
		    }
        }
		return false;
    }

}
