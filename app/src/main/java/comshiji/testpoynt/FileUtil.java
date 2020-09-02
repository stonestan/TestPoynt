package comshiji.testpoynt;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class FileUtil {
    private static String path = Environment.getExternalStorageDirectory() + "/testPoynt.txt";
    public static void saveTransactionContent(String content){
        FileWriter fwriter = null;
        try {
            fwriter = new FileWriter(path);
            fwriter.write(content);
        } catch (IOException ex)
        {		ex.printStackTrace();
        } finally {
            try {
                fwriter.flush();
                fwriter.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static String getTransactionContent(){
        File file = new File(path);
        if(!file.exists()){
            return "";
        }
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            StringBuffer sb = new StringBuffer();
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            fis.close();
            return sb.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
