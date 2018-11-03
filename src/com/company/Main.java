package com.company;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Scanner;

public class Main {
//Runs Java on a folder directory in the command line
//Gives prompts on the files in the video, asking for the name of the dance piece
//Renames and uploads to Google Drive in a new folder by date

    public static void main(String[] args) throws IOException {
        File my_folder = new File(args[0]);
        Scanner in = new Scanner(System.in);
        System.out.println("What is the date of the rehearsal? Format : [Month-Date]");
        String date = in.nextLine();

        OsCheck.OSType ostype= OsCheck.getOperatingSystemType();

        File[] file_array = my_folder.listFiles();
        assert file_array != null;
        Arrays.sort(file_array);

        System.out.println("There are " + file_array.length + " files. What is the order of pieces [separted by commas]");
        String order = in.nextLine();
        String[] pieces = order.split("\\s*,\\s*");

        for (int i = 0; i < file_array.length; i++) {
            if (file_array[i].isFile()) {
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
                File my_file = new File(my_folder +
                        dash + file_array[i].getName());
                String long_file_name = file_array[i].getName();

                String new_file_name = date + pieces[i];
                System.out.println("Changing " + long_file_name + " to " + new_file_name);

                my_file.renameTo(new File(my_folder +
                        dash + new_file_name + ".mp4"));
            }
        }

        System.out.println("COMPLETE");
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
