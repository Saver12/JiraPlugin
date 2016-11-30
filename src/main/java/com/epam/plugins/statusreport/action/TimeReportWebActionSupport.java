package com.epam.plugins.statusreport.action;

import com.epam.plugins.statusreport.manager.StatusReportManager;

import java.util.*;

public class TimeReportWebActionSupport extends DefaultSupport {

    private static final String PROJECT_PARAM_NAME = "enabledInProject";
    private static final String FIELDS_PARAM_NAME = "fields";

    public TimeReportWebActionSupport(final StatusReportManager statusReportManager) {
        super(statusReportManager);
    }

    @Override
    protected String doExecute() throws Exception {
        if (!hasPermission()) {
            return PERMISSION_VIOLATION;
        }

        String projectName = null;
        String[] issueFields = getHttpRequest().getParameterValues(FIELDS_PARAM_NAME);

        List<String> strings1 = issueFields != null ? Arrays.asList(issueFields) : new ArrayList<>();

        Enumeration<String> parameterNames = getHttpRequest().getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String parameterName = parameterNames.nextElement();
            if (parameterName.equals(PROJECT_PARAM_NAME)) {
                projectName = getHttpRequest().getParameter(parameterName);
                System.out.println(projectName);
            }
        }

        statusReportManager.updateTemplateBuilderSettings(projectName, strings1);

        return SUCCESS;
    }

    public String getProjectName() {
        return statusReportManager.getProjectName();
    }
}
