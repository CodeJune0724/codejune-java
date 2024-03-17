import com.codejune.ExcelReader;
import com.codejune.common.Listener;
import com.codejune.excelreader.Cell;
import com.codejune.excelreader.Row;
import com.codejune.excelreader.Sheet;

public class Main {

    public static void main(String[] args) {
        try (ExcelReader excelReader = new ExcelReader("C:\\Users\\14762\\Desktop\\file\\DECLARATION_AGREEMENT.xlsx")) {
            for (Sheet sheet : excelReader) {
                sheet.read(new Listener<Row>() {
                    @Override
                    public void then(Row data) {

                    }
                }, new Listener<Cell>() {
                    @Override
                    public void then(Cell data) {
                        System.out.print(data.getValue() + "\t");
                    }
                }, new Listener<Row>() {
                    @Override
                    public void then(Row data) {
                        System.out.println();
                    }
                });
            }
        }
    }

}