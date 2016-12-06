package com.epam.plugins.rest;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
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
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.epam.plugins.manager.StatusReportManager;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.sql.Timestamp;
import java.util.*;

@Path("/")
//@AnonymousAllowed
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
    public Response getStatuses(){
        /*List<StatusList> list = new ArrayList<>();
        list.add(new StatusList(Arrays.asList("One", "Two", "Three")));
        list.add(new StatusList(Arrays.asList("Four", "Five", "Six")));*/

        List<Status> statusObjects = workflowManager.getWorkflow(10000L, "10000").getLinkedStatusObjects();
        List<String> list = new ArrayList<>();
        for (Status statusObject : statusObjects) {
            list.add(statusObject.getName());

        }

        return Response.ok(new StatusList(list)).build();
    }

    @GET
    @Path("/getResults")
    public Response getResults(){

        List<Issue> issues = getIssues();

        for (Issue issue : issues) {

            List<ChangeItemBean> statuses = changeHistoryManager.getChangeItemsForField(issue, "status");

            Map<String, Timestamp> map = new HashMap<>();

            map.put(issue.getStatus().getName(), issue.getCreated());

            for (ChangeItemBean status : statuses) {
                map.put(status.getToString(), status.getCreated());
            }
        }



        return null;
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
    public static class ValuesMap{

        @XmlElement
        String issueKey;

        @XmlElement
        Map<String, Object> values;

        @SuppressWarnings({"UnusedDeclaration", "unused"})
        private ValuesMap() {
        }

        public ValuesMap(String issueKey, Map<String, Object> values) {
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
}
