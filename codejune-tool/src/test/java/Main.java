import com.codejune.tool.cloudstoragedownload.YukaidiCloudStorageDownload;

public final class Main {

    static void main() {
        YukaidiCloudStorageDownload yukaidiCloudStorageDownload = new YukaidiCloudStorageDownload();
        String directUrl = yukaidiCloudStorageDownload.getDirectUrl("G9PGcx/僵尸开炮TOOL-8.76.exe");
        System.out.println(directUrl);
    }

}