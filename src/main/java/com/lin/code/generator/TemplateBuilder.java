package com.lin.code.generator;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wenxuelin on 2017/1/8.
 */
public class TemplateBuilder {
    private Map<String, Template> templates = new HashMap<String, Template>();
    private final String[] templateFiles = new String[] {
            "vo", "idao", "dao", "iservice", "service", "controller"};

    private final String extension = ".ftl";

    private Configuration configuration;

    public TemplateBuilder() throws IOException {
        configuration = new Configuration();
        configuration.setObjectWrapper(new DefaultObjectWrapper());
        configuration.setClassForTemplateLoading(this.getClass(), "template");
        configuration.setDefaultEncoding("utf-8");

        for(String templateFile : templateFiles) {
            Template template = configuration.getTemplate(templateFile + extension);
            templates.put(templateFile, template);
        }
    }

    public void build(String templateFile, Map data, File file) throws IOException, TemplateException {
        Template template = templates.get(templateFile);
        if(template != null) {
            FileOutputStream fileOutputStream = null;
            OutputStreamWriter outputStreamWriter = null;
            try {
                fileOutputStream = FileUtils.openOutputStream(file);
                outputStreamWriter = new OutputStreamWriter(fileOutputStream, "utf-8");
                template.process(data, outputStreamWriter);
            } finally {
                try {
                    outputStreamWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
