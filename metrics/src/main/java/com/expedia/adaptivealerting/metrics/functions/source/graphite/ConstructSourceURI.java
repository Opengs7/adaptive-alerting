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

public class ConstructSourceURI {
    private final String GRAPHITE_URI_KEY = "urlTemplate";

    public String getGraphiteURI(Config metricSourceSinkConfig, MetricFunctionsSpec metricFunctionsSpec) {
        String graphiteUri = metricSourceSinkConfig.getString(GRAPHITE_URI_KEY);
        val intervalInSecs = metricFunctionsSpec.getIntervalInSecs();
        val currentGraphiteEpochSeconds = System.currentTimeMillis() / 1000;
        val startOfCurrentInterval = (currentGraphiteEpochSeconds / intervalInSecs) * intervalInSecs;
        long from = startOfCurrentInterval - 1;
        long until = startOfCurrentInterval + intervalInSecs;
        return String.format("%s%s&from=%d&until=%d",
                graphiteUri,
                metricFunctionsSpec.getFunction(),
                from,
                until);
    }

}
