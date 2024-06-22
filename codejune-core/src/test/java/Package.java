import com.codejune.core.os.File;
import com.codejune.core.os.Folder;

public final class Package {

    private static final String DIR = "C:\\application\\project\\swgd-mongodb-util\\src\\main\\java\\com\\easipass\\swgd\\param\\base";

    private static final String PACKAGE = "com.easipass.swgd.param.base";

    private static final String[] MODULE_LIST = new String[] {"core", "http", "jdbc", "excel", "json", "xml"};

    public static void main(String[] args) {
        new Folder(DIR).delete();
        new Folder(DIR);
        for (String module : MODULE_LIST) {
            Folder folder = new Folder(System.getProperty("user.dir") + "/codejune-" + module + "/src/main/java/com/codejune");
            for (Folder item : folder.getFolder()) {
                item.copy(DIR);
            }
            for (File item : folder.getFile()) {
                item.copy(DIR);
            }
        }
        setPackage(new Folder(DIR));
    }

    private static void setPackage(Folder folder) {
        for (Folder item : folder.getFolder()) {
            setPackage(item);
        }
        for (File file : folder.getFile()) {
            file.write(file.getData().replaceAll("com\\.codejune", PACKAGE));
        }
    }

}