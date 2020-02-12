/*
 * Copyright 2018-2019 Expedia Group, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.expedia.adaptivealerting.metrics.functions.source.graphite;

import com.expedia.adaptivealerting.metrics.functions.source.MetricFunctionsSpec;
import com.typesafe.config.Config;
import lombok.val;

import static com.expedia.adaptivealerting.anomdetect.util.ThreadUtil.sleep;

public class ConstructSourceURI {
    public static final int MAX_INTERVAL = 60;
    private final String GRAPHITE_URI_KEY = "urlTemplate";

    public String getGraphiteURI(Config metricSourceSinkConfig, MetricFunctionsSpec metricFunctionsSpec) {
        String graphiteUri = metricSourceSinkConfig.getString(GRAPHITE_URI_KEY);
        val intervalInSecs = metricFunctionsSpec.getIntervalInSecs();
        val currentGraphiteEpochSeconds = System.currentTimeMillis() / 1000;
        val startOfCurrentBin = (currentGraphiteEpochSeconds / intervalInSecs) * intervalInSecs;
        val startOfPreviousBin = startOfCurrentBin - intervalInSecs;
        sleepUntilPreviousBinIsFull(intervalInSecs, currentGraphiteEpochSeconds, startOfCurrentBin);
        return String.format("%s%s&from=%d&until=%d",
                graphiteUri,
                metricFunctionsSpec.getFunction(),
                startOfPreviousBin,
                startOfCurrentBin-1);
    }

    /**
     * We need to give Graphite time to receive all of the metrics for the last bin (where bin equals passage of time intervalInSecs long).
     * We assume that Graphite is using round numbers for bin intervals.
     * We wait until enough time has passed since last bin.  Time is considered enough when at least one minute or half of intervalInSecs
     * (whichever is smaller) has passed since start of current bin.
     * @param intervalInSecs
     * @param currentGraphiteEpochSeconds
     * @param startOfCurrentBin
     */
    private void sleepUntilPreviousBinIsFull(int intervalInSecs, long currentGraphiteEpochSeconds, long startOfCurrentBin) {
        val secondsSinceCurrentBinStarted = currentGraphiteEpochSeconds - startOfCurrentBin;
        val halfIntervalInSecs = intervalInSecs / 2;
        val secondsUntilPreviousBinIsFull = Math.min(60, halfIntervalInSecs - secondsSinceCurrentBinStarted);
        boolean tooSoonSinceLastBin = secondsSinceCurrentBinStarted < secondsUntilPreviousBinIsFull;
        if (tooSoonSinceLastBin) {
            sleep(secondsUntilPreviousBinIsFull * 1000);
        }
    }

}
