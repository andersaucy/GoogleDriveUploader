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
import com.google.api.services.drive.model.Permission;
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
        
        java.io.File rehearsalFolder = new java.io.File("/Users/Andersaucy/Desktop/Unique_Rehearsal");
        Scanner in = new Scanner(System.in);
    	System.out.println("What is the date of the rehearsal? Format: [Day-MonthDate]");
    	String rehearsalDate = in.nextLine();
    	
    	OsCheck.OSType osType= OsCheck.getOperatingSystemType();

	    java.io.File[] rehearsalArray = rehearsalFolder.listFiles(new java.io.FilenameFilter() {
	        public boolean accept(java.io.File dir, String name) {
	            return name.toLowerCase().endsWith(".mp4");
	        }
	    });

	    assert rehearsalArray != null;
	     //This sort is effective because the phone saves files by time stamp, or filename
	    Arrays.sort(rehearsalArray);
	
	    System.out.println("There are " + rehearsalArray.length + " files. What is the order of pieces [separated by commas]");
	    String[] pieces;
	
	    pieces = in.nextLine().split("\\s*,\\s*");
	    while (rehearsalArray.length != pieces.length){
	        System.out.println("Error. Not " + rehearsalArray.length + " files listed. Try Again.");
	        pieces = in.nextLine().split("\\s*,\\s*");
	    }
	
	    //Determine paths based on Operating System
	    String DASH = "";
	    switch (osType) {
	        case Windows:
	            DASH = "\\";
	            break;
	        case MacOS:
	            DASH = "/";
	            break;
	        case Linux:
	            break;
	        case Other:
	            break;
	    }
	    
	    HashMap<java.io.File, java.io.File> oldToNew = new HashMap<java.io.File,java.io.File>();
		
	    for (int i = 0; i < rehearsalArray.length; i++) {
	        String ext = rehearsalArray[i].getName().substring(rehearsalArray[i].getName().indexOf(".") + 1);
	        if(rehearsalArray[i].isFile() && ext.equalsIgnoreCase("mp4")){
	
	        //Retrieve Old Filename
	           java.io.File oldFile = new java.io.File(rehearsalFolder +
	        		   DASH + rehearsalArray[i].getName());
	           String oldFileName = rehearsalArray[i].getName();
	        //Retrieve New Filename
	           String newFileName = rehearsalDate + "-" + pieces[i];
	           java.io.File newFile = new java.io.File(rehearsalFolder +
	                   DASH + newFileName + ".mp4");
	           System.out.println("Renaming " + oldFileName + " to " + newFileName);
	        //Load for Renaming process
	           oldToNew.put(oldFile, newFile);
	         }
	     }
	     
	     //Confirmation Prompt
	     System.out.println("Confirm (This will begin the upload)? (Yes/No)");
	     String confirm = in.nextLine().toUpperCase();
	     List<java.io.File> uploadReady = null;
	     switch (confirm){
	         case "YES":
	             uploadReady = Rename(oldToNew);
	             break;
	         case "NO":
	        	 System.out.println("Try Again");
	             System.exit(0);
	             break;
	     }

    	in.close();
    	//The current parent folder is for Winter Training 2018
    	
    	//Upload folder with date
        String seasonFolderId = "***REMOVED***";
        insertPermission(service, seasonFolderId);
    	File folderMetadata = new File();
        folderMetadata.setName(rehearsalDate);
        folderMetadata.setParents(Collections.singletonList(seasonFolderId));
        folderMetadata.setMimeType("application/vnd.google-apps.folder");
        File folder = service.files().create(folderMetadata)
            .setFields("id, webViewLink")
            .execute();
        insertPermission(service, folder.getId());
        System.out.println("Folder ID: " + folder.getId());
        
        //Upload files within the newly created folder
        for (java.io.File vid : uploadReady) {
	        File fileMetadata = new File();
	        fileMetadata.setName(vid.getName());
	        fileMetadata.setParents(Collections.singletonList(folder.getId()));
	        FileContent mediaContent = new FileContent("video/mp4", vid.getAbsoluteFile());
	        File file = service.files().create(fileMetadata, mediaContent)
	            .setFields("id")
	            .execute();
	        insertPermission(service, file.getId());
	        System.out.println("File ID: " + file.getId());
     	}
        System.out.println(folder.getWebViewLink());
    }

	public static List<java.io.File> Rename(HashMap<java.io.File, java.io.File> oldToNew){
		List<java.io.File> renamedFileList = new ArrayList<java.io.File>();
	    for (Map.Entry<java.io.File, java.io.File> entry : oldToNew.entrySet()){
	        entry.getKey().renameTo(entry.getValue());
	        renamedFileList.add(entry.getValue());
	    }
	    System.out.println("RENAMING SUCCESSFUL. NOW UPLOADING");
		return renamedFileList;
	}
	
	private static Permission insertPermission(Drive service, String fileId) {
		Permission newPermission = new Permission();

		newPermission.setType("anyone");
		newPermission.setRole("reader");
		try {
			return service.permissions().create(fileId, newPermission)
					.execute();
		} catch (IOException e) {
			System.out.println("An error occurred: " + e);
		}
		return null;
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
