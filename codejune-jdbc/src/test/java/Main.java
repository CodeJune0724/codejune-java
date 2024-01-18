import com.codejune.jdbc.oracle.OracleJdbc;
import com.codejune.jdbc.query.Filter;
import com.codejune.jdbc.query.filter.Compare;
import com.codejune.jdbc.util.SqlBuilder;
import java.util.ArrayList;

public final class Main {

    public static void main(String[] args) {
        String s = new SqlBuilder("TEST", OracleJdbc.class).parseWhere(new Filter().and(Compare.equals("NAME", "ZJ")).and(Compare.in("TEST", new ArrayList<>())));
        System.out.println(s);
    }

}