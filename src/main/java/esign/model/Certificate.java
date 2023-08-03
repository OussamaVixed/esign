package esign.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "certificates")
public class Certificate {
    @Id
    private String id;
    private String userId; // The ID of the user associated with this certificate
    private String issuer; // The issuer of the certificate
    private String subject; // The subject of the certificate
    private String publicKey; // The public key of the subject
    private String privateKey; // The private key of the subject
    private String certificate; // The certificate as a string

    public Certificate(String userId, String issuer, String subject, String publicKey, String privateKey, String certificate) {
        this.userId = userId;
        this.issuer = issuer;
        this.subject = subject;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.certificate = certificate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }
}
