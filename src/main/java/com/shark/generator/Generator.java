package com.shark.generator;

import org.beetl.core.Configuration;
import org.beetl.core.GroupTemplate;
import org.beetl.core.Template;
import org.beetl.core.resource.ClasspathResourceLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Map;

public interface Generator {
    static void init(Class<?>... classes) {
        Arrays.stream(classes).forEach(cls -> {
            try {
                ClasspathResourceLoader loader = new ClasspathResourceLoader();
                Configuration cfg = Configuration.defaultConfiguration();
                GroupTemplate template = new GroupTemplate(loader, cfg);
                FileType[] types = {FileType.CONTROLLER, FileType.REPOSITORY};
                for (FileType type : types) {
                    Template controller = template.getTemplate(Utils.getTemplateName(type));
                    Map<String, Object> dataMap = Utils.analyze(cls);
                    controller.binding(dataMap);
                    File file = Utils.initOutputFile(type,
                            dataMap.get("package").toString(),
                            dataMap.get("name").toString());
                    System.out.println(file);
                    FileOutputStream fos = new FileOutputStream(file);
                    controller.renderTo(fos);
                    fos.close();
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }
    static void initPojo (Map<?, ?> dataMap) {
        try {
            ClasspathResourceLoader loader = new ClasspathResourceLoader();
            Configuration cfg = Configuration.defaultConfiguration();
            GroupTemplate template = new GroupTemplate(loader, cfg);
            Template controller = template.getTemplate("pojo.template");
            controller.binding(dataMap);
            File file = Utils.initOutputFile(FileType.POJO,
                    dataMap.get("package").toString(),
                    dataMap.get("name").toString());
            System.out.println(file);
            FileOutputStream fos = new FileOutputStream(file);
            controller.renderTo(fos);
            fos.close();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
