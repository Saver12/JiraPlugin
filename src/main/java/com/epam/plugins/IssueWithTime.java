package com.epam.plugins;

import com.atlassian.jira.issue.Issue;
import org.apache.commons.lang.time.DurationFormatUtils;

public class IssueWithTime {

    private Issue issue;
    private long timeInPreviousStatus;
    private long timeInCurrentStatus;


    public IssueWithTime(Issue issue,
                         long timeInPreviousStatus,
                         long timeInCurrentStatus) {
        this.issue = issue;
        this.timeInPreviousStatus = timeInPreviousStatus;
        this.timeInCurrentStatus = timeInCurrentStatus;
    }

    public Issue getIssue() {
        return issue;
    }

    public String getTimeInPreviousStatus(String format) {
        if (timeInPreviousStatus == -1) {
            return "-";
        } else {
            return DurationFormatUtils.formatDuration(timeInPreviousStatus, format);
        }
    }

    public String getTimeInCurrentStatus(String format) {
            return DurationFormatUtils.formatDuration(timeInCurrentStatus, format);
    }
}
