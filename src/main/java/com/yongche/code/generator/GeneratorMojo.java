package com.yongche.code.generator;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wenxuelin on 2017/1/8.
 */

/**
 * @goal generate
 */
public class GeneratorMojo extends AbstractMojo {
    /**
     * @parameter expression="${project}"
     * @readonly
     */
    private MavenProject project;

    /**
     * @parameter
     * @required
     */
    private DBConfig dbConfig;

    /**
     * @parameter expression="${project.groupId}.${project.artifactId}"
     */
    private String basePackage;

    /**
     * @parameter
     * @required
     */
    private List<TableGroup> tableGroups;

    /**
     * @parameter
     * @required
     */
    private String[] includes;

    private Map<String, Boolean> includesMap = new HashMap<String, Boolean>();
    TemplateBuilder templateBuilder = null;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Log logger = getLog();

        logger.info("Web模块路径为：" + project.getBasedir().getPath() + File.separator + project.getArtifactId() + "_web");
        logger.info("数据库类型为：" + dbConfig.getDbType());

        logger.info("开始查询数据库表信息");
        Database database = new Database(dbConfig);
        List<DBTable> tables = database.getTableInfo(tableGroups);
        logger.info("完成查询数据库表信息，共查询到【" + tables.size() + "】张表");

        if (tables.size() == 0) {
            logger.info("未指定需要生成代码的数据库表，结束代码生成");
            return;
        }

        logger.info("开始加载模板");
        try {
            templateBuilder = new TemplateBuilder();
        } catch (IOException e) {
            e.printStackTrace();
            logger.info("模板加载时发生错误：" + e.getMessage());
            return;
        }
        logger.info("完成加载模板");

        for (String include : includes) {
            String[] scope = StringUtils.split(include, ",");
            boolean overwrite = false;
            if (scope.length > 1 && StringUtils.equalsIgnoreCase(scope[1], "overwrite")) {
                overwrite = true;
            }
            includesMap.put(StringUtils.lowerCase(scope[0]), overwrite);
        }
        logger.info("includesMap-test："+includesMap);

        basePackage = basePackage.replaceAll("-", "");
        logger.info("开始代码生成");
        logger.info("代码基础包为：" + basePackage);
        for (DBTable table : tables) {
            logger.info("【" + table.getName() + "】：代码生成开始");
            if (includesMap.containsKey("vo")) {
                logger.info("【" + table.getName() + "】：VO生成：" + buildVO(table));
            }
            if (includesMap.containsKey("idao")) {
                logger.info("【" + table.getName() + "】：IDAO生成：" + buildIDao(table));
            }
            if (includesMap.containsKey("dao")) {
                logger.info("【" + table.getName() + "】：DAO生成：" + buildDAO(table));
            }
            if (includesMap.containsKey("iservice")) {
                logger.info("【" + table.getName() + "】：IService生成：" + buildIService(table));
            }
            if (includesMap.containsKey("service")) {
                logger.info("【" + table.getName() + "】：Service生成：" + buildService(table));
            }
            if (includesMap.containsKey("controller")) {
                logger.info("【" + table.getName() + "】：Controller生成：" + buildController(table));
            }
            logger.info("【" + table.getName() + "】：代码生成结束");
        }
        logger.info("完成代码生成");
    }


    private String buildVO(DBTable table) {
        String result = "成功";

        Map data = new HashMap();
        data.put("package", basePackage);
        data.put("name", formatName(table.getName()));

        StringBuilder filePath = new StringBuilder();
        filePath.append(project.getBasedir().getPath()).append(File.separator);
        filePath.append("src").append(File.separator).append("main").append(File.separator);
        filePath.append("java").append(File.separator);
        filePath.append(StringUtils.replace(basePackage, ".", File.separator)).append(File.separator);
        filePath.append("entity").append(File.separator).append(data.get("name")).append(".java");

        File file = new File(filePath.toString());
        if (!includesMap.get("vo") && file.exists()) {
            result = "文件已存在";
            return result;
        }

        if (StringUtils.isNotBlank(table.getComment())) {
            StringBuilder comment = new StringBuilder();
            comment.append("/**\r\n").append(" * ");
            comment.append(StringUtils.replace(table.getComment(), "\r\n", "\r\n * "));
            comment.append("\r\n */");
            data.put("comment", comment.toString());
        }

        List<Map> fields = new ArrayList<Map>();
        for (DBColumn column : table.getColumns()) {
            Map columnInfo = new HashMap();
            columnInfo.put("name", StringUtils.uncapitalize(formatName(column.getName())));
            if (StringUtils.equalsIgnoreCase(column.getType(), "BLOB") /**|| StringUtils.equalsIgnoreCase(column.getType(), "CLOB")  //CLOB直接使用String处理*/ || StringUtils.equalsIgnoreCase(column.getType(), "LONGBLOB")) {
                columnInfo.put("type", "byte[]");
            } else {
                columnInfo.put("type", "String");
            }
            if (StringUtils.isNotBlank(column.getComment())) {
                columnInfo.put("comment", "//" + StringUtils.replace(column.getComment(), "\r\n", " "));
            }
            fields.add(columnInfo);
        }
        data.put("fields", fields);

        try {
            templateBuilder.build("vo", data, file);
        } catch (Exception e) {
            e.printStackTrace();
            result = "失败";
        }

        return result;
    }


    private String buildIDao(DBTable table) {
        String result = "成功";

        Map data = new HashMap();
        data.put("package", basePackage);
        data.put("name", formatName(table.getName()));

        StringBuilder filePath = new StringBuilder();
        filePath.append(project.getBasedir().getPath()).append(File.separator);
        filePath.append("src").append(File.separator).append("main").append(File.separator);
        filePath.append("java").append(File.separator);
        filePath.append(StringUtils.replace(basePackage, ".", File.separator)).append(File.separator);
        filePath.append("dao").append(File.separator).append("I").append(data.get("name")).append("Dao.java");

        File file = new File(filePath.toString());
        if (!includesMap.get("idao") && file.exists()) {
            result = "文件已存在";
            return result;
        }

        try {
            templateBuilder.build("idao", data, file);
        } catch (Exception e) {
            e.printStackTrace();
            result = "失败";
        }

        return result;
    }


    private String buildDAO(DBTable table) {
        String result = "成功";

        Map data = new HashMap();
        data.put("package", basePackage);
        data.put("dbType", dbConfig.getDbType());
        data.put("name", formatName(table.getName()));
        data.put("tableName", table.getName());
        data.put("autoIncrement", formatName(table.getAutoIncrement()));

        StringBuilder filePath = new StringBuilder();
        filePath.append(project.getBasedir().getPath()).append(File.separator);
        filePath.append("src").append(File.separator).append("main").append(File.separator);
        filePath.append("java").append(File.separator);
        filePath.append(StringUtils.replace(basePackage, ".", File.separator)).append(File.separator);
        filePath.append("dao").append(File.separator).append("impl");
        filePath.append(File.separator).append(data.get("name")).append("Dao.java");

        File file = new File(filePath.toString());
        if (!includesMap.get("dao") && file.exists()) {
            result = "文件已存在";
            return result;
        }

        Map primaryKeyMap = new HashMap();
        List<Map> primaryKeyFields = null;
        if (table.getPrimaryKeys() != null) {
            data.put("dbPrimaryKeys", table.getPrimaryKeys());
            primaryKeyFields = new ArrayList<Map>();
            List<String> primaryKeys = new ArrayList<String>();
            for (String primaryKey : table.getPrimaryKeys()) {
                primaryKeys.add(StringUtils.uncapitalize(formatName(primaryKey)));
                primaryKeyMap.put(primaryKey, primaryKey);
            }
            data.put("primaryKeys", primaryKeys);
            if (table.getPrimaryKeys().size() == 1) {
                data.put("dbPrimaryKey", table.getPrimaryKeys().get(0));
                data.put("primaryKey", StringUtils.uncapitalize(formatName(table.getPrimaryKeys().get(0))));
            }
        }

        List<Map> fields = new ArrayList<Map>();
        for (DBColumn column : table.getColumns()) {
            Map columnInfo = new HashMap();
            columnInfo.put("name", StringUtils.uncapitalize(formatName(column.getName())));
            columnInfo.put("dbName", column.getName());
            columnInfo.put("type", column.getType());
            fields.add(columnInfo);
            if(primaryKeyMap.containsKey(column.getName())){
                primaryKeyFields.add(columnInfo);
            }
        }
        data.put("fields", fields);
        data.put("primaryKeyFields", primaryKeyFields);

        try {
            templateBuilder.build("dao", data, file);
        } catch (Exception e) {
            e.printStackTrace();
            result = "失败";
        }

        return result;
    }

    private String buildIService(DBTable table) {
        String result = "成功";

        Map data = new HashMap();
        data.put("package", basePackage);
        data.put("name", formatName(table.getName()));

        StringBuilder filePath = new StringBuilder();
        filePath.append(project.getBasedir().getPath()).append(File.separator);
        filePath.append("src").append(File.separator).append("main").append(File.separator);
        filePath.append("java").append(File.separator);
        filePath.append(StringUtils.replace(basePackage, ".", File.separator)).append(File.separator);
        filePath.append("service").append(File.separator).append("I").append(data.get("name")).append("Service.java");

        File file = new File(filePath.toString());
        if (!includesMap.get("iservice") && file.exists()) {
            result = "文件已存在";
            return result;
        }

        try {
            templateBuilder.build("iservice", data, file);
        } catch (Exception e) {
            e.printStackTrace();
            result = "失败";
        }

        return result;
    }

    private String buildService(DBTable table) {
        String result = "成功";

        Map data = new HashMap();
        data.put("package", basePackage);
        data.put("name", formatName(table.getName()));

        StringBuilder filePath = new StringBuilder();
        filePath.append(project.getBasedir().getPath()).append(File.separator);
        filePath.append("src").append(File.separator).append("main").append(File.separator);
        filePath.append("java").append(File.separator);
        filePath.append(StringUtils.replace(basePackage, ".", File.separator)).append(File.separator);
        filePath.append("service").append(File.separator).append("impl").append(File.separator);
        filePath.append(data.get("name")).append("Service.java");

        File file = new File(filePath.toString());
        if (!includesMap.get("service") && file.exists()) {
            result = "文件已存在";
            return result;
        }

        try {
            templateBuilder.build("service", data, file);
        } catch (Exception e) {
            e.printStackTrace();
            result = "失败";
        }

        return result;
    }


    private String buildController(DBTable table) {
        String result = "成功";

        Map data = new HashMap();
        data.put("package", basePackage);
        data.put("name", formatName(table.getName()));

        StringBuilder filePath = new StringBuilder();
        filePath.append(project.getBasedir().getPath()).append(File.separator);
        filePath.append("src").append(File.separator).append("main").append(File.separator);
        filePath.append("java").append(File.separator);
        filePath.append(StringUtils.replace(basePackage, ".", File.separator)).append(File.separator);
        filePath.append("controller").append(File.separator).append(data.get("name")).append("Controller.java");

        File file = new File(filePath.toString());
        if (!includesMap.get("controller") && file.exists()) {
            result = "文件已存在";
            return result;
        }

        data.put("group", table.getGroup());

        if (table.getPrimaryKeys() != null) {
            List<String> primaryKeys = new ArrayList<String>();
            for (String primaryKey : table.getPrimaryKeys()) {
                primaryKeys.add(StringUtils.uncapitalize(formatName(primaryKey)));
            }
            data.put("primaryKeys", primaryKeys);
        }

        try {
            templateBuilder.build("controller", data, file);
        } catch (Exception e) {
            e.printStackTrace();
            result = "失败";
        }

        return result;
    }


    private String formatName(String dbName) {
        String name = StringUtils.replace(dbName, "_", " ");
        name = StringUtils.lowerCase(name);
        name = WordUtils.capitalize(name);
        name = StringUtils.replace(name, " ", "");
        return name;
    }
}
