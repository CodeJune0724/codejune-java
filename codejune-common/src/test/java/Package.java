import com.codejune.common.os.File;
import com.codejune.common.os.Folder;

public final class Package {

    private static final String DIR = "C:\\application\\project\\swgd-mongodb-util\\src\\main\\java\\com\\easipass\\swgd\\param\\common";

    private static final String PACKAGE = "com.easipass.swgd.param.common";

    private static final String[] MODULE_LIST = new String[] {"common", "http", "jdbc", "excel"};

    public static void main(String[] args) {
        new Folder(DIR).delete();
        new Folder(DIR);
        for (String module : MODULE_LIST) {
            Folder folder = new Folder(System.getProperty("user.dir") + "/codejune-" + module + "/src/main/java/com/codejune");
            for (Folder item : folder.getFolderList()) {
                item.copy(DIR);
            }
            for (File item : folder.getFileList()) {
                item.copy(DIR);
            }
        }
        setPackage(new Folder(DIR));
    }

    private static void setPackage(Folder folder) {
        for (Folder item : folder.getFolderList()) {
            setPackage(item);
        }
        for (File file : folder.getFileList()) {
            file.write(file.getData().replaceAll("com\\.codejune", PACKAGE));
        }
    }

}