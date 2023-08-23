package esign.controller;

import esign.model.FileUpload;
import esign.model.Groups;
import esign.model.User;
import esign.service.BlobStorageService;
import esign.service.GroupsService;
import esign.service.UserService;
import esign.service.UserService.SignatureInfo;
import esign.service.UserService.SignatureInfo2;

import java.util.ArrayList;
import java.util.List;
import esign.model.signature;
import esign.repository.SignatureRepository;

import java.util.Date;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class UserController {
	
	@Autowired
	private SignatureRepository signatureRepository;

    @Autowired
    private UserService userService;
    
    @Autowired
    private GroupsService groupsService;
    
    @Autowired
    private BlobStorageService blobStorageService;

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        return "register"; // Updated the template name to "register.html"
    }
    
    
    @GetMapping("/")
    public String homePage() {
        return "home1";
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
//            List<SignatureInfo> signatures = userService.findSignaturesByUsername1(username);
            List<String> formattedDurations = new ArrayList<>();
//            for (SignatureInfo signatureInfo : signatures) {
//                long durationMillis = signatureInfo.getDuration();
//                String formattedDuration = userService.formatDuration(durationMillis);
//                formattedDurations.add(formattedDuration);
//            }

			/*
			 * // Get the list of files to be signed and format their durations
			 * List<SignatureInfo2> filesToBeSigned =
			 * userService.findSignaturesByUsername2(username); List<String>
			 * formattedDurationsForFilesToBeSigned = new ArrayList<>(); for (SignatureInfo2
			 * signatureInfo2 : filesToBeSigned) { long durationMillis =
			 * signatureInfo2.getDuration(); String formattedDuration =
			 * userService.formatDuration(durationMillis);
			 * formattedDurationsForFilesToBeSigned.add(formattedDuration); }
			 */
         // Query the uploaded files for the logged-in user
            List<FileUpload> userFiles = userService.getUserFiles1(user.getId()); // You'll need to create this method
            List<String> groupNames = groupsService.getGroupNamesByUsername(username);
            model.addAttribute("groups", groupNames);

            // Add the list of uploaded files to the model
            model.addAttribute("files", userFiles);
            // Add the list of uploaded files to the model
            model.addAttribute("files", userFiles);
            model.addAttribute("username", username);
//            model.addAttribute("signatures", signatures);
            model.addAttribute("formattedDurations", formattedDurations);
//            model.addAttribute("filesToBeSigned", filesToBeSigned); // Add the list of files to be signed
//            model.addAttribute("formattedDurationsForFilesToBeSigned", formattedDurationsForFilesToBeSigned); // Add the list of formatted durations for files to be signed
            System.out.println(groupNames); // Debugging message

            return "postlogin";
        } else {
            model.addAttribute("loginError", "Invalid username or password");
            return "postlogin";
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
    @GetMapping("/userfiles")
    public String listUserFiles(@RequestParam("username") String username, Model model) {
        List<String> userFiles = userService.getUserFiles(username);
        List<String> signedFiles = new ArrayList<>();

        System.out.println("User Files: " + userFiles); // Debug print

        for (String file : userFiles) {
            boolean isSigned = userService.checkSignatureFileExists(username, file);
            System.out.println("File: " + file + ", Is Signed: " + isSigned); // Debug print
            if (isSigned) {
                signedFiles.add(file);
            }
        }

        System.out.println("Signed Files: " + signedFiles); // Debug print

        model.addAttribute("username", username);
        model.addAttribute("userFiles", userFiles);
        model.addAttribute("signedFiles", signedFiles);

        return "userfiles"; // a new HTML template that displays the files
    }

    
    @PostMapping("/sign")
    public String signFile(@RequestParam("username") String username,
                           @RequestParam("filename") String filename,
                           Model model) {
        try {
            // Fetch the user by their username
            User user = userService.findByUsername(username);
            // Check if a user with the provided username exists
            if (user != null) {
                userService.signFile(user.getId().toString(), filename);
                model.addAttribute("message", "File signed successfully");
                return "sign_success";
            } else {
                model.addAttribute("message", "User not found");
                return "sign_error";
            }
        } catch (Exception e) {
            model.addAttribute("message", "Failed to sign file");
            return "sign_error";
        }
    }
    @PostMapping("/send")
    public String sendFile(@RequestParam("username1") String username,
				            @RequestParam(value = "username12", required = false) String receiverUsername,
				            @RequestParam(value = "groupName1", required = false) String groupName,
				            @RequestParam("selectedFileName") String filename1,
				            @RequestParam("expiryDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date expiryDate,
				            Model model) {
    	String filename = "/" + filename1;
        username = username.replace(",", "");
        if (groupName != null && !groupName.isEmpty()) {
            groupName = groupName.replace(",", "");
        }
        System.out.println("Sender Username: " + username); 
        System.out.println("Receves Username: " + receiverUsername);// Print sender username
        System.out.println("Receves filename: " + filename);// Print sender username
        System.out.println("Receves Username: " + groupName);// Print sender username

        try {
            System.out.println("Sender Username: " + username); // Print sender username

            // Fetch the sender by their username
            User sender = userService.findByUsername(username);
            System.out.println("Sender User: " + sender); // Print sender user object

            List<String> receiverUsernames = new ArrayList<>();
            if (receiverUsername != null && !receiverUsername.isEmpty()) {
                receiverUsernames.add(receiverUsername);
            } else if (groupName != null && !groupName.isEmpty()) {
                receiverUsernames = groupsService.getGroupMembersByUsernameAndGroupName(username, groupName);
                if (receiverUsernames == null) {
                    throw new IllegalArgumentException("Group not found");
                }
            }

            System.out.println("Receiver Usernames: " + receiverUsernames); // Print all receiver usernames

            // Check if the sender exists and there is at least one receiver
            if (sender != null && !receiverUsernames.isEmpty()) {
                System.out.println("Transferring file..."); // Debugging message
                for (String receiverId : receiverUsernames) {
                    blobStorageService.transferFile(sender.getId().toString(), receiverId, filename);
                }

                System.out.println("Creating signature object..."); // Debugging message
                // Create a new signature object
                signature signatureObj = new signature();
                signatureObj.setUsername1(sender.getUsername());
                signatureObj.setUsername2(receiverUsernames);
                signatureObj.setFileName(filename);
                signatureObj.setIssuanceDate(new Date()); // Current date
                signatureObj.setExpiryDate(expiryDate);
                signatureObj.setFileNameUID(UUID.randomUUID().toString()); // Unique ID

                // Save the signature to the database
                System.out.println("Saving signature object..."); // Debugging message
                signatureRepository.save(signatureObj);

                model.addAttribute("message", "File sent successfully");
                return "send_success";
            } else {
                model.addAttribute("message", "Sender or Receiver not found");
                System.out.println("Error: Sender or Receiver not found"); // Updated error message
                return "send_error";
            }
        } catch (Exception e) {
            System.out.println("Exception caught: " + e.getMessage()); // Print exception message
            e.printStackTrace(); // Print the full stack trace
            model.addAttribute("message", "Failed to send file");
            return "send_error";
        }
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadFile(@RequestParam("username") String username, @RequestParam("filename") String filename) {
        byte[] fileContent = blobStorageService.downloadFile(username, filename);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .body(fileContent);
    }

    @GetMapping("/downloadsigned")
    public ResponseEntity<byte[]> downloadSignedFile(@RequestParam("username") String username, @RequestParam("filename") String filename) {
        byte[] fileContent = blobStorageService.downloadsignedFile(username, filename);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=\"" + "signed_" + filename + "\"")
                .body(fileContent);
    }

    @PostMapping("/addgroup")
    public ResponseEntity<String> addGroup(@RequestBody Groups group) {
        try {
            groupsService.createGroup(group.getOwner(), group.getGroupname(), group.getMembers());
            return new ResponseEntity<>("Group created successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("upload_error", HttpStatus.BAD_REQUEST); // Replace with the correct error message
        }
    }


}
