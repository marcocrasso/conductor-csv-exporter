package com.invitae.conductor.reports

import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.netflix.conductor.common.run.Workflow

/**
 * Hard-coded list of workflows JSON attrs mapped onto CVS columns
 *
 * @author marco.crasso@invitae.com
 */
fun generateHeadersForWorkflows() = listOf(
    listOf(
        "workflowId",
        "correlationId",
        "startTime",
        "endTime",
        "(endTime - startTime)",
        "status",
        "reasonForIncomplete"
    )
)

/**
 * Hard-coded list of tasks JSON attrs mapped onto CVS columns
 */
fun generateHeaderForTasks() = listOf(
    listOf(
        "wf.workflowName",
        "wf.workflowId",
        "wf.correlationId",
        "wf.startTime",
        "wf.endTime",
        "(wf.endTime - wf.startTime)",
        "wf.status",
        "taskId",
        "taskType",
        "queueWaitTime",
        "scheduledTime",
        "startTime",
        "endTime",
        "(endTime - startTime)",
        "status",
        "reasonForIncomplete"
    )
)

/**
 * Create two new files with CSV headers
 */
fun csvHeaders(
    tasksTargetCsv: String,
    workflowsTargetCsv: String
) {
    csvWriter().writeAll(generateHeaderForTasks(), tasksTargetCsv, append = false)
    csvWriter().writeAll(generateHeadersForWorkflows(), workflowsTargetCsv, append = false)
}

/**
 * Append an array of workflows and tasks execution details into workflow and task CSV files respectively
 */
fun toCsv(
    tasksTargetCsv: String,
    workflowsTargetCsv: String,
    vararg wfs: Workflow
) {
    val workflows = mutableListOf<List<String>>()
    val tasks = mutableListOf<List<String>>()
    wfs.forEach { wf ->
        wf.tasks.forEach {
            val row = listOf(
                wf.workflowDefinition.name,
                wf.workflowId,
                wf.correlationId,
                wf.startTime.toString(),
                wf.endTime.toString(),
                (wf.endTime - wf.startTime).toString(),
                wf.status.toString(),
                it.taskId,
                it.taskType,
                it.queueWaitTime.toString(),
                it.scheduledTime.toString(),
                it.startTime.toString(),
                it.endTime.toString(),
                (it.endTime - it.startTime).toString(),
                it.status.toString(),
                it.reasonForIncompletion
            )
            tasks.add(row)
        }
        workflows.add(
            listOf(
                wf.workflowId,
                wf.correlationId,
                wf.startTime.toString(),
                wf.endTime.toString(),
                (wf.endTime - wf.startTime).toString(),
                wf.status.toString(),
                wf.reasonForIncompletion
            )
        )
    }
    csvWriter().writeAll(tasks, tasksTargetCsv, append = true)
    csvWriter().writeAll(workflows, workflowsTargetCsv, append = true)
}
