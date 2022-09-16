import com.codejune.common.os.File;
import com.codejune.common.os.Folder;

public final class Package {

    private static final String DIR = "E:\\ZJ\\project\\swgd-mongodb-util\\src\\main\\java\\com\\easipass\\swgd\\param\\common";

    private static final String PACKAGE = "com.easipass.swgd.param.common";

    public static void main(String[] args) {
        handler(new Folder(DIR));
    }

    private static void handler(Folder folder) {
        for (Folder item : folder.getFolderList()) {
            handler(item);
        }
        for (File file : folder.getFileList()) {
            file.write(file.getData().replaceAll("com\\.codejune", PACKAGE));
        }
    }

}