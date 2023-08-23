package esign.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document(collection = "signatures")
public class signature {

    @Id
    private String id;
    private String username1;
    private List<String> username2;
    private String fileName;
    private String FileNameUID;
    private Date issuanceDate;
    private Date expiryDate;

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername1() {
        return username1;
    }

    public void setUsername1(String username1) {
        this.username1 = username1;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Date getIssuanceDate() {
        return issuanceDate;
    }

    public void setIssuanceDate(Date issuanceDate) {
        this.issuanceDate = issuanceDate;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

	public String getFileNameUID() {
		return FileNameUID;
	}

	public void setFileNameUID(String fileNameUID) {
		FileNameUID = fileNameUID;
	}

	public List<String> getUsername2() {
		return username2;
	}

	public void setUsername2(List<String> username2) {
		this.username2 = username2;
	}


	



}

