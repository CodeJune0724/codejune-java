import com.codejune.Excel;
import com.codejune.core.BaseException;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class Main {

    public static void main(String[] args) {
        try (Excel excel = new Excel("C:\\Users\\14762\\Desktop\\file\\1.xlsx")) {
            try (InputStream inputStream = new FileInputStream("C:\\Users\\14762\\Desktop\\file\\公章.png")) {
                excel.getSheet(0).addImage(inputStream, null, null);
                excel.save(new File("C:\\Users\\14762\\Desktop\\file\\2.xlsx"));
            } catch (Exception e) {
                throw new BaseException(e);
            }
        }
    }

}