/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.integrationtests;

import static org.junit.Assert.assertEquals;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import java.util.Map;
import org.apache.fineract.integrationtests.common.SchedulerJobHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.Before;
import org.junit.Test;

public class SchedulerJobsTest {

    private RequestSpecification requestSpec;
    private SchedulerJobHelper schedulerJobHelper;

    @Before
    public void setup() {
        Utils.initializeRESTAssured();
        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        requestSpec.header("Fineract-Platform-TenantId", "default");
        schedulerJobHelper = new SchedulerJobHelper(requestSpec);
    }

    @Test
    public void testFlippingSchedulerStatus() throws InterruptedException {
        // Retrieving Status of Scheduler
        Boolean schedulerStatus = schedulerJobHelper.getSchedulerStatus();
        if (schedulerStatus == true) {
            schedulerJobHelper.updateSchedulerStatus("stop");
            schedulerStatus = schedulerJobHelper.getSchedulerStatus();
            // Verifying Status of the Scheduler after stopping
            assertEquals("Verifying Scheduler Job Status", false, schedulerStatus);
        } else {
            schedulerJobHelper.updateSchedulerStatus("start");
            schedulerStatus = schedulerJobHelper.getSchedulerStatus();
            // Verifying Status of the Scheduler after starting
            assertEquals("Verifying Scheduler Job Status", true, schedulerStatus);
        }
    }

    @Test
    public void testFlippingJobsActiveStatus() throws InterruptedException {
        // Stop the Scheduler while we test flapping jobs' active on/off, to avoid side effects
        schedulerJobHelper.updateSchedulerStatus("stop");

        // For each retrieved scheduled job (by ID)...
        for (Integer jobId : schedulerJobHelper.getAllSchedulerJobIds()) {
            // Retrieving Scheduler Job by ID
            Map<String, Object> schedulerJob = schedulerJobHelper.getSchedulerJobById(jobId);

            Boolean active = (Boolean) schedulerJob.get("active");
            active = !active;

            // Updating Scheduler Job
            Map<String, Object> changes = schedulerJobHelper.updateSchedulerJob(jobId, active.toString());

            // Verifying Scheduler Job updates
            assertEquals("Verifying Scheduler Job Updates", active, changes.get("active"));

            schedulerJob = schedulerJobHelper.getSchedulerJobById(jobId);
            assertEquals("Verifying Get Scheduler Job", active, schedulerJob.get("active"));
        }
    }

    @Test
    public void testTriggeringManualExecutionOfAllSchedulerJobs() {
        for (String jobName : schedulerJobHelper.getAllSchedulerJobNames()) {
            schedulerJobHelper.executeAndAwaitJob(jobName);
        }
    }
}