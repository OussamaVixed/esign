package esign.controller;

import esign.model.FileUpload;
import esign.model.Groups;
import esign.model.SignatureStatus;
import esign.model.User;
import esign.service.BlobStorageService;
import esign.service.GroupsService;
import esign.service.SignatureService;
import esign.service.UserService;
import esign.util.Multitobytes;

import java.util.ArrayList;
import java.util.List;
import esign.model.signature;
import esign.repository.SignatureRepository;
import esign.repository.SignatureStatusRepository;

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
	private SignatureStatusRepository signatureStatusRepository;
	@Autowired
	private Multitobytes multitobytes;
	@Autowired
	private SignatureRepository signatureRepository;

    @Autowired
    private UserService userService;
    @Autowired
    private SignatureService signatureService;
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
            SignatureStatus signatureStatus = signatureStatusRepository.findBySender(username);
            List<String> formattedDurations = new ArrayList<>();
            List<FileUpload> userFiles = userService.getUserFiles1(user.getId()); // You'll need to create this method
            List<String> groupNames = groupsService.getGroupNamesByUsername(username);
            List<Boolean> issigned = signatureStatus.getIssigned();
            List<String> Username2 = signatureStatus.getUsername2();
            List<Date> sigdate1 = signatureStatus.getSigdate();
            List<String> filenamess = signatureStatus.getFileid();
            List<String> sigdate = userService.getTimeRemaining(sigdate1);
            model.addAttribute("issigned", issigned);
            model.addAttribute("Username2", Username2);
            model.addAttribute("sigdate", sigdate);
            model.addAttribute("filenamess", filenamess);
            model.addAttribute("groups", groupNames);
            model.addAttribute("files", userFiles);
            model.addAttribute("files", userFiles);
            model.addAttribute("username", username);
            model.addAttribute("formattedDurations", formattedDurations);
            System.out.println(groupNames); // Debugging message
            System.out.println(sigdate);
            return "postlogin";
        } else {
            model.addAttribute("loginError", "Invalid username or password");
            return "postlogin";
        }
    }


    @PostMapping("/upload1")
    public String uploadFile1(@RequestParam("file1") MultipartFile file,
                             @RequestParam("username2") String username,
                             Model model) {
        System.out.println(file);
        System.out.println(username);
        
        String filename = "/" + file.getOriginalFilename();  // Get the filename here as a String

        try {
            // Fetch the user by their username
            User user = userService.findByUsername(username);

            // Check if a user with the provided username exists
            if (user != null) {
                // Upload the file using the user's ID instead of the username
                blobStorageService.uploadFile(user.getId(), file);
                model.addAttribute("message", "File uploaded successfully");

                // Sign the uploaded file
                userService.signFile(user.getId().toString(), filename, username);
                model.addAttribute("message", "File signed successfully");
                
                // Update signature status
                signatureService.updateIssigned(username, filename);
                
                return "sign_success";
            } else {
                model.addAttribute("message", "User not found");
                return "upload_error";
            }
        } catch (Exception e) {
            model.addAttribute("message", "Failed to upload and sign file");
            return "upload_error";
        }
    }

    @PostMapping("/upload2")
    public String uploadFile2(@RequestParam("file1") MultipartFile file,
                              @RequestParam("username2") String username,
                              Model model) {
        try {
            System.out.println("Entering the uploadFile2 method.");

            // Fetch the user by their username
            User user = userService.findByUsername(username);
            System.out.println("Fetched user by username: " + username);

            // Check if a user with the provided username exists
            if (user != null) {
                System.out.println("User exists.");

                // Upload the file using the user's ID
                blobStorageService.simpleUploadFile(user.getId(), file);
                System.out.println("File uploaded.");

                // Get the filename as a String
                String filename = "/" + file.getOriginalFilename();
                System.out.println("File name: " + filename);

                // Retrieve the signer's username from the PDF
                String signerUsername = blobStorageService.getSignerUsernameFromPdfBlob(user.getId(), filename);
                String signerDate = blobStorageService.getSignatureDateFromPdfBlob(user.getId(), filename);
                System.out.println("Signer date: " + signerDate);
                System.out.println("Signer Username: " + signerUsername);

                // Convert the MultipartFile to byte array
                byte[] pdfData = multitobytes.multipartFileToByteArray(file);
                System.out.println("Converted MultipartFile to byte array.");
                Boolean xxx1 = userService.verifyPdfSignature(pdfData, signerUsername);
                blobStorageService.simpleDeleteFile(user.getId(), filename);

                // Verify the PDF signature
                if (xxx1) {
                    System.out.println("File and signature verified successfully.");
                    model.addAttribute("message", "File and signature verified successfully");
                    model.addAttribute("signerUsername", signerUsername);
                    model.addAttribute("signerDate", signerDate);
                    return "successful_verification";
                } else {
                    System.out.println("Signature verification failed.");
                    model.addAttribute("message", "Signature verification failed");
                    return "failed_verification";
                }
            } else {
                System.out.println("User not found.");
                model.addAttribute("message", "User not found");
                return "failed_verification";
            }
        } catch (Exception e) {
            System.out.println("Exception occurred: " + e.getMessage());
            model.addAttribute("message", "Failed to upload and verify file");
            return "failed_verification";
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
                userService.signFile(user.getId().toString(), filename,username);
                model.addAttribute("message", "File signed successfully");
                signatureService.updateIssigned(username,filename);
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
        System.out.println("Receves filename: " + filename1);// Print sender username
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
                	User receiver1 = userService.findByUsername(receiverId);
                	System.out.println(receiver1.getId().toString());
                    blobStorageService.transferFile(sender.getId().toString(), receiver1.getId().toString(), filename);
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
                signatureObj.setGroupName(groupName);
                // Save the signature to the database
                System.out.println("Saving signature object..."); // Debugging message
                signatureRepository.save(signatureObj);
                SignatureStatus signatureStatus = signatureStatusRepository.findBySender(username);
                for (String receiver : receiverUsernames) {
                    signatureStatus.getFileid().add(filename);  // Duplicate filename
                    signatureStatus.getIssigned().add(false);    // Duplicate false (0) for issigned
                    signatureStatus.getSigdate().add(expiryDate);// Duplicate expiryDate for sigdate
                    signatureStatus.getUsername2().add(receiver); // Add each username from receiverUsernames
                    signatureStatus.getSigID().add(signatureObj.getId());  // Duplicate signatureObj ID
                }
                
                // Save the updated object back into the database
                signatureStatusRepository.save(signatureStatus);

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
