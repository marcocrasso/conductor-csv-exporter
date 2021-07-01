package com.invitae.conductor.reports

import com.netflix.conductor.common.run.SearchResult
import com.netflix.conductor.common.run.Workflow
import com.netflix.conductor.common.run.WorkflowSummary

val TEST_SEARCH_RESULT_MODEL: () -> SearchResult<WorkflowSummary> = {
    val searchResult = SearchResult<WorkflowSummary>()
    searchResult.totalHits = 2
    var wf0 = Workflow()
    wf0.workflowId = "123-completed"
    wf0.status = Workflow.WorkflowStatus.COMPLETED
    wf0.correlationId = "687"
    wf0.input = mapOf("k" to "v")
    searchResult.results = mutableListOf()
    searchResult.results.add(WorkflowSummary(wf0))
    wf0 = Workflow()
    wf0.workflowId = "124-failed"
    wf0.status = Workflow.WorkflowStatus.FAILED
    wf0.correlationId = "679"
    wf0.input = mapOf("k" to "v")
    searchResult.results.add(WorkflowSummary(wf0))
    searchResult
}
