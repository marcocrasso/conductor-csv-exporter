package com.invitae.conductor.exporter

import com.netflix.conductor.common.run.Workflow

/**
 * Export Service interface. Designed to allow different exporters working on a set of Workflows one time downloaded
 * from Conductor server.
 *
 * @author marco.crasso@invitae.com
 */
interface ExportService {

    fun export(vararg wfs: Workflow)
}
