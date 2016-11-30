package com.example.plugins.tutorial.crud.action;

import com.example.plugins.tutorial.crud.manager.StatusReportManager;

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

//        Integer days = null;
        String projectName = null;
        String[] issueFields = getHttpRequest().getParameterValues(FIELDS_PARAM_NAME);

//        HashSet<String> strings = issueFields != null ? Sets.newHashSet(issueFields) : new HashSet<>();

        List<String> strings1 = issueFields != null ? Arrays.asList(issueFields) : new ArrayList<>();
        System.out.println(strings1);
//        if (issueFields)

        System.out.println(Arrays.toString(issueFields));
        Enumeration<String> parameterNames = getHttpRequest().getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String parameterName = parameterNames.nextElement();
            /*if (parameterName.equals(DAYS_PARAM_NAME)) {
                try {
                    days = Integer.valueOf(getHttpRequest().getParameter(parameterName));
                    System.out.println(days);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            } else*/ if (parameterName.equals(PROJECT_PARAM_NAME)) {
                projectName = getHttpRequest().getParameter(parameterName);
                System.out.println(projectName);
            }
        }

        statusReportManager.updateTemplateBuilderSettings(projectName, strings1);

        return SUCCESS;
    }
}
