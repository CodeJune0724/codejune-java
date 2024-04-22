import com.codejune.Http;
import com.codejune.Json;
import com.codejune.common.BaseException;
import com.codejune.common.util.ArrayUtil;
import com.codejune.common.util.MapUtil;
import com.codejune.common.util.ObjectUtil;
import com.codejune.http.ContentType;
import com.codejune.http.Type;
import com.codejune.jdbc.Query;
import com.codejune.jdbc.access.AccessDatabaseJdbc;
import com.codejune.jdbc.access.AccessDatabaseTable;
import com.codejune.jdbc.query.filter.Compare;
import com.codejune.uiauto.WebDriver;
import com.codejune.uiauto.webdriver.ChromeWebDriver;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public final class Main {

    public static void main(String[] args) {
        try (
                AccessDatabaseJdbc accessDatabaseJdbc = new AccessDatabaseJdbc(new File("C:\\Users\\14762\\Desktop\\file\\export.accdb"));
                AccessDatabaseJdbc exportTaxRateAccessDatabaseJdbc = new AccessDatabaseJdbc(new File("C:\\Users\\14762\\Desktop\\file\\1.accdb"));
                WebDriver webDriver = new ChromeWebDriver("C:\\Users\\14762\\Desktop\\file\\chromedriver-win64\\chromedriver-win64\\chromedriver.exe", true)
        ) {
            AccessDatabaseTable exportTaxRate = exportTaxRateAccessDatabaseJdbc.getDefaultDatabase().getTable("EXPORT_TAX_RATE");
            webDriver.open("https://www.guanwuxiaoer.com/hscode.php");
            System.out.print("登录:");
            new Scanner(System.in).next();
            for (Map<String, Object> item : accessDatabaseJdbc.getDefaultDatabase().getTable("COMPLEX").queryData()) {
                String hsCode = MapUtil.get(item, "CODE_TS", String.class);
                System.out.println(hsCode);

                // 判断hs是否已经拉取
                if (!exportTaxRate.queryData(Query.and(Compare.equals("HS_CODE", hsCode))).isEmpty()) {
                    System.out.println("已拉取!");
                    continue;
                }

                String encode = ObjectUtil.transform(webDriver.executeScript("JZ0yvmdXOEuxBWV7yKCupBaRPxsmnotyEo2kEXaqhROw0Ze7v1vAr7eciZHxxpAep1cKk0rijq82rWD1mr6bJU2GYtmlQRGEYki_0();return window.gtoKey"), String.class);

                Http http = new Http("https://api.guanwuxiaoer.com/api/hscode/hsCodeSearch", Type.POST);
                http.setContentType(ContentType.FORM_URLENCODED);
                http.setBody(Map.of("key", hsCode, "x", encode, "is_expires", "true"));
                Map<?, ?> response = http.send().parse(Map.class).getBody();
                System.out.println(response);
                Map<?, ?> data = MapUtil.parse(ArrayUtil.get(ArrayUtil.parse(MapUtil.get(response, "data", List.class), Object.class), 0));
                if (data == null) {
                    throw new BaseException("拉取失败");
                }

                // expTariffRate
                String expTariffRate = MapUtil.get(data, "expTariffRate", String.class);

                // expTempRate
                String expTempRate = MapUtil.get(data, "expTempRate", String.class);

                // rebateRate
                String rebateRate = MapUtil.get(MapUtil.parse(Json.parse(MapUtil.get(data, "rebateRate", String.class), List.class).get(0)), "tax", String.class);

                Map<String, Object> map = new HashMap<>();
                map.put("HS_CODE", hsCode);
                map.put("TARIFF_RATE", expTariffRate);
                map.put("PROVISIONAL_RATE", expTempRate);
                map.put("REFUND_RATE", rebateRate);

                exportTaxRate.insert(map);
            }
        }
    }

}