package com.invitae.conductor.reports

import com.netflix.conductor.client.config.DefaultConductorClientConfiguration
import com.netflix.conductor.client.http.WorkflowClient
import com.netflix.conductor.common.run.SearchResult
import com.netflix.conductor.common.run.Workflow
import com.netflix.conductor.common.run.WorkflowSummary
import com.sun.jersey.api.client.config.DefaultClientConfig
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.net.URL
import javax.annotation.PostConstruct

const val RESTARTABLE_WORKFLOWS_ORDER = "endTime:ASC"
const val DEFAULT_LIMIT = 256

/**
 * A wrapper of the actual Conductor client library, which add a function to collect workflows & tasks execution details
 * given a range of dates.
 *
 * @author marco.crasso@invitae.com
 */
@Component
class ConductorProxy {

    var workflowApi: WorkflowClient = WorkflowClient()

    @Value("\${conductor.client.api-url}")
    lateinit var conductorApiUrl: String

    @PostConstruct
    fun init() {
        this.conductorApiUrl = "${this.conductorApiUrl.removeSuffix("/")}/"
        val url = URL(conductorApiUrl)
        if (url.userInfo != null) {
            val u = url.userInfo.split(":")[0]
            val p = url.userInfo.substring(URL(conductorApiUrl).userInfo.indexOf(':') + 1)
            workflowApi = WorkflowClient(
                DefaultClientConfig(),
                DefaultConductorClientConfiguration(),
                null,
                HTTPBasicAuthFilter(u, p)
            )
        }
        workflowApi.setRootURI(this.conductorApiUrl)
    }

    /**
     * Obtain non-running workflows between a rage of dates
     *
     * @param workflowName the name of the workflow
     * @param workflowVersion the version of the workflow
     * @param startTime the time that workflows start
     * @param endTime the time that workflows end
     */
    fun getWorkflowsBetween(
        workflowName: String,
        workflowVersion: Int,
        startTime: Long,
        endTime: Long
    ): Array<Workflow> = getFinishedWorkflows(workflowName, workflowVersion, "startTime>$startTime AND endTime<$endTime")

    /**
     * Obtain the workflow execution details along with its tasks data
     *
     * @param wfSummary summary of a workflow returned by a Conductor API search request
     */
    private fun getWorkflowExecutionDetails(wfSummary: WorkflowSummary): Workflow =
        workflowApi.getWorkflow(wfSummary.workflowId, true)

    /**
     * Retrieves a list of workflows that have finished and satisfy a given query. For example:
     * freeText=(workflowType:your_pipeline) AND (version:1) and query=startTime>1621518431077 AND endTime<1621518838410
     *
     * @param workflowName the workflow definition name
     * @param workflowVersion the workflow definition version
     * @param query a string query
     * @param limit a page size limit
     */
    private fun getFinishedWorkflows(
        workflowName: String,
        workflowVersion: Int,
        query: String = "",
        limit: Int = DEFAULT_LIMIT
    ): Array<Workflow> {
        // iteration over API with pagination support based on offset and limit, no cursor is provided by the API
        var wfInstances = SearchResult<WorkflowSummary>()
        var offset: Int = 0
        wfInstances.totalHits = Long.MAX_VALUE
        val foo = ArrayList<Workflow>()
        while (wfInstances.totalHits > offset) {
            wfInstances = workflowApi.search(
                offset,
                limit,
                RESTARTABLE_WORKFLOWS_ORDER,
                "(workflowType:$workflowName) AND (version:$workflowVersion)",
                query
            )
            wfInstances.results.parallelStream().forEach() { foo.add(getWorkflowExecutionDetails(it)) }
            offset += limit
        }
        return foo.toTypedArray()
    }
}
