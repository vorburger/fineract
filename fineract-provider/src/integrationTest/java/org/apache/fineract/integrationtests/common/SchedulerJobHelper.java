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
package org.apache.fineract.integrationtests.common;

import static java.time.Instant.now;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import com.google.common.util.concurrent.Uninterruptibles;
import com.google.gson.Gson;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.util.Pair;

public class SchedulerJobHelper {

    private final RequestSpecification requestSpec;
    private final ResponseSpecification response200Spec;
    private final ResponseSpecification response202Spec;

    public SchedulerJobHelper(final RequestSpecification requestSpec) {
        this.requestSpec = requestSpec;
        this.response200Spec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.response202Spec = new ResponseSpecBuilder().expectStatusCode(202).build();
    }

    public SchedulerJobHelper(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.response200Spec = responseSpec;
        this.response202Spec = responseSpec;
    }

    private List<Map<String, Object>> getAllSchedulerJobs() {
        final String GET_ALL_SCHEDULER_JOBS_URL = "/fineract-provider/api/v1/jobs?" + Utils.TENANT_IDENTIFIER;
        System.out.println("------------------------ RETRIEVING ALL SCHEDULER JOBS -------------------------");
        List<Map<String, Object>> response = Utils.performServerGet(requestSpec, response200Spec, GET_ALL_SCHEDULER_JOBS_URL, "");
        assertNotNull(response);
        return response;
    }

    private <T> List<T> getAllSchedulerJobDetails(Function<Map<String, Object>, T> mapper) {
        return getAllSchedulerJobs().stream().map(mapper).collect(Collectors.toList());
    }

    public List<Integer> getAllSchedulerJobIds() {
        return getAllSchedulerJobDetails(map -> (Integer) map.get("jobId"));
    }

    public List<String> getAllSchedulerJobNames() {
        return getAllSchedulerJobDetails(map -> (String) map.get("displayName"));
    }

    public Map<String, Object> getSchedulerJobById(int jobId) {
        final String GET_SCHEDULER_JOB_BY_ID_URL = "/fineract-provider/api/v1/jobs/" + jobId + "?" + Utils.TENANT_IDENTIFIER;
        final Map<String, Object> response = Utils.performServerGet(requestSpec, response200Spec, GET_SCHEDULER_JOB_BY_ID_URL, "");
        assertNotNull(response);
        return response;
    }

    public Boolean getSchedulerStatus() {
        final String GET_SCHEDULER_STATUS_URL = "/fineract-provider/api/v1/scheduler?" + Utils.TENANT_IDENTIFIER;
        System.out.println("------------------------ RETRIEVING SCHEDULER STATUS -------------------------");
        final Map<String, Object> response = Utils.performServerGet(requestSpec, response200Spec, GET_SCHEDULER_STATUS_URL, "");
        return (Boolean) response.get("active");
    }

    public void updateSchedulerStatus(final String command) {
        final String UPDATE_SCHEDULER_STATUS_URL = "/fineract-provider/api/v1/scheduler?command=" + command + "&" + Utils.TENANT_IDENTIFIER;
        System.out.println("------------------------ UPDATING SCHEDULER STATUS -------------------------");
        Utils.performServerPost(requestSpec, response202Spec, UPDATE_SCHEDULER_STATUS_URL, runSchedulerJobAsJSON(), null);
    }

    public Map<String, Object> updateSchedulerJob(int jobId, final String active) {
        final String UPDATE_SCHEDULER_JOB_URL = "/fineract-provider/api/v1/jobs/" + jobId + "?" + Utils.TENANT_IDENTIFIER;
        System.out.println("------------------------ UPDATING SCHEDULER JOB -------------------------");
        final Map<String, Object> response = Utils.performServerPut(requestSpec, response200Spec, UPDATE_SCHEDULER_JOB_URL,
                updateSchedulerJobAsJSON(active), "changes");
        return response;
    }

    private static String updateSchedulerJobAsJSON(final String active) {
        final Map<String, String> map = new HashMap<>();
        map.put("active", active);
        System.out.println("map : " + map);
        return new Gson().toJson(map);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getSchedulerJobHistory(int jobId) {
        final String GET_SCHEDULER_STATUS_URL = "/fineract-provider/api/v1/jobs/" + jobId + "/runhistory?" + Utils.TENANT_IDENTIFIER;
        System.out.println("------------------------ RETRIEVING SCHEDULER JOB HISTORY -------------------------");
        final Map<String, Object> response = Utils.performServerGet(requestSpec, response200Spec, GET_SCHEDULER_STATUS_URL, "");
        return (List<Map<String, Object>>) response.get("pageItems");
    }

    private void runSchedulerJob(int jobId) {
        final ResponseSpecification responseSpec = new ResponseSpecBuilder().expectStatusCode(202).build();
        final String RUN_SCHEDULER_JOB_URL = "/fineract-provider/api/v1/jobs/" + jobId + "?command=executeJob&" + Utils.TENANT_IDENTIFIER;
        System.out.println("------------------------ RUN SCHEDULER JOB -------------------------");
        Utils.performServerPost(requestSpec, responseSpec, RUN_SCHEDULER_JOB_URL, runSchedulerJobAsJSON(), null);
    }

    private static String runSchedulerJobAsJSON() {
        final Map<String, String> map = new HashMap<>();
        String runSchedulerJob = new Gson().toJson(map);
        System.out.println(runSchedulerJob);
        return runSchedulerJob;
    }

    private int getSchedulerJobIdByName(String jobName) {
        List<Map<String, Object>> allSchedulerJobsData = getAllSchedulerJobs();
        for (Integer jobIndex = 0; jobIndex < allSchedulerJobsData.size(); jobIndex++) {
            if (allSchedulerJobsData.get(jobIndex).get("displayName").equals(jobName)) {
                return (Integer) allSchedulerJobsData.get(jobIndex).get("jobId");
            }
        }
        throw new IllegalArgumentException("No such named Job (see org.apache.fineract.infrastructure.jobs.service.JobName enum):" + jobName);
    }

    @Deprecated // FINERACT-922 TODO Gradually replace use of this method with new executeAndAwaitJob() below, if it proves to be more stable than this one
    public void executeJob(String jobName) throws InterruptedException {
        // Stop the Scheduler while we manually trigger execution of job, to avoid side effects and simplify debugging when readings logs
        updateSchedulerStatus("stop");

        int jobId = getSchedulerJobIdByName(jobName);

        // Executing Scheduler Job
        runSchedulerJob(jobId);

        // Retrieving Scheduler Job by ID
        Map<String, Object> schedulerJob = getSchedulerJobById(jobId);

        // Waiting for Job to complete
        while ((Boolean) schedulerJob.get("currentlyRunning") == true) {
            Thread.sleep(15000);
            schedulerJob = getSchedulerJobById(jobId);
            assertNotNull(schedulerJob);
            System.out.println("Job is Still Running");
        }

        List<Map<String, Object>> jobHistoryData = getSchedulerJobHistory(jobId);

        assertFalse("Job History is empty :(  Was it too slow? Failures in background job?", jobHistoryData.isEmpty());

        // print error associated with recent job failure (if any)
        System.out.println("Job run error message (printed only if the job fails: "
                + jobHistoryData.get(jobHistoryData.size() - 1).get("jobRunErrorMessage"));
        System.out.println("Job failure error log (printed only if the job fails: "
                + jobHistoryData.get(jobHistoryData.size() - 1).get("jobRunErrorLog"));

        // Verifying the Status of the Recently executed Scheduler Job
        assertEquals("Verifying Last Scheduler Job Status", "success",
                jobHistoryData.get(jobHistoryData.size() - 1).get("status"));
    }

    /**
     * Launches a Job and awaits its completion.
     * @param jobName displayName (see {@link org.apache.fineract.infrastructure.jobs.service.JobName}) of Scheduler Job
     *
     * @author Michael Vorburger.ch
     */
    public void executeAndAwaitJob(String jobName) {
        Duration TIMEOUT = Duration.ofSeconds(30);
        Duration PAUSE = Duration.ofMillis(500);
        DateTimeFormatter df = DateTimeFormatter.ISO_INSTANT; // FINERACT-926
        Instant beforeExecuteTime = now().truncatedTo(ChronoUnit.SECONDS);

        // Stop the Scheduler while we manually trigger execution of job, to avoid side effects and simplify debugging when readings logs
        updateSchedulerStatus("stop");

        // Executing Scheduler Job
        int jobId = getSchedulerJobIdByName(jobName);
        runSchedulerJob(jobId);

        // Await JobDetailData.lastRunHistory [JobDetailHistoryData] jobRunStartTime >= beforeExecuteTime (or timeout)
        await(TIMEOUT, PAUSE, jobLastRunHistoryChecker(jobId, lastRunHistory -> {
            String jobRunStartText = lastRunHistory.get("jobRunStartTime");
            if (jobRunStartText == null) {
                return false;
            }
            Instant jobRunStartTime = df.parse(jobRunStartText, Instant::from);
            return jobRunStartTime.equals(jobRunStartTime) || jobRunStartTime.isAfter(beforeExecuteTime);
        }));

        // Await JobDetailData.lastRunHistory [JobDetailHistoryData] jobRunEndTime to be both set and >= jobRunStartTime (or timeout)
        Map<String, String> finalLastRunHistory = await(TIMEOUT, PAUSE, jobLastRunHistoryChecker(jobId, lastRunHistory -> {
            String jobRunEndText = lastRunHistory.get("jobRunEndTime");
            if (jobRunEndText == null) {
                return false;
            }
            Instant jobRunEndTime = df.parse(jobRunEndText, Instant::from);
            Instant jobRunStartTime = df.parse(lastRunHistory.get("jobRunStartTime"), Instant::from);
            return jobRunEndTime.equals(jobRunStartTime) || jobRunEndTime.isAfter(jobRunStartTime);
        }));

        // Verify triggerType
        assertThat(finalLastRunHistory.get("triggerType"), is("application"));

        // Verify status & propagate jobRunErrorMessage and/or jobRunErrorLog (if any)
        String status = finalLastRunHistory.get("status");
        if (!status.equals("success")) {
            fail(finalLastRunHistory.toString());
        }

        // PS: Checking getSchedulerJobHistory() [/runhistory] is pointless, because the lastRunHistory JobDetailHistoryData is already part of JobDetailData anyway.
    }

    private AwaiterSupplier<Map<String, String>> jobLastRunHistoryChecker(int jobId, CheckedFunction<Map<String, String>, Boolean> lastRunHistoryChecker) {
        return () -> {
            Map<String, Object> job = getSchedulerJobById(jobId);
            if (job == null) {
                return Pair.of(false, null);
            }
            @SuppressWarnings("unchecked")
            Map<String, String> lastRunHistory = (Map<String, String>) job.get("lastRunHistory");
            if (lastRunHistory == null) {
                return Pair.of(false, null);
            }
            return Pair.of(lastRunHistoryChecker.apply(lastRunHistory), lastRunHistory);
        };
    }

    private @FunctionalInterface interface AwaiterSupplier<T> {
        // TODO use Hamcrest matcher instead of Boolean supplier.. that would allow us to cause very nice error messages!
        Pair<Boolean, T> get() throws Exception;

        default Exception getTimeoutException() {
            return new IllegalStateException("Timed out!");
        }
    }

    private @FunctionalInterface interface CheckedFunction<T, R> {
        R apply(T t) throws Exception;
    }

    /**
     * Utility to await until 'checker' returns true, waiting 'pause' between each attempt, maximum until 'timeout'.
     */
    private <T> T await(Duration timeout, Duration pause, AwaiterSupplier<T> checker) {
        Instant timeoutTime = now().plus(timeout);
        try {
            do {
                Pair<Boolean, T> pair = checker.get();
                if (pair.getFirst()) { // succeeded
                    return pair.getSecond();
                }
                // not quite there yet, pause, and then loop to retry, unless time's up...
                Uninterruptibles.sleepUninterruptibly(pause);
            } while (now().isBefore(timeoutTime));

            // Hm, we timed out, let's give up:
            throw checker.getTimeoutException();

        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new IllegalStateException(e);
        }
    }
}