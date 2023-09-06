package esign.util;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
@Service
public class Multitobytes {
	
	public byte[] multipartFileToByteArray(MultipartFile file) {
	    try {
	        return file.getBytes();
	    } catch (Exception e) {
	        throw new RuntimeException("Failed to convert MultipartFile to byte array", e);
	    }
	}

}
