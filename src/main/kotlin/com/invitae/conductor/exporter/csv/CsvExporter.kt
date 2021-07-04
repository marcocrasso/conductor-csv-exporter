package com.invitae.conductor.exporter.csv

import com.invitae.conductor.exporter.ExportService
import com.netflix.conductor.common.run.Workflow
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * Exports Conductor model into CSV files. This was the first implemented exporter for the conductor-exporter project,
 * and thus it is the default one.
 *
 * @author marco.crasso@invitae.com
 */
@Component
class CsvExporter : ExportService {

    @Value("\${append}")
    var append: Boolean = false

    @Value("\${tasksFile:tasks.csv}")
    var tasksFile: String = "tasks.csv"

    @Value("\${workflowsFile:workflows.csv}")
    var workflowsFile: String = "workflows.csv"

    override fun export(vararg wfs: Workflow) {
        val s = System.currentTimeMillis()
        if (!append)
            csvHeaders(tasksFile, workflowsFile)
        toCsv(
            tasksFile,
            workflowsFile,
            *wfs
        )
        println("Files '$tasksFile' and '$workflowsFile' were successfully generated in ${System.currentTimeMillis() - s} [ms]")
    }
}
