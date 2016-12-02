package com.epam.plugins.action;

import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.permission.GlobalPermissionKey;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.epam.plugins.manager.StatusReportManager;

public abstract class DefaultSupport extends JiraWebActionSupport {

    protected static final String PERMISSION_VIOLATION = "permissionviolation";
    protected static final String SUCCESS = "success";
    protected static final String ERROR = "error";

    protected final StatusReportManager statusReportManager;
    protected final CustomFieldManager customFieldManager;

    public DefaultSupport(final StatusReportManager statusReportManager,
                          final CustomFieldManager customFieldManager) {
        this.statusReportManager = statusReportManager;
        this.customFieldManager = customFieldManager;
    }

    @Override
    public String doDefault() {
        if (!hasPermission()) {
            return PERMISSION_VIOLATION;
        } else {
            return SUCCESS;
        }
    }

    protected boolean hasPermission() {
        return hasGlobalPermission(GlobalPermissionKey.ADMINISTER);
    }
}
