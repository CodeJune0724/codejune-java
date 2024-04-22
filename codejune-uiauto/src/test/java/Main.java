import com.codejune.uiauto.WebDriver;
import com.codejune.uiauto.webdriver.ChromeWebDriver;

public final class Main {

    public static void main(String[] args) {
        try (WebDriver webDriver = new ChromeWebDriver("C:\\Users\\14762\\Desktop\\file\\chromedriver-win64\\chromedriver-win64\\chromedriver.exe")) {
            webDriver.open("https://www.guanwuxiaoer.com/hscode.php");
            Object o = webDriver.executeScript("JZ0yvmdXOEuxBWV7yKCupBaRPxsmnotyEo2kEXaqhROw0Ze7v1vAr7eciZHxxpAep1cKk0rijq82rWD1mr6bJU2GYtmlQRGEYki_0();return window.gtoKey");
            Object o2 = webDriver.executeScript("JZ0yvmdXOEuxBWV7yKCupBaRPxsmnotyEo2kEXaqhROw0Ze7v1vAr7eciZHxxpAep1cKk0rijq82rWD1mr6bJU2GYtmlQRGEYki_0();return window.gtoKey");
            Object o3 = webDriver.executeScript("JZ0yvmdXOEuxBWV7yKCupBaRPxsmnotyEo2kEXaqhROw0Ze7v1vAr7eciZHxxpAep1cKk0rijq82rWD1mr6bJU2GYtmlQRGEYki_0();return window.gtoKey");
            System.out.println();
        }
    }

}