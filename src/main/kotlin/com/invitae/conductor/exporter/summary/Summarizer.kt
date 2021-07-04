package com.invitae.conductor.exporter.summary

import com.invitae.conductor.exporter.ExportService
import com.netflix.conductor.common.run.Workflow

/**
 * Exports a summary of the collected workflow instances. This class is intended to serve as an example for those who
 * want to use custom exporters. To enable it:
 *
 * 1. edit resources/META-INF/services/com.invitae.conductor.exporter.ExportService with line:
 * com.invitae.conductor.exporter.summary.Summarizer
 *
 *
 * @author marco.crasso@invitae.com
 */
class Summarizer : ExportService {

    override fun export(vararg wfs: Workflow) {
        val wf = wfs.first()
        val name = wf.workflowName
        val version = wf.workflowVersion
        val total = wfs.size
        val firstCreated = wfs.minOf { it -> it.createTime }
        val lastEnded = wfs.maxOf { it -> it.endTime }
        val byStatus = wfs.groupBy { it.status }

        println("Collected '$total' workflows of type '$name' version '$version'.")
        println("Workflows by status:")
        byStatus.keys.forEach {
            println("\t $it: ${byStatus[it]?.size}")
        }
        println("Total roundtrip '${lastEnded - firstCreated}'.")
    }
}
