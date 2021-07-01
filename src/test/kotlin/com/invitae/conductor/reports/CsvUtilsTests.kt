package com.invitae.conductor.reports

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.netflix.conductor.common.metadata.tasks.Task
import com.netflix.conductor.common.metadata.workflow.WorkflowDef
import com.netflix.conductor.common.run.Workflow
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.File

/**
 * Unit tests related to CSV headers and ordinary rows generation
 *
 * @author marco.crasso@invitae.com
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CsvUtilsTests {

    private val tmpTasksCsvFile: File = File.createTempFile("tasks", ".csv")
    private val tmpWorkflowsCsvFile: File = File.createTempFile("workflows", ".csv")

    init {
        tmpTasksCsvFile.deleteOnExit()
        tmpWorkflowsCsvFile.deleteOnExit()
    }

    @Test
    fun `CSV headers are well-formed`() {

        csvHeaders(
            tmpTasksCsvFile.absolutePath,
            tmpWorkflowsCsvFile.absolutePath
        )

        var rows: List<List<String>> = csvReader().readAll(tmpTasksCsvFile)

        Assertions.assertEquals(generateHeaderForTasks()[0].size, rows[0].size)

        rows = csvReader().readAll(tmpWorkflowsCsvFile)
        Assertions.assertEquals(generateHeadersForWorkflows()[0].size, rows[0].size)
    }

    @Test
    fun `generated CSVs, from 1 task workflow, have one more row`() {
        val wf = Workflow()
        wf.status = Workflow.WorkflowStatus.TERMINATED
        wf.createTime = 1622064529286
        wf.startTime = 1622064529286
        wf.updateTime = 1622064544979
        wf.endTime = 1622064544979
        wf.workflowId = "e338d607-38c3-4d47-9537-4f7a8c42af54"
        wf.workflowDefinition = WorkflowDef()
        wf.workflowDefinition.name = "sub_flow_1"

        val t = Task()
        t.taskId = "678"
        t.taskType = "task_5"
        t.queueWaitTime = 0
        t.scheduledTime = 1622064529309
        t.startTime = 0
        t.endTime = 1622064544999
        t.status = Task.Status.CANCELED
        wf.tasks = listOf(t)

        val wsfCsvPrevSize = csvReader().readAll(tmpWorkflowsCsvFile).size
        val tasksCsvPrevSize = csvReader().readAll(tmpTasksCsvFile).size

        toCsv(tmpTasksCsvFile.absolutePath, tmpWorkflowsCsvFile.absolutePath, wf)

        var rows: List<List<String>> = csvReader().readAll(tmpWorkflowsCsvFile)
        Assertions.assertEquals(wsfCsvPrevSize + 1, rows.size)

        rows = csvReader().readAll(tmpTasksCsvFile)
        Assertions.assertEquals(tasksCsvPrevSize + 1, rows.size)

        Assertions.assertEquals(rows[rows.size - 1][0], wf.workflowDefinition.name)
        Assertions.assertEquals(rows[rows.size - 1][7], t.taskId)
        Assertions.assertEquals(rows[rows.size - 1][8], t.taskType)
    }
}
