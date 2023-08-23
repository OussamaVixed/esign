package esign.model;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "FileUpload")
public class FileUpload {
	
	private String FileName;
	private Date issuanceDate;
	@Id
	private String id;
	private String OwnerUid;
	
	public String getFileName() {
		return FileName;
	}
	public void setFileName(String fileName) {
		FileName = fileName;
	}
	public Date getIssuanceDate() {
		return issuanceDate;
	}
	public void setIssuanceDate(Date issuanceDate) {
		this.issuanceDate = issuanceDate;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getOwnerUid() {
		return OwnerUid;
	}
	public void setOwnerUid(String ownerUid) {
		OwnerUid = ownerUid;
	}

}
