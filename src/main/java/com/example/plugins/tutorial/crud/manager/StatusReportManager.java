package com.example.plugins.tutorial.crud.manager;

import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.thoughtworks.xstream.XStream;
//import epam.jira.pptx.generator.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.TreeMap;

@Component
public class StatusReportManager {
    private final static String PLUGIN_SETTINGS_KEY = "tutorial.crud:plugin.settings";
    private final static String DAYS_KEY = "days";
    private final static String PROJECT_NAME_KEY = "projectName";

    private PluginSettingsFactory pluginSettingsFactory;
//    private final XStream xStream;

    @Autowired
    public StatusReportManager(@ComponentImport final PluginSettingsFactory pluginSettingsFactory) {
        this.pluginSettingsFactory = pluginSettingsFactory;

//        xStream = new XStream();
//        xStream.setClassLoader(StatusReportManager.class.getClassLoader());
    }


    private PluginSettings getPluginsSettings() {
        return pluginSettingsFactory.createSettingsForKey(PLUGIN_SETTINGS_KEY);
    }

    public void updateTemplateBuilderSettings(final Integer days, final String projectName) {
        getPluginsSettings().remove(DAYS_KEY);

        getPluginsSettings().put(DAYS_KEY, days != null ? days.toString() : null);
        System.out.println(getPluginsSettings().get(DAYS_KEY) + " - days");

        getPluginsSettings().remove(PROJECT_NAME_KEY);

        getPluginsSettings().put(PROJECT_NAME_KEY, projectName);
        System.out.println(getPluginsSettings().get(PROJECT_NAME_KEY) + " - name");
    }

    public Integer getDays() {
        PluginSettings pluginSettings = getPluginsSettings();
        Object obj = pluginSettings.get(DAYS_KEY);
        if (obj != null) {
            return Integer.valueOf(obj.toString());
        }
        return null;
    }

    public String getProjectName() {
        PluginSettings pluginSettings = getPluginsSettings();
        Object obj = pluginSettings.get(PROJECT_NAME_KEY);
        if (obj != null) {
            return obj.toString();
        }
        return null;
    }
}
