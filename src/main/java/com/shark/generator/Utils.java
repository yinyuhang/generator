package com.shark.generator;


import org.beetl.core.Configuration;
import org.beetl.core.GroupTemplate;
import org.beetl.core.Template;
import org.beetl.core.resource.ClasspathResourceLoader;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

interface Utils {
    static Map<String, Object> analyze(Class<?> cls) {
        Map<String, Object> entity = new HashMap<>();
        Field[] fields = cls.getDeclaredFields();
        entity.put("name", cls.getSimpleName());
        entity.put("lower_name", cls.getSimpleName().toLowerCase());
        String name = cls.getPackage().getName();
        name = name.substring(0, name.lastIndexOf('.'));
        entity.put("package", name);
        List<Object> conditions = new ArrayList<>();
        List<String> fieldNames = new ArrayList<>();
        Arrays.stream(fields).forEach(field -> {
            ChineseName chinaName = field.getAnnotation(ChineseName.class);
            Condition condition = field.getAnnotation(Condition.class);
            if (condition != null) {
                HashMap<String, String> map = new HashMap<>(2);
                map.put("type", field.getType().getName());
                String fieldName = field.getName();
                map.put("name", fieldName);
                String expression = String.format("null != %s", fieldName);
                switch (condition.expression()) {
                    case NOT_EMPTY:
                        expression += String.format(" && !%s.isEmpty()", fieldName);
                    case NOT_NULL:
                    default:
                        break;
                }
                if (chinaName != null) {
                    map.put("chinaName", chinaName.value());
                }
                map.put("expression", expression);
                String logic = condition.logic();
                map.put("logic", logic.isEmpty() ? null : logic);
                conditions.add(map);
            }
            if (chinaName != null) {
                fieldNames.add(chinaName.value());
            }
        });
        entity.put("conditions", conditions);
        entity.put("fields", fieldNames);
        return entity;
    }

    static File initOutputFile(FileType type, String basePackage, String entityName) throws URISyntaxException, IOException {
        URI uri = Thread.currentThread().getContextClassLoader().getResource(".").toURI();
        File path = new File(Paths.get(uri).toString()).getParentFile();
        basePackage = basePackage.replace('.', '/');
        path = new File(path, basePackage);
        File file;
        switch (type) {
            case CONTROLLER:
                path = new File(path, "controller");
                file = new File(path, entityName + "Controller.java");
                break;
            case REPOSITORY:
                path = new File(path, "repository");
                file = new File(path, entityName + "Repository.java");
                break;
            case POJO:
                path = new File(path, "pojo");
                file = new File(path, entityName + ".java");
                break;
            case HTML:
                path = new File(path, "html");
                file = new File(path, entityName.toLowerCase() + ".html");
                break;
            default:
                return null;
        }
        if (!file.exists()) {
            Files.createDirectories(path.toPath());
            file = Files.createFile(file.toPath()).toFile();
        }
        return file;
    }

    static String getTemplate(FileType type) {
        switch (type) {
            case CONTROLLER:
                return "controller.template";
            case REPOSITORY:
                return "repository.template";
            case POJO:
                return "pojo.template";
            case HTML:
                return "html.template";
            default:
                return null;
        }
    }

    static void generate(String templateFile, Map<String, Object> dataMap, OutputStream stream) throws IOException {
        ClasspathResourceLoader loader = new ClasspathResourceLoader();
        Configuration cfg = Configuration.defaultConfiguration();
        GroupTemplate template = new GroupTemplate(loader, cfg);
        Template t = template.getTemplate(templateFile);
        t.binding(dataMap);
        t.renderTo(stream);
    }
}
