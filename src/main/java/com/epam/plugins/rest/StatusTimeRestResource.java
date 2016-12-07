package com.epam.plugins.rest;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.epam.plugins.manager.StatusReportManager;
import com.google.common.collect.Sets;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.*;

@Path("/")
@Produces({MediaType.APPLICATION_JSON})
@Scanned
public class StatusTimeRestResource {

    @ComponentImport
    private WorkflowManager workflowManager;

    @ComponentImport
    private JiraAuthenticationContext jiraAuthenticationContext;

    @ComponentImport
    private SearchService searchService;

    private StatusReportManager statusReportManager;

    @ComponentImport
    private CustomFieldManager customFieldManager;

    @ComponentImport
    private ChangeHistoryManager changeHistoryManager;

    @Inject
    public StatusTimeRestResource(final WorkflowManager workflowManager,
                                  final JiraAuthenticationContext jiraAuthenticationContext,
                                  final SearchService searchService,
                                  final StatusReportManager statusReportManager,
                                  final CustomFieldManager customFieldManager,
                                  final ChangeHistoryManager changeHistoryManager) {
        this.workflowManager = workflowManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.searchService = searchService;
        this.statusReportManager = statusReportManager;
        this.customFieldManager = customFieldManager;
        this.changeHistoryManager = changeHistoryManager;
    }

    @GET
    @Path("/getStatuses")
    public Response getStatuses() {
        List<Status> statusObjects = workflowManager.getWorkflow(10000L, "10000").getLinkedStatusObjects();
        List<String> list = new ArrayList<>();
        for (Status statusObject : statusObjects) {
            list.add(statusObject.getName());
        }
        return Response.ok(new StatusList(list)).build();
    }

    @GET
    @Path("/getResults")
    public Response getResults() {

        List<Issue> issues = getIssues();

        List<ValuesMap> jsons = new ArrayList<>();

        for (Issue issue : issues) {

            List<ChangeItemBean> statuses = changeHistoryManager.getChangeItemsForField(issue, "status");

            Map<String, Object> values = new HashMap<>();

            fillStatuses(values, statuses, issue);

            List<String> issueFields = statusReportManager.getIssueFields();

            fillValuesMap(values, issueFields, issue);

            jsons.add(new ValuesMap(issue.getKey(), values));
        }
        return Response.ok(jsons).build();
    }

    @XmlRootElement
    public static class StatusList {

        @XmlElement
        Collection<String> listOfStatuses;

        @SuppressWarnings({"UnusedDeclaration", "unused"})
        private StatusList() {
        }

        StatusList(final Collection<String> listOfStatuses) {
            this.listOfStatuses = listOfStatuses;
        }

        public Collection<String> getListOfStatuses() {
            return listOfStatuses;
        }
    }

    @XmlRootElement
    public static class ValuesMap {

        @XmlElement
        String issueKey;

        @XmlElement
        Map<String, Object> values;

        @SuppressWarnings({"UnusedDeclaration", "unused"})
        private ValuesMap() {
        }

        ValuesMap(String issueKey, Map<String, Object> values) {
            this.issueKey = issueKey;
            this.values = values;
        }
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

    private void fillValuesMap(Map<String, Object> values, List<String> issueFields, Issue issue) {

        HashSet<String> currentFields = Sets.newHashSet(issueFields);

        if (currentFields.contains("Assignee")) {
            ApplicationUser assignee = issue.getAssignee();
            values.put("Assignee", assignee == null ? null : assignee.toString());
        }
        if (currentFields.contains("Description")) {
            values.put("Description", issue.getDescription());
        }
        if (currentFields.contains("Reporter")) {
            values.put("Reporter", issue.getReporter().getName());
        }
        if (currentFields.contains("Summary")) {
            values.put("Summary", issue.getSummary());
        }

        List<CustomField> customFields = customFieldManager.getCustomFieldObjects();

        for (CustomField customField : customFields) {
            Object value = customField.getValue(issue);
            values.put(customField.getFieldName(), value == null ? null : value.toString());
        }
    }

    private void fillStatuses(Map<String, Object> values, List<ChangeItemBean> statuses, Issue issue) {

        List<Status> statusObjects = workflowManager.getWorkflow(issue).getLinkedStatusObjects();

        String firstStatus = statusObjects.get(0).getName();

        values.put(firstStatus, new Date(issue.getCreated().getTime()).toString());

        for (int i = 1; i < statusObjects.size(); i++) {
            String currentStatusname = statusObjects.get(i).getName();
            values.put(currentStatusname, null);
        }

        for (ChangeItemBean status : statuses) {
            values.put(status.getToString(), new Date(status.getCreated().getTime()).toString());
        }
    }
}
