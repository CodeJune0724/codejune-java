import com.codejune.shell.LinuxClientShell;

public class Main {

    public static void main(String[] args) {
        try (LinuxClientShell linuxClientShell = new LinuxClientShell("113.44.41.225", 22, "root", "June3259")) {
//            linuxClientShell.setListener(System.out::println);
//            linuxClientShell.send("ipconfig\n\n");

            while (true) {}
        }
    }

}