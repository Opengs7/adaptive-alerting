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
package com.expedia.adaptivealerting.anomdetect.forecast.interval.algo.expwelford;

import com.expedia.adaptivealerting.anomdetect.forecast.ForecasterDocument;
import com.expedia.adaptivealerting.anomdetect.forecast.interval.IntervalForecasterFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class ExponentialWelfordIntervalForecasterFactory
        implements IntervalForecasterFactory<ExponentialWelfordIntervalForecaster> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @NonNull
    private ForecasterDocument document;

    @Override
    public ExponentialWelfordIntervalForecaster build() {
        val paramsMap = document.getConfig().get("params");
        val params = objectMapper.convertValue(paramsMap, ExponentialWelfordIntervalForecasterParams.class);
        return new ExponentialWelfordIntervalForecaster(document.getUuid(), params);
    }
}
