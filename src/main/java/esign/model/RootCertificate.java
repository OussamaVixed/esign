package esign.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "rootCertificate")
public class RootCertificate {

    @Id
    private String id;
    private String certificate; // Encoded as a base64 String
    private String privateKey;  // Encoded as a base64 String, encrypted using a symmetric encryption algorithm such as AES
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getCertificate() {
		return certificate;
	}
	public void setCertificate(String certificate) {
		this.certificate = certificate;
	}
	public String getPrivateKey() {
		return privateKey;
	}
	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}


}
