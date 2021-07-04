package com.invitae.conductor.exporter

import com.netflix.conductor.client.config.DefaultConductorClientConfiguration
import com.netflix.conductor.client.http.TaskClient
import com.netflix.conductor.client.http.WorkflowClient
import com.netflix.conductor.common.metadata.tasks.Task
import com.netflix.conductor.common.metadata.tasks.TaskDef
import com.netflix.conductor.common.metadata.tasks.TaskExecLog
import com.netflix.conductor.common.metadata.workflow.WorkflowTask
import com.netflix.conductor.common.run.SearchResult
import com.netflix.conductor.common.run.Workflow
import com.netflix.conductor.common.run.WorkflowSummary
import com.sun.jersey.api.client.config.DefaultClientConfig
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.net.URL
import java.util.Optional
import javax.annotation.PostConstruct
import kotlin.collections.ArrayList

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
    var tasksApi: TaskClient = TaskClient()

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
            tasksApi = TaskClient(
                DefaultClientConfig(),
                DefaultConductorClientConfiguration(),
                null,
                HTTPBasicAuthFilter(u, p)
            )
        }
        workflowApi.setRootURI(this.conductorApiUrl)
        tasksApi.setRootURI(this.conductorApiUrl)
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
    private fun getWorkflowExecutionDetails(wfSummary: WorkflowSummary): Workflow {
        val wf = workflowApi.getWorkflow(wfSummary.workflowId, true)
        val loggedTasks = mutableListOf<Task>()
        wf.tasks.forEach {
            loggedTasks.add(LoggedTask(it, getTaskExecLogs(it.taskId)))
        }
        wf.tasks = loggedTasks
        return wf
    }

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

    /**
     * Retrieves a list of logs belogning to a task
     *
     * @param taskId the unique id of the task
     */
    fun getTaskExecLogs(taskId: String) = tasksApi.getTaskLogs(taskId)
}

class LoggedTask(task: Task, taskExecLogs: MutableList<TaskExecLog>) : Task() {

    var task: Task = task
    var logs: List<TaskExecLog> = taskExecLogs

    override fun getTaskType(): String {
        return task.taskType
    }

    override fun getStatus(): Status {
        return task.status
    }

    override fun getTaskStatus(): Status {
        return task.taskStatus
    }

    override fun getInputData(): MutableMap<String, Any> {
        return task.inputData
    }

    override fun getReferenceTaskName(): String {
        return task.referenceTaskName
    }

    override fun getCorrelationId(): String {
        return task.correlationId
    }

    override fun getRetryCount(): Int {
        return task.retryCount
    }

    override fun getScheduledTime(): Long {
        return task.scheduledTime
    }

    override fun getStartTime(): Long {
        return task.startTime
    }

    override fun getEndTime(): Long {
        return task.endTime
    }

    override fun getStartDelayInSeconds(): Int {
        return task.startDelayInSeconds
    }

    override fun getRetriedTaskId(): String {
        return task.retriedTaskId
    }

    override fun getSeq(): Int {
        return task.seq
    }

    override fun getUpdateTime(): Long {
        return task.updateTime
    }

    override fun getQueueWaitTime(): Long {
        return task.queueWaitTime
    }

    override fun getPollCount(): Int {
        return task.pollCount
    }

    override fun getTaskDefName(): String {
        return task.taskDefName
    }

    override fun getResponseTimeoutSeconds(): Long {
        return task.responseTimeoutSeconds
    }

    override fun getWorkflowInstanceId(): String {
        return task.workflowInstanceId
    }

    override fun getWorkflowType(): String {
        return task.workflowType
    }

    override fun getTaskId(): String {
        return task.taskId
    }

    override fun getReasonForIncompletion(): String? {
        return task.reasonForIncompletion
    }

    override fun getWorkerId(): String {
        return task.workerId
    }

    override fun getOutputData(): MutableMap<String, Any> {
        return task.outputData
    }

    override fun getWorkflowTask(): WorkflowTask {
        return task.workflowTask
    }

    override fun getDomain(): String {
        return task.domain
    }

    override fun getInputMessage(): com.google.protobuf.Any {
        return task.inputMessage
    }

    override fun getOutputMessage(): com.google.protobuf.Any {
        return task.outputMessage
    }

    override fun getTaskDefinition(): Optional<TaskDef> {
        return task.taskDefinition
    }

    override fun getRateLimitPerFrequency(): Int {
        return task.rateLimitPerFrequency
    }

    override fun getRateLimitFrequencyInSeconds(): Int {
        return task.rateLimitFrequencyInSeconds
    }

    override fun getExternalInputPayloadStoragePath(): String {
        return task.externalInputPayloadStoragePath
    }

    override fun getExternalOutputPayloadStoragePath(): String {
        return task.externalOutputPayloadStoragePath
    }

    override fun getIsolationGroupId(): String {
        return task.isolationGroupId
    }

    override fun getExecutionNameSpace(): String {
        return task.executionNameSpace
    }

    override fun getIteration(): Int {
        return task.iteration
    }

    override fun getWorkflowPriority(): Int {
        return task.workflowPriority
    }

    override fun getSubWorkflowId(): String {
        return task.subWorkflowId
    }
}
