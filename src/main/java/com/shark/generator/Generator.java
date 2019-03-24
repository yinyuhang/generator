package com.shark.generator;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Map;

public interface Generator {
    static void init(Class<?>... classes) {
        Arrays.stream(classes).forEach(Generator::init);
    }

    static void initPojo(Map<String, Object> dataMap) {
        try {
            File file = Utils.initOutputFile(FileType.POJO,
                    dataMap.get("package").toString(), dataMap.get("name").toString());
            System.out.println(file);
            FileOutputStream fos = new FileOutputStream(file);
            Utils.generate(Utils.getTemplate(FileType.POJO), dataMap, fos);
            fos.close();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    static void init(Class<?> cls) {
        try {
            FileType[] types = {FileType.REPOSITORY, FileType.CONTROLLER, FileType.HTML};
            for (FileType type : types) {
                Map<String, Object> dataMap = Utils.analyze(cls);
                String packageName = dataMap.get("package").toString();
                String entityName = dataMap.get("name").toString();
                if (FileType.HTML.equals(type)) {
                    packageName = "";
                }
                File file = Utils.initOutputFile(type, packageName, entityName);
                System.out.println(file);
                FileOutputStream fos = new FileOutputStream(file);
                Utils.generate(Utils.getTemplate(type), dataMap, fos);
                fos.close();
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
