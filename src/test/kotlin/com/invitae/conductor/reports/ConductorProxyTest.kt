package com.invitae.conductor.reports

import com.netflix.conductor.client.http.WorkflowClient
import com.netflix.conductor.common.run.Workflow
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ConductorProxyTest {

    private val conductorProxy = ConductorProxy()
    private val mockedWorkflowApi = mockk<WorkflowClient>(relaxed = true)
    private val apiUrl = "http://foo:bar@test.com/api"

    @BeforeEach
    fun `setup mocks`() {
        conductorProxy.conductorApiUrl = apiUrl

        every {
            mockedWorkflowApi.search(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns TEST_SEARCH_RESULT_MODEL()

        every {
            mockedWorkflowApi.getWorkflow(
                any(),
                true
            )
        } returns Workflow()

        conductorProxy.init()
        conductorProxy.workflowApi = mockedWorkflowApi
    }

    @Test
    fun `test complete URL path during initialization`() {
        Assertions.assertEquals("$apiUrl/", conductorProxy.conductorApiUrl)
    }

    @Test
    fun `test search`() {
        Assertions.assertEquals(2, conductorProxy.getWorkflowsBetween("test", 1, 0, 0).size)
    }
}
