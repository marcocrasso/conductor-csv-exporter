package com.invitae.conductor.exporter

import com.invitae.conductor.exporter.csv.CsvExporter
import com.netflix.conductor.common.run.Workflow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.ServiceLoader

/**
 * Main configuration that indicates which exporters would be used. To enable external exporters, you could use SPI:
 *
 * 1. external exporters should implement com.invitae.conductor.exporter.ExportService Interface
 * 2. external exporters should have in META-INF a folder named services with a text file named like the interface, and
 * one text line for each provider implementation
 * 3. add required packages into project dependencies
 *
 * If the above steps have been followed the exporter method will find the SPI providers and return those instead of
 * the default one.
 *
 * @author marco.crasso@invitae.com
 */
@Configuration
class Configuration {

    @Autowired
    lateinit var defaultCsvExporter: CsvExporter

    /**
     * Returns an ExportService implementing the Composite Pattern, with all SPI exporters that may be found, or the
     * default one (@see CsvExporter)
     */
    @Bean
    fun exporter(): ExportService {
        val srvs = object : ExportService {
            val children = mutableListOf<ExportService>()
            override fun export(vararg wfs: Workflow) {
                children.stream().forEach { it.export(*wfs) }
            }
        }
        val loader = ServiceLoader.load(ExportService::class.java)
        val it: Iterator<ExportService> = loader.iterator()
        while (it.hasNext()) {
            val candidate: ExportService = it.next()
            srvs.children.add(candidate)
        }
        if (srvs.children.size == 0) {
            srvs.children.add(defaultCsvExporter)
        }
        return srvs
    }
}
