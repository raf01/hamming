import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by rafael on 08/10/16.
 */
public class Test {
    public static void main(String[] args) {
        try {
            FileOutputStream fos = new FileOutputStream("msg");
            fos.write(255);
            fos.write(255);
            fos.write(255);
            fos.write(255);
            fos.close();
            HammingCode hc = new HammingCode(new File("msg"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
