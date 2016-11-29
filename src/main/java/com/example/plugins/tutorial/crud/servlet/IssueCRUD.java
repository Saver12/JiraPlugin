package com.example.plugins.tutorial.crud.servlet;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.example.plugins.tutorial.crud.manager.StatusReportManager;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Scanned
public class IssueCRUD extends HttpServlet {

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

    @Autowired
    private StatusReportManager statusReportManager;

    private static final String LIST_BROWSER_TEMPLATE = "/templates/list.vm";
    private static final String NEW_BROWSER_TEMPLATE = "/templates/new.vm";
    private static final String EDIT_BROWSER_TEMPLATE = "/templates/edit.vm";

    @Inject
    public IssueCRUD(IssueService issueService, ProjectService projectService,
                     SearchService searchService,
                     JiraAuthenticationContext jiraAuthenticationContext,
                     TemplateRenderer templateRenderer,
                     StatusReportManager statusReportManager) {
        this.issueService = issueService;
        this.projectService = projectService;
        this.searchService = searchService;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.templateRenderer = templateRenderer;
        this.statusReportManager = statusReportManager;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("y".equals(req.getParameter("new"))) {
            // Renders new.vm template if the "new" parameter is passed

            // Create an empty context map to pass into the render method
            Map<String, Object> context = new HashMap<>();
            // Make sure to set the contentType otherwise bad things happen
            resp.setContentType("text/html;charset=utf-8");
            // Render the velocity template (new.vm). Since the new.vm template
            // doesn't need to render any in dynamic content, we just pass it an empty context
            templateRenderer.render(NEW_BROWSER_TEMPLATE, context, resp.getWriter());
        } else if ("y".equals(req.getParameter("edit"))) {
            // Renders edit.vm template if the "edit" parameter is passed

            // Retrieve issue with the specified key
            IssueService.IssueResult issue = issueService.getIssue(getCurrentUser(),
                    req.getParameter("key"));
            Map<String, Object> context = new HashMap<>();
            context.put("issue", issue.getIssue());
            resp.setContentType("text/html;charset=utf-8");
            // Render the template with the issue inside the context
            templateRenderer.render(EDIT_BROWSER_TEMPLATE, context, resp.getWriter());
        } else {
            // Render the list of issues (list.vm) if no params are passed in
            List<Issue> issues = getIssues();
            Map<String, Object> context = new HashMap<>();
            System.out.println("Project Name - " + statusReportManager.getProjectName());
            context.put("issues", issues);
            resp.setContentType("text/html;charset=utf-8");
            // Pass in the list of issues as the context
            templateRenderer.render(LIST_BROWSER_TEMPLATE, context, resp.getWriter());
        }
    }

    //Returns ApplicationUser instead of com.atlassian.crowd.embedded.api.User
    private ApplicationUser getCurrentUser() {
        return jiraAuthenticationContext.getLoggedInUser();
    }

    private List<Issue> getIssues() {
        // User is required to carry out a search
        ApplicationUser user = getCurrentUser();

        // search issues

        // The search interface requires JQL clause... so let's build one
        JqlClauseBuilder jqlClauseBuilder = JqlQueryBuilder.newClauseBuilder();
        // Our JQL clause is simple project="TUTORIAL"
        com.atlassian.query.Query query = jqlClauseBuilder.project("TUTORIAL").buildQuery();
        // A page filter is used to provide pagination. Let's use an unlimited filter to
        // to bypass pagination.
        PagerFilter pagerFilter = PagerFilter.getUnlimitedFilter();
        com.atlassian.jira.issue.search.SearchResults searchResults = null;
        try {
            // Perform search results
            searchResults = searchService.search(user, query, pagerFilter);
        } catch (SearchException e) {
            e.printStackTrace();
        }
        // return the results
        return searchResults.getIssues();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        ApplicationUser user = getCurrentUser();

        if ("y".equals(req.getParameter("edit"))) {
            // Perform update if the "edit" param is passed in
            // First get the issue from the key that's passed in
            IssueService.IssueResult issueResult = issueService.getIssue(user, req.getParameter("key"));
            MutableIssue issue = issueResult.getIssue();
            // Next we need to validate the updated issue
            IssueInputParameters issueInputParameters = issueService.newIssueInputParameters();
            issueInputParameters.setSummary(req.getParameter("summary"));
            issueInputParameters.setDescription(req.getParameter("description"));
            IssueService.UpdateValidationResult result = issueService.validateUpdate(user, issue.getId(),
                    issueInputParameters);

            if (result.getErrorCollection().hasAnyErrors()) {
                // If the validation fails, we re-render the edit page with the errors in the context
                Map<String, Object> context = new HashMap<>();
                context.put("issue", issue);
                context.put("errors", result.getErrorCollection().getErrors());
                resp.setContentType("text/html;charset=utf-8");
                templateRenderer.render(EDIT_BROWSER_TEMPLATE, context, resp.getWriter());
            } else {
                // If the validation passes, we perform the update then redirect the user back to the
                // page with the list of issues
                issueService.update(user, result);
                resp.sendRedirect("issuecrud");
            }

        } else {
            // Perform creation if the "new" param is passed in
            // First we need to validate the new issue being created
            IssueInputParameters issueInputParameters = issueService.newIssueInputParameters();
            // We're only going to set the summary and description. The rest are hard-coded to
            // simplify this tutorial.
            issueInputParameters.setSummary(req.getParameter("summary"));
            issueInputParameters.setDescription(req.getParameter("description"));
            // We need to set the assignee, reporter, project, and issueType...
            // For assignee and reporter, we'll just use the currentUser
            issueInputParameters.setAssigneeId(user.getName());
            issueInputParameters.setReporterId(user.getName());
            // We hard-code the project name to be the project with the TUTORIAL key
            Project project = projectService.getProjectByKey(user, "TUTORIAL").getProject();
            issueInputParameters.setProjectId(project.getId());
            // We also hard-code the issueType to be a "bug" == 1
            issueInputParameters.setIssueTypeId("10000");
            // Perform the validation
            IssueService.CreateValidationResult result = issueService.validateCreate(user, issueInputParameters);

            if (result.getErrorCollection().hasAnyErrors()) {
                // If the validation fails, render the list of issues with the error in a flash message
                List<Issue> issues = getIssues();
                Map<String, Object> context = new HashMap<>();
                context.put("issues", issues);
                context.put("errors", result.getErrorCollection().getErrors());
                resp.setContentType("text/html;charset=utf-8");
                templateRenderer.render(LIST_BROWSER_TEMPLATE, context, resp.getWriter());
            } else {
                // If the validation passes, redirect the user to the main issue list page
                issueService.create(user, result);
                resp.sendRedirect("issuecrud");
            }
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ApplicationUser user = getCurrentUser();
        // This will be the output string that we will put the JSON in
        String respStr;
        // Retrieve the issue with the specified key
        IssueService.IssueResult issue = issueService.getIssue(user, req.getParameter("key"));
        if (issue.isValid()) {
            // If the issue is found, let's delete it...
            // ... but first, we must validate that user can delete issue
            IssueService.DeleteValidationResult result = issueService.validateDelete(user, issue.getIssue().getId());
            if (result.getErrorCollection().hasAnyErrors()) {
                // If the validation fails, we send the error back to the user in a JSON payload
                respStr = "{ \"success\": \"false\", error: \"" + result.getErrorCollection().getErrors().get(0) + "\" }";
            } else {
                // If the validation passes, we perform the delete, then return a success msg back to the user
                issueService.delete(user, result);
                respStr = "{ \"success\" : \"true\" }";
            }
        } else {
            // The issue can't be found... so we send an error to the user
            respStr = "{ \"success\" : \"false\", error: \"Couldn't find issue\"}";
        }
        // We set the content-type to application/json here so that the AJAX client knows how to deal with it
        resp.setContentType("application/json;charset=utf-8");
        // Send the raw output string we put together
        resp.getWriter().write(respStr);
    }
}
