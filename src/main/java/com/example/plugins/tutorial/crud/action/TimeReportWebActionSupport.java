package com.example.plugins.tutorial.crud.action;

import java.util.Enumeration;

public class TimeReportWebActionSupport extends DefaultSupport {

    @Override
    protected String doExecute() throws Exception {
        if (!hasPermission()) {
            return PERMISSION_VIOLATION;
        }

        Integer days = null;
        String projectName = null;

        /*Enumeration<String> parameterNames = getHttpRequest().getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String parameterName = parameterNames.nextElement();
            if (parameterName.equals(DAYS_PARAM_NAME)) {
                try {
                    days = Integer.valueOf(getHttpRequest().getParameter(parameterName));
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            } else if (parameterName.equals(PROJRCT_PARAM_NAME)) {
                projectName = getHttpRequest().getParameter(parameterName);
            }
        }

        pptxBuilderManager.updateTemplateBuilderSettings(days, projectName);*/

        return SUCCESS;
    }
}
