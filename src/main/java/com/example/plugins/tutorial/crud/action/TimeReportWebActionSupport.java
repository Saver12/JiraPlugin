package com.example.plugins.tutorial.crud.action;

import com.example.plugins.tutorial.crud.manager.StatusReportManager;

import java.util.Enumeration;

public class TimeReportWebActionSupport extends DefaultSupport {

    private static final String DAYS_PARAM_NAME = "days";
    private static final String PROJRCT_PARAM_NAME = "enabledInProject";

    public TimeReportWebActionSupport(final StatusReportManager statusReportManager) {
        super(statusReportManager);
    }

    @Override
    protected String doExecute() throws Exception {
        if (!hasPermission()) {
            return PERMISSION_VIOLATION;
        }

        Integer days = null;
        String projectName = null;

        Enumeration<String> parameterNames = getHttpRequest().getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String parameterName = parameterNames.nextElement();
            if (parameterName.equals(DAYS_PARAM_NAME)) {
                try {
                    days = Integer.valueOf(getHttpRequest().getParameter(parameterName));
                    System.out.println(days);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            } else if (parameterName.equals(PROJRCT_PARAM_NAME)) {
                projectName = getHttpRequest().getParameter(parameterName);
                System.out.println(projectName);
            }
        }

        statusReportManager.updateTemplateBuilderSettings(days, projectName);

        return SUCCESS;
    }
}
