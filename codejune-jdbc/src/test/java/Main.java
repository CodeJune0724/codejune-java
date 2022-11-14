import com.codejune.jdbc.Column;
import com.codejune.jdbc.mysql.MysqlJdbc;
import com.codejune.jdbc.mysql.MysqlTable;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        MysqlJdbc mysqlJdbc = new MysqlJdbc("192.168.74.100", 3306, "TEST", "root", "root");
        List<MysqlTable> test = mysqlJdbc.getTables("TEST");
        System.out.println();
    }

}