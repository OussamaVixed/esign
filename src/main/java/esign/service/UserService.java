package esign.service;

import esign.model.User;
import esign.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BlobStorageService blobStorageService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User register(User user) {
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        User registeredUser = userRepository.save(user);

        // Create a virtual directory for the registered user
        String folderName = registeredUser.getId() + "upload";
        blobStorageService.createUserFolder(folderName);

        return registeredUser;
    }
    public User authenticate(String username, String password) {
        User user = userRepository.findByUsername(username);

        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            return user;
        }

        return null;
    }
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    
}
