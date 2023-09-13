package esign.model;

import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "Sigstats")
public class SignatureStatus {
	@Id
	private String UID;
	private List<Boolean> issigned;
	private List<Date> sigdate;
	private String sender;
	private List<String> Username2;
	private List<String> fileid;
	private List<String> sigID;
	private List<Boolean> refused;
	private List<String> groupnames;

	
	public List<String> getGroupnames() {
		return groupnames;
	}
	public void setGroupnames(List<String> groupnames) {
		this.groupnames = groupnames;
	}
	public List<Boolean> getRefused() {
		return refused;
	}
	public void setRefused(List<Boolean> refused) {
		this.refused = refused;
	}
	public String getUID() {
		return UID;
	}
	public void setUID(String uID) {
		UID = uID;
	}
	public List<Boolean> getIssigned() {
		return issigned;
	}
	public void setIssigned(List<Boolean> issigned) {
		this.issigned = issigned;
	}
	public List<Date> getSigdate() {
		return sigdate;
	}
	public void setSigdate(List<Date> sigdate) {
		this.sigdate = sigdate;
	}

	public List<String> getUsername2() {
		return Username2;
	}
	public void setUsername2(List<String> username2) {
		Username2 = username2;
	}
	public List<String> getFileid() {
		return fileid;
	}
	public void setFileid(List<String> fileid) {
		this.fileid = fileid;
	}
	public List<String> getSigID() {
		return sigID;
	}
	public void setSigID(List<String> sigID) {
		this.sigID = sigID;
	}
	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}
	
	
	
	



	
}
