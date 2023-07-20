package esign.controller;

import esign.model.User;
import esign.service.BlobStorageService;
import esign.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class UserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private BlobStorageService blobStorageService;

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        return "register"; // Updated the template name to "register.html"
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user, Model model) {
        try {
            User registered = userService.register(user);
            model.addAttribute("user", registered);
            return "registration_success";
        } catch (Exception e) {
            return "error"; // Updated the template name to "error.html"
        }
    }
    @GetMapping("/login")
    public String loginForm(Model model) {
        return "login"; // Will render "login.html"
    }
    @PostMapping("/login")
    public String loginUser(@RequestParam("username") String username,
                            @RequestParam("password") String password,
                            Model model) {
        User user = userService.authenticate(username, password);
        
        if (user != null) {
            model.addAttribute("username", username);
            return "postlogin"; // Will render "upload.html" after successful login
        } else {
            model.addAttribute("loginError", "Invalid username or password");
            return "postlogin"; // Will render "login.html" with error message
        }
    }
    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file,
                             @RequestParam("username") String username,
                             Model model) {
        try {
            // Fetch the user by their username
            User user = userService.findByUsername(username);

            // Check if a user with the provided username exists
            if (user != null) {
                // Upload the file using the user's ID instead of the username
                blobStorageService.uploadFile(user.getId(), file);
                model.addAttribute("message", "File uploaded successfully");
                return "upload_success";
            } else {
                model.addAttribute("message", "User not found");
                return "upload_error";
            }
        } catch (Exception e) {
            model.addAttribute("message", "Failed to upload file");
            return "upload_error";
        }
    }

}
