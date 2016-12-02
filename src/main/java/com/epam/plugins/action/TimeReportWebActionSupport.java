package com.epam.plugins.action;

import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.epam.plugins.manager.StatusReportManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

public class TimeReportWebActionSupport extends DefaultSupport {

    private static final String PROJECT_PARAM_NAME = "enabledInProject";
    private static final String FIELDS_PARAM_NAME = "fields";

    public TimeReportWebActionSupport(final StatusReportManager statusReportManager,
                                      final CustomFieldManager customFieldManager) {
        super(statusReportManager, customFieldManager);
    }

    @Override
    protected String doExecute() throws Exception {
        if (!hasPermission()) {
            return PERMISSION_VIOLATION;
        }

        String projectName = null;
        String[] issueFields = getHttpRequest().getParameterValues(FIELDS_PARAM_NAME);

        List<String> strings = issueFields != null ? Arrays.asList(issueFields) : new ArrayList<>();

        Enumeration<String> parameterNames = getHttpRequest().getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String parameterName = parameterNames.nextElement();
            if (parameterName.equals(PROJECT_PARAM_NAME)) {
                projectName = getHttpRequest().getParameter(parameterName);
            }
        }

        statusReportManager.updateTemplateBuilderSettings(projectName, strings);

        return SUCCESS;
    }

    public String getProjectName() {
        return statusReportManager.getProjectName();
    }

    public List<CustomField> getCustomFields(){
        return customFieldManager.getCustomFieldObjects();
    }
}
