import com.codejune.common.BaseException;
import com.codejune.common.ClassInfo;
import com.codejune.common.os.File;
import com.codejune.common.util.ArrayUtil;
import com.codejune.common.util.StringUtil;
import com.codejune.service.BasePO;
import jakarta.persistence.Column;
import java.util.Date;

public final class CreatePO {

    private static final Class<? extends BasePO<?>> BASE_PO_CLASS = null;

    private static final String PACKAGE = "com.hytmanage";

    private static final String OUT_PATH = "C:\\application\\project\\hyt_manage";

    public static void main(String[] args) {
        if (BASE_PO_CLASS == null || StringUtil.isEmpty(PACKAGE) || StringUtil.isEmpty(OUT_PATH)) {
            return;
        }
        ClassInfo classInfo = new ClassInfo(BASE_PO_CLASS);
        ClassInfo basePOClass = classInfo.getSuperClass(BasePO.class);
        if (basePOClass == null) {
            return;
        }
        Class<?> idClass = basePOClass.getGenericClass().getFirst().getRawClass();
        createService(idClass);
        createController(idClass);
        createUiPO(idClass);
        createUiService(idClass);
    }

    private static void createService(Class<?> idClass) {
        if (idClass == null) {
            throw new BaseException("idClass is null");
        }
        new File(new java.io.File(new java.io.File(OUT_PATH, "src/main/java/" + PACKAGE.replace(".", "/") + "/service").getAbsolutePath(), getFileName("Service") + ".java"))
                .write("package " + PACKAGE + ".service;\r\n" +
                        "\r\n" +
                        "import com.codejune.service.Database;\r\n" +
                        "import com.codejune.service.POService;\r\n" +
                        "import " + PACKAGE + ".po." + BASE_PO_CLASS.getSimpleName() + ";\r\n" +
                        "import org.springframework.stereotype.Service;\r\n" +
                        "\r\n" +
                        "@Service\r\n" +
                        "public class " + getFileName("Service") + " extends POService<" + BASE_PO_CLASS.getSimpleName() + ", " + idClass.getSimpleName() + "> {\r\n" +
                        "    \r\n" +
                        "    @Override\r\n" +
                        "    public Database getDatabase() {\r\n" +
                        "        return " + PACKAGE + ".Database.getInstance();\r\n" +
                        "    }\r\n" +
                        "\r\n" +
                        "    @Override\r\n" +
                        "    public Class<" + BASE_PO_CLASS.getSimpleName() + "> getPOClass() {\r\n" +
                        "        return " + BASE_PO_CLASS.getSimpleName() + ".class;\r\n" +
                        "    }\r\n" +
                        "    \r\n" +
                        "}");
    }

    private static void createController(Class<?> idClass) {
        if (idClass == null) {
            throw new BaseException("idClass is null");
        }
        new File(new java.io.File(new java.io.File(OUT_PATH, "src/main/java/" + PACKAGE.replace(".", "/") + "/controller").getAbsolutePath(), getFileName("Controller") + ".java"))
                .write("package " + PACKAGE + ".controller;\r\n" +
                        "\r\n" +
                        "import com.codejune.service.POController;\r\n" +
                        "import com.codejune.service.POService;\r\n" +
                        "import " + PACKAGE + ".po." + BASE_PO_CLASS.getSimpleName() + ";\r\n" +
                        "import " + PACKAGE + ".service." + getFileName("Service") + ";\r\n" +
                        "import jakarta.annotation.Resource;\r\n" +
                        "import org.springframework.web.bind.annotation.*;\r\n" +
                        "\r\n" +
                        "@RestController\r\n" +
                        "@RequestMapping(\"/api/" + getFirstSmallLetter(BASE_PO_CLASS.getSimpleName()).substring(0, getFirstSmallLetter(BASE_PO_CLASS.getSimpleName()).length() - 2) + "\")\r\n" +
                        "public class " + getFileName("Controller") + " extends POController<" + BASE_PO_CLASS.getSimpleName() + ", " + idClass.getSimpleName() + "> {\r\n" +
                        "\r\n" +
                        "    @Resource\r\n" +
                        "    private " + getFileName("Service") + " " + getFirstSmallLetter(getFileName("Service")) + ";\r\n" +
                        "\r\n" +
                        "    @Override\r\n" +
                        "    public POService<" + BASE_PO_CLASS.getSimpleName() + ", " + idClass.getSimpleName() + "> getService() {\r\n" +
                        "        return " + getFirstSmallLetter(getFileName("Service")) + ";\r\n" +
                        "    }\r\n" +
                        "\r\n" +
                        "}");
    }

    private static void createUiPO(Class<?> idClass) {
        if (idClass == null) {
            throw new BaseException("idClass is null");
        }
        new File(new java.io.File(new java.io.File(OUT_PATH, "ui/src/po").getAbsolutePath(), getFileName("PO") + ".ts"))
                .write("import BasePO from \"./BasePO\";\r\n" +
                        "\r\n" +
                        "export default class " + BASE_PO_CLASS.getName() + " extends BasePO<" + getTsType(idClass) + "> {\r\n" +
                        "\r\n" + ArrayUtil.toString(new ClassInfo(BASE_PO_CLASS).getFields(), field -> {
                            if (!field.isAnnotation(Column.class)) {
                                return null;
                            }
                            String filedString = field.getName();
                            String type = getTsType(field.getType());
                            if (field.getAnnotation(Column.class).nullable()) {
                                type = type + " | null";
                            }
                            String value = "null";
                            if (!field.getAnnotation(Column.class).nullable()) {
                                ClassInfo classInfo = new ClassInfo(field.getType());
                                if (classInfo.isInstanceof(Number.class)) {
                                    value = "0";
                                } else if (classInfo.isInstanceof(String.class)) {
                                    value = "\"\"";
                                } else if (classInfo.isInstanceof(Boolean.class)) {
                                    value = "false";
                                } else if (classInfo.isInstanceof(Date.class)) {
                                    value = "\"\"";
                                } else {
                                    throw new BaseException("classInfo未配置");
                                }
                            }
                            return "    " + filedString + ": " + type + " = " + value + ";";
                        }, "\r\n\r\n") + "\r\n" +
                        "\r\n" +
                        "};");
    }

    private static void createUiService(Class<?> idClass) {
        if (idClass == null) {
            throw new BaseException("idClass is null");
        }
        new File(new java.io.File(new java.io.File(OUT_PATH, "ui/src/service").getAbsolutePath(), getFileName("Service") + ".ts"))
                .write("import POService from \"./POService\";\r\n" +
                        "import " + BASE_PO_CLASS.getName() + " from \"../po/" + BASE_PO_CLASS.getName() + "\";\r\n" +
                        "\r\n" +
                        "export default class " + getFileName("Service") + " extends POService<" + BASE_PO_CLASS.getName() + ", " + getTsType(idClass) + "> {\r\n" +
                        "\r\n" +
                        "    constructor() {\r\n" +
                        "        super(\"" + getFirstSmallLetter(BASE_PO_CLASS.getName()).substring(0, getFirstSmallLetter(BASE_PO_CLASS.getName()).length() - 2) + "\");\r\n" +
                        "    }\r\n" +
                        "\r\n" +
                        "};");
    }

    private static String getFileName(String suffix) {
        String result = BASE_PO_CLASS.getName();
        if (StringUtil.isEmpty(suffix)) {
            return result;
        }
        return result.substring(0, result.length() - 2) + suffix;
    }

    private static String getFirstSmallLetter(String data) {
        if (StringUtil.isEmpty(data)) {
            throw new BaseException("data is null");
        }
        return data.substring(0, 1).toLowerCase() + data.substring(1);
    }

    private static String getTsType(Class<?> aClass) {
        ClassInfo classInfo = new ClassInfo(aClass);
        if (classInfo.isInstanceof(Number.class)) {
            return "number";
        } else if (classInfo.isInstanceof(String.class)) {
            return "string";
        } else if (classInfo.isInstanceof(Boolean.class)) {
            return "boolean";
        } else if (classInfo.isInstanceof(Date.class)) {
            return "string";
        } else {
            throw new BaseException("aClass未配置");
        }
    }

}