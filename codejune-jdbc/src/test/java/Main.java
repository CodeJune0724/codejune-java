import com.codejune.common.DataType;
import com.codejune.jdbc.Column;
import com.codejune.jdbc.access.AccessDatabaseJdbc;
import com.codejune.jdbc.access.AccessDatabaseTable;
import com.codejune.jdbc.mysql.MysqlJdbc;
import com.codejune.jdbc.mysql.MysqlTable;
import com.codejune.jdbc.oracle.OracleJdbc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
//        MysqlJdbc mysqlJdbc = new MysqlJdbc("192.168.74.100", 3306, "TEST", "root", "root");
        AccessDatabaseJdbc jdbc = new AccessDatabaseJdbc(new File("C:\\Users\\ZJ\\Desktop\\file\\Database.accdb"));
//        OracleJdbc jdbc = new OracleJdbc("192.168.130.138", 1521, "dev12c", "SYSTEM", "easipass");


        List<Column> columns = new ArrayList<>();

        Column column1 = new Column();
        column1.setName("ID");
        column1.setRemark("ID");
        column1.setNullable(false);
        column1.setPrimaryKey(true);
        column1.setAutoincrement(true);
        columns.add(column1);

        Column column2 = new Column();
        column2.setName("NAME");
        column2.setRemark("姓名");
        column2.setDataType(DataType.STRING);
        column2.setLength(255);
        columns.add(column2);

        jdbc.createTable("T_TEST", null, columns);
    }

}