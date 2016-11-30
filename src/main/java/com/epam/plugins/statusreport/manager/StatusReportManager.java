package com.epam.plugins.statusreport.manager;

import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class StatusReportManager {

    private final static String PROJECT_NAME_KEY = "com.example.plugins.tutorial.crud.projectName";
    private final static String ISSUE_FIELDS_KEY = "com.example.plugins.tutorial.crud.fields";
    private PluginSettingsFactory pluginSettingsFactory;

    @Autowired
    public StatusReportManager(@ComponentImport final PluginSettingsFactory pluginSettingsFactory) {
        this.pluginSettingsFactory = pluginSettingsFactory;
    }


    private PluginSettings getPluginsSettings() {
        return pluginSettingsFactory.createGlobalSettings();
    }

    public void updateTemplateBuilderSettings(final String projectName, final List<String> issueFields) {
        getPluginsSettings().remove(PROJECT_NAME_KEY);
        getPluginsSettings().put(PROJECT_NAME_KEY, projectName);

        getPluginsSettings().remove(ISSUE_FIELDS_KEY);
        getPluginsSettings().put(ISSUE_FIELDS_KEY, issueFields);
    }

    public String getProjectName() {
        PluginSettings pluginSettings = getPluginsSettings();
        Object obj = pluginSettings.get(PROJECT_NAME_KEY);
        if (obj != null) {
            return obj.toString();
        }
        return "";
    }

    public List<String> getIssueFields() {
        PluginSettings pluginSettings = getPluginsSettings();
        //noinspection unchecked
        List<String> strings = (List<String>) pluginSettings.get(ISSUE_FIELDS_KEY);

        return strings == null ? new ArrayList<>() : strings;
    }
}
