package esign.service;

import java.io.IOException;
import java.io.StringWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.List;

import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.shredzone.acme4j.Account;
import org.shredzone.acme4j.AccountBuilder;
import org.shredzone.acme4j.Authorization;
import org.shredzone.acme4j.Certificate;
import org.shredzone.acme4j.Order;
import org.shredzone.acme4j.Session;
import org.shredzone.acme4j.Status;
import org.shredzone.acme4j.challenge.Http01Challenge;
import org.shredzone.acme4j.exception.AcmeException;
import org.shredzone.acme4j.util.CSRBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import esign.model.User;
import esign.repository.UserRepository;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public User register(User user) throws NoSuchAlgorithmException, AcmeException, InterruptedException, IOException, CertificateEncodingException {
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        // Generate the certificate and key pair using acme4j
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
        keyPairGenerator.initialize(256);
        KeyPair accountKeyPair = keyPairGenerator.generateKeyPair();

        // Create a session
        Session session = new Session("acme://letsencrypt.org/");

        // Create a new account
        Account account = new AccountBuilder()
                .agreeToTermsOfService()
                .useKeyPair(accountKeyPair)
                .create(session);

        // Generate the key pair for the domain
        KeyPair domainKeyPair = keyPairGenerator.generateKeyPair();

        // Generate the CSR
        CSRBuilder csrBuilder = new CSRBuilder();
        csrBuilder.addDomain(user.getUsername());
        csrBuilder.sign(domainKeyPair);

        // Order the authorization
        Order order = account.newOrder().domains(user.getUsername()).create();
        for (Authorization auth : order.getAuthorizations()) {
            Http01Challenge challenge = auth.findChallenge(Http01Challenge.TYPE);
            challenge.trigger();

            // Poll for the challenge to be valid
            while (challenge.getStatus() != Status.VALID) {
                Thread.sleep(3000L);  // Wait 3 seconds before polling again
                challenge.update();
            }
        }

        // Execute the order
        order.execute(csrBuilder.getEncoded());

        // Poll for the order to be valid
        while (order.getStatus() != Status.VALID) {
            Thread.sleep(3000L);  // Wait 3 seconds before polling again
            order.update();
        }

        // Get the certificate
        Certificate certificate = order.getCertificate();

        // Download the certificate chain
        List<X509Certificate> certificateChain = certificate.getCertificateChain();

        // Access the first certificate in the chain
        X509Certificate x509Certificate = certificateChain.get(0);

        // Convert certificate to string
        StringWriter certWriter = new StringWriter();
        try (PemWriter pemWriter = new PemWriter(certWriter)) {
            pemWriter.writeObject(new PemObject("CERTIFICATE", x509Certificate.getEncoded()));
        }

        // Convert KeyPair to string
        StringWriter keyPairWriter = new StringWriter();
        try (JcaPEMWriter jcaPEMWriter = new JcaPEMWriter(keyPairWriter)) {
            jcaPEMWriter.writeObject(domainKeyPair.getPrivate());
        }

        // Store the certificate and key pair in the User model
        user.setCertificate(certWriter.toString());
        user.setPublicKey(keyPairWriter.toString());

        // Save the user in the MongoDB collection
        userRepository.save(user);

        return user;
    }
}
