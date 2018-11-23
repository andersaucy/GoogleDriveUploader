import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.client.http.FileContent;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.*;

public class DriveQuickstart {
    private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quick-start.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = DriveQuickstart.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public static void main(String... args) throws IOException, GeneralSecurityException {
    	
    	
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

      //   Print the names and IDs for up to 10 files.
//        FileList result = service.files().list()
//                .setPageSize(10)
//                .setFields("nextPageToken, files(id, name)")
//                .execute();
//        List<File> files = result.getFiles();
//        if (files == null || files.isEmpty()) {
//            System.out.println("No files found.");
//        } else {
//            System.out.println("Files:");
//            for (File file : files) {
//                System.out.printf("%s (%s)\n", file.getName(), file.getId());
//            }
//        }
        
//        
//        File fileMetadata = new File();
//        fileMetadata.setName("photo.jpg");
//        java.io.File filePath = new java.io.File("video.mp4");
//        FileContent mediaContent = new FileContent("video/mp4", filePath);
//        File file = service.files().create(fileMetadata, mediaContent)
//            .setFields("id")
//            .execute();
//        System.out.println("File ID: " + file.getId());
        
        java.io.File my_folder = new java.io.File("/Users/Andersaucy/Desktop/Unique_Rehearsal");
        Scanner in = new Scanner(System.in);
    	System.out.println("What is the date of the rehearsal? Format: [Day-MonthDate]");
    	String date = in.nextLine();
    	
    	OsCheck.OSType ostype= OsCheck.getOperatingSystemType();

	    java.io.File[] file_array = my_folder.listFiles(new java.io.FilenameFilter() {
	        public boolean accept(java.io.File dir, String name) {
	            return name.toLowerCase().endsWith(".mp4");
	        }
	    });

	    assert file_array != null;
	     //This sort is effective because the phone saves files by timestamp.
	     //LastModified Comparator is risky if certain files are tampered with beforehand
	    Arrays.sort(file_array);
	             //, Comparator.comparingLong(File::lastModified));
	
	    System.out.println("There are " + file_array.length + " files. What is the order of pieces [separated by commas]");
	    String order;
	    String[] pieces;
	
	    order = in.nextLine();
	    pieces = order.split("\\s*,\\s*");
	    while (file_array.length != pieces.length){
	        System.out.println("Error. Not " + file_array.length + " files listed. Try Again.");
	        order = in.nextLine();
	        pieces = order.split("\\s*,\\s*");
	    }
	
	    String dash = "";
	     
	    switch (ostype) {
	        case Windows:
	            dash = "\\";
	            break;
	        case MacOS:
	            dash = "/";
	            break;
	        case Linux:
	            break;
	        case Other:
	            break;
	    }
	    
	    HashMap<java.io.File, java.io.File> renamed_files = new HashMap<java.io.File,java.io.File>();
		
	     for (int i = 0; i < file_array.length; i++) {
	
	         String ext = file_array[i].getName().substring(file_array[i].getName().indexOf(".") + 1);
	
	         if (file_array[i].isFile() && ext.equalsIgnoreCase("mp4")){
	
	            java.io.File my_file = new java.io.File(my_folder +
	                     dash + file_array[i].getName());
	            String long_file_name = file_array[i].getName();
	            
	            String new_file_name = date + "-" + pieces[i];
	            java.io.File renamed_file = new java.io.File(my_folder +
	                     dash + new_file_name + ".mp4");
	
	             System.out.println("Renaming " + long_file_name + " to " + new_file_name);
	
	             renamed_files.put(my_file, renamed_file);
	         }
	     }
	     System.out.println("Confirm? (Yes/No)");
	
	     String confirm = in.nextLine().toUpperCase();
	     List<java.io.File> ready_to_upload = null;
	     switch (confirm){
	         case "YES":
	             ready_to_upload = Rename(renamed_files);
	             break;
	         case "NO":
	        	 System.out.println("Try Again");
	             System.exit(0);
	             break;
	     }

    	in.close();
    	//First upload FOLDER of date
    	//This id is for Winter Training 2018
        String folderId = "***REMOVED***";
    	File folderMetadata = new File();
        folderMetadata.setName(date);
        folderMetadata.setParents(Collections.singletonList(folderId));
        folderMetadata.setMimeType("application/vnd.google-apps.folder");
        File folder = service.files().create(folderMetadata)
            .setFields("id")
            .execute();
        String date_folder_id = folder.getId();
        System.out.println("Folder ID: " + date_folder_id);
        
        for (java.io.File vid : ready_to_upload) {
	        File fileMetadata = new File();
	        fileMetadata.setName(vid.getName());
	        fileMetadata.setParents(Collections.singletonList(date_folder_id));
	        //java.io.File filePath = new java.io.File("/Users/Andersaucy/Desktop/Unique_Rehearsal/" + vid.getName());
	        FileContent mediaContent = new FileContent("video/mp4", vid.getAbsoluteFile());
	        File file = service.files().create(fileMetadata, mediaContent)
	            .setFields("id")
	            .execute();
	        System.out.println("File ID: " + file.getId());
     	}
    }

	public static List<java.io.File> Rename(HashMap<java.io.File, java.io.File> old_to_new){
		List<java.io.File> renamed_file_list = new ArrayList<java.io.File>();
	    for (Map.Entry<java.io.File, java.io.File> entry : old_to_new.entrySet()){
	        entry.getKey().renameTo(entry.getValue());
	        renamed_file_list.add(entry.getValue());
	    }
	    System.out.println("COMPLETE");
		return renamed_file_list;
	}
	
	public static final class OsCheck {
		 /**
	     * types of Operating Systems
	     */
	    public enum OSType {
	        Windows, MacOS, Linux, Other
	    };
	
	    // cached result of OS detection
	    static OSType detectedOS;
	
	    /**
	     * detect the operating system from the os.name System property and cache
	     * the result
	     *
	     * @returns - the operating system detected
	     */
	    static OSType getOperatingSystemType() {
	        if (detectedOS == null) {
	            String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
	            if ((OS.indexOf("mac") >= 0) || (OS.indexOf("darwin") >= 0)) {
	                detectedOS = OSType.MacOS;
	            } else if (OS.indexOf("win") >= 0) {
	                detectedOS = OSType.Windows;
	            } else if (OS.indexOf("nux") >= 0) {
	                detectedOS = OSType.Linux;
	            } else {
	                detectedOS = OSType.Other;
	            }
	        }
	        return detectedOS;
	    }
	}
}
