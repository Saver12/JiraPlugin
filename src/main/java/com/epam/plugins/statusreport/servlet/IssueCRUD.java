package com.epam.plugins.statusreport.servlet;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.epam.plugins.statusreport.IssueWithTime;
import com.epam.plugins.statusreport.manager.StatusReportManager;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@Scanned
public class IssueCRUD extends HttpServlet {

    @ComponentImport
    private SearchService searchService;

    @ComponentImport
    private JiraAuthenticationContext jiraAuthenticationContext;

    @ComponentImport
    private TemplateRenderer templateRenderer;

    @Autowired
    private StatusReportManager statusReportManager;

    @ComponentImport
    private ChangeHistoryManager changeHistoryManager;

    private static final String LIST_BROWSER_TEMPLATE = "/templates/list.vm";

    @Inject
    public IssueCRUD(SearchService searchService,
                     JiraAuthenticationContext jiraAuthenticationContext,
                     TemplateRenderer templateRenderer,
                     StatusReportManager statusReportManager,
                     ChangeHistoryManager changeHistoryManager) {
        this.searchService = searchService;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.templateRenderer = templateRenderer;
        this.changeHistoryManager = changeHistoryManager;
        this.statusReportManager = statusReportManager;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        List<Issue> issues = getIssues();
        List<IssueWithTime> issueWithTimes = new ArrayList<>();

        for (Issue issue : issues) {
            List<ChangeItemBean> statuses = changeHistoryManager.getChangeItemsForField(issue, "status");
            long startTime;
            long endTime;
            long timeInCurrentStatus;
            long timeInPreviousStatus;
            int size = statuses.size();
            if (statuses.isEmpty()) {
                timeInCurrentStatus = Calendar.getInstance().getTimeInMillis() - issue.getCreated().getTime();
                issueWithTimes.add(new IssueWithTime(issue, -1, timeInCurrentStatus));
            } else if (size == 1) {
                endTime = statuses.get(size - 1).getCreated().getTime();
                startTime = issue.getCreated().getTime();
                timeInPreviousStatus = endTime - startTime;
                timeInCurrentStatus = Calendar.getInstance().getTimeInMillis() - endTime;
                issueWithTimes.add(new IssueWithTime(issue, timeInPreviousStatus, timeInCurrentStatus));
            } else {
                startTime = statuses.get(size - 2).getCreated().getTime();
                endTime = statuses.get(size - 1).getCreated().getTime();
                timeInPreviousStatus = endTime - startTime;
                timeInCurrentStatus = Calendar.getInstance().getTimeInMillis() - endTime;

                issueWithTimes.add(new IssueWithTime(issue, timeInPreviousStatus, timeInCurrentStatus));
            }
        }

        Map<String, Object> context = new HashMap<>();
        System.out.println("Project Name - " + statusReportManager.getProjectName());

        context.put("issues", issues);

        List<String> issueFields = statusReportManager.getIssueFields();

        context.put("issueFields", Sets.newHashSet(issueFields));
        context.put("issues", issueWithTimes);
        resp.setContentType("text/html;charset=utf-8");

        templateRenderer.render(LIST_BROWSER_TEMPLATE, context, resp.getWriter());
    }

    private ApplicationUser getCurrentUser() {
        return jiraAuthenticationContext.getLoggedInUser();
    }

    private List<Issue> getIssues() {

        ApplicationUser user = getCurrentUser();
        JqlClauseBuilder jqlClauseBuilder = JqlQueryBuilder.newClauseBuilder();
        com.atlassian.query.Query query = jqlClauseBuilder.project(statusReportManager.getProjectName()).buildQuery();
        PagerFilter pagerFilter = PagerFilter.getUnlimitedFilter();
        com.atlassian.jira.issue.search.SearchResults searchResults = null;
        try {
            searchResults = searchService.search(user, query, pagerFilter);
        } catch (SearchException e) {
            e.printStackTrace();
        }
        return searchResults.getIssues();
    }
}
