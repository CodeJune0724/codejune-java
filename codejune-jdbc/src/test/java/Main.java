import com.codejune.jdbc.Query;
import com.codejune.jdbc.access.AccessDatabaseJdbc;
import com.codejune.jdbc.access.AccessDatabaseTable;
import com.codejune.jdbc.query.filter.Compare;

public final class Main {

    public static void main(String[] args) {
        try (AccessDatabaseJdbc accessDatabaseJdbc1 = new AccessDatabaseJdbc("C:\\Users\\14762\\Desktop\\file\\DECLARATION_AGREEMENT.accdb")) {
            AccessDatabaseTable declarationAgreement = accessDatabaseJdbc1.getDefaultDatabase().getTable("DECLARATION_AGREEMENT");

            long l = System.currentTimeMillis();
            declarationAgreement.query(Query.and(Compare.equals("PARADB_ID", "9706900090915022024-01-01 00:00:00")));
            System.out.println(System.currentTimeMillis() - l);
        }
    }

}