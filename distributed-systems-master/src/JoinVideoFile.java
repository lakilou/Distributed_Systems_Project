import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
public  class JoinVideoFile {

    public static void main(String[] args) {
        try {
            File splitFiles = new File("C:/Documents/Videos_Split/highestintheroom/");// get all files which are to be join
            if (splitFiles.exists()) {
                File[] files = splitFiles.getAbsoluteFile().listFiles();
                if (files.length != 0) {
                    System.out.println("Total files to be join: "+ files.length);

                    String joinFileName = Arrays.asList(files).get(0).getName();
                    System.out.println("Join file created with name -> "+ joinFileName);

                    String fileName = joinFileName.substring(0, joinFileName.lastIndexOf("."));// video fileName without extension
                    File fileJoinPath = new File("C:/Documents/Videos_Join/"+ fileName);// merge video files saved in this location

                    if (!fileJoinPath.exists()) {
                        fileJoinPath.mkdirs();
                        System.out.println("Created Directory -> "+ fileJoinPath.getAbsolutePath());
                    }

                    OutputStream outputStream = new FileOutputStream(fileJoinPath.getAbsolutePath() +"/"+ joinFileName);

                    for (File file : files) {
                        System.out.println("Reading the file -> "+ file.getName());
                        InputStream inputStream = new FileInputStream(file);

                        int readByte = 0;
                        while((readByte = inputStream.read()) != -1) {
                            outputStream.write(readByte);
                        }
                        inputStream.close();
                    }

                    System.out.println("Join file saved at -> "+ fileJoinPath.getAbsolutePath() +"/"+ joinFileName);
                    outputStream.close();
                } else {
                    System.err.println("No Files exist in path -> "+ splitFiles.getAbsolutePath());
                }
            } else {
                System.err.println("This path doesn't exist -> "+ splitFiles.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}