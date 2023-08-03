package esign.service;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class RootCertificateGenerator {

    public X509Certificate generateRootCertificate(String issuer, KeyPair keyPair) throws OperatorCreationException, CertificateException {
        X500Name issuerName = new X500Name("CN=" + issuer);
        BigInteger serial = BigInteger.valueOf(new SecureRandom().nextInt());
        Date notBefore = Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
        Date notAfter = Date.from(LocalDateTime.now().plusYears(10L).atZone(ZoneId.systemDefault()).toInstant());
        // Subject name same as issuer for a self-signed certificate
        X500Name subjectName = issuerName; 

        JcaX509v3CertificateBuilder certificateBuilder = new JcaX509v3CertificateBuilder(
                issuerName, serial, notBefore, notAfter, subjectName, keyPair.getPublic());

        ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSAEncryption")
                .setProvider(BouncyCastleProvider.PROVIDER_NAME).build(keyPair.getPrivate());

        return new JcaX509CertificateConverter().getCertificate(certificateBuilder.build(signer));
    }
}
