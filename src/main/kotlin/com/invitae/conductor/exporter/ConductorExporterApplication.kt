package com.invitae.conductor.exporter

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.net.URL

/**
 * Conductor Exporter it expects one mandatory argument, startTime [epoch].
 * To use it with the default CSV exporter, optionally, it accepts:
 * endTime=[epoch] default is now.
 * append=[true|false] , which can be used to append|override existing files. Default is false.
 * workflowsFile="string", which can be used to set target files. Default workflows.csv.
 * tasksFile="string", which can be used to set target files. Default tasks.csv.
 *
 * Run with:
 * ` ./gradlew bootRun --args="--startTime=1621518731077 --endTime=1621518838410 --append=false"`
 *
 * @author marco.crasso@invitae.com
 */
@SpringBootApplication
class ConductorExporterApplication : ApplicationRunner {

    @Value("\${conductor.workflow-name}")
    lateinit var workflowName: String

    @Value("\${conductor.workflow-version}")
    var workflowVersion: Int = 0

    @Autowired
    lateinit var conductor: ConductorProxy

    @Value("\${startTime}")
    var startTime: Long = 0

    @Value("\${endTime:#{T(System).currentTimeMillis()}}")
    var endTime: Long = 0

    @Autowired
    lateinit var exporter: ExportService

    override fun run(args: ApplicationArguments?) {
        println("Starting to export Conductor Details for '$workflowName'/'$workflowVersion' at '${URL(conductor.conductorApiUrl).host}' range: $startTime - $endTime")
        exporter.export(*obtainWorkflowsDetails())
    }

    fun obtainWorkflowsDetails(
        workflowName: String = this.workflowName,
        workflowVersion: Int = this.workflowVersion,
        startTime: Long = this.startTime,
        endTime: Long = this.endTime
    ) = conductor.getWorkflowsBetween(workflowName, workflowVersion, startTime = startTime, endTime = endTime)
}

fun main(args: Array<String>) {
    runApplication<ConductorExporterApplication>(*args)
}
