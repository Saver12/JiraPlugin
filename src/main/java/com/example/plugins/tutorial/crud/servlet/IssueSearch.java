package com.example.plugins.tutorial.crud.servlet;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.templaterenderer.TemplateRenderer;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class IssueSearch extends HttpServlet{
    @ComponentImport
    private IssueService issueService;

    @ComponentImport
    private ProjectService projectService;

    @ComponentImport
    private SearchService searchService;

    @ComponentImport
    private JiraAuthenticationContext jiraAuthenticationContext;

    @ComponentImport
    private TemplateRenderer templateRenderer;

    @ComponentImport
    private ChangeHistoryManager changeHistoryManager;

    private static final String LIST_BROWSER_TEMPLATE = "/templates/list.vm";
    private static final String NEW_BROWSER_TEMPLATE = "/templates/new.vm";
    private static final String EDIT_BROWSER_TEMPLATE = "/templates/edit.vm";
    private static final String SEARCHRESULTS_BROWSER_TEMPLATE = "/templates/searchresults.vm";

    @Inject
    public IssueSearch (IssueService issueService, ProjectService projectService,
                     SearchService searchService,
                     JiraAuthenticationContext jiraAuthenticationContext,
                     TemplateRenderer templateRenderer,
                     ChangeHistoryManager changeHistoryManager) {
        this.issueService = issueService;
        this.projectService = projectService;
        this.searchService = searchService;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.templateRenderer = templateRenderer;
        this.changeHistoryManager = changeHistoryManager;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ApplicationUser user = getCurrentUser();

        JqlClauseBuilder jqlClauseBuilder = JqlQueryBuilder.newClauseBuilder();

        com.atlassian.query.Query query = jqlClauseBuilder.issue(req.getParameter("query")).buildQuery();

        PagerFilter pagerFilter = PagerFilter.getUnlimitedFilter();
        com.atlassian.jira.issue.search.SearchResults searchResults = null;
        try {
            // Perform search results
            searchResults = searchService.search(user, query, pagerFilter);
        } catch (SearchException e) {
            e.printStackTrace();
        }
        Map<String, Object> context = new HashMap<>();
        context.put("searchresults", searchResults.getIssues());
        System.out.println("EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE");
        resp.setContentType("text/html;charset=utf-8");
        templateRenderer.render(SEARCHRESULTS_BROWSER_TEMPLATE, context, resp.getWriter());

    }

    private ApplicationUser getCurrentUser() {
        return jiraAuthenticationContext.getLoggedInUser();
    }
}
