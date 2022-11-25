import com.codejune.common.DataType;
import com.codejune.common.util.BeanUtil;

public class Main {

    public static void main(String[] args) {
        String isNullable = BeanUtil.getGetterMethodName("isNullable", DataType.BOOLEAN);
        System.out.println(isNullable);
    }

}