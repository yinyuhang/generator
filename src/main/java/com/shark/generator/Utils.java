package com.shark.generator;


import java.io.File;
import java.io.IOException;
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
        Arrays.stream(fields).forEach(field -> {
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
                map.put("expression", expression);
                String logic = condition.logic();
                map.put("logic", logic.isEmpty() ? null: logic);
                conditions.add(map);
            }
        });
        entity.put("conditions", conditions);
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
            default:
                return null;
        }
        if (!file.exists()) {
            Files.createDirectories(path.toPath());
            file = Files.createFile(file.toPath()).toFile();
        }
        return file;
    }

    static String getTemplateName(FileType type) {
        switch (type) {
            case CONTROLLER:
                return "controller.template";
            case REPOSITORY:
                return "repository.template";
            default:
                return null;
        }
    }
}
