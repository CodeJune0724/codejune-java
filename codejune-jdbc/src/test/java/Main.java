import com.codejune.core.util.ObjectUtil;
import com.codejune.jdbc.query.Filter;
import com.codejune.jdbc.query.filter.Compare;

public final class Main {

    public static void main(String[] args) {
        Filter and = new Filter().and(Compare.equals("1", "1"));
        Filter clone = ObjectUtil.clone(and);
        System.out.println();
    }

}