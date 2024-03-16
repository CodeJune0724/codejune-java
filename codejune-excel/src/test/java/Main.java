import com.codejune.Excel;
import com.codejune.common.util.ThreadUtil;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

public class Main {

    public static void main(String[] args) {
        try (Excel excel = new Excel("C:\\Users\\14762\\Desktop\\file\\1.xlsx")) {
            System.out.println();
        }
    }

}