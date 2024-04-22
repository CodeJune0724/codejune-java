import com.codejune.uiauto.WebDriver;
import com.codejune.uiauto.webDriver.ChromeWebDriver;

public final class Main {

    public static void main(String[] args) {
        try (WebDriver webDriver = new ChromeWebDriver("C:\\Users\\14762\\Desktop\\file\\chromedriver-win64\\chromedriver-win64\\chromedriver.exe", true)) {
            webDriver.open("https://www.guanwuxiaoer.com/hscode.php");
            System.out.println();
        }
    }

}