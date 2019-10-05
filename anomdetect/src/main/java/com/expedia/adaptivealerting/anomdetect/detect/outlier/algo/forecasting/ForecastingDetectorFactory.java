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
package com.expedia.adaptivealerting.anomdetect.detect.outlier.algo.forecasting;

import com.expedia.adaptivealerting.anomdetect.detect.outlier.OutlierType;
import com.expedia.adaptivealerting.anomdetect.forecast.interval.IntervalForecaster;
import com.expedia.adaptivealerting.anomdetect.forecast.point.PointForecaster;
import com.expedia.adaptivealerting.anomdetect.detect.DetectorDocument;
import com.expedia.adaptivealerting.anomdetect.detect.DetectorFactory;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.UUID;

/**
 * Factory to create a forecasting detector from a detector document.
 */
@RequiredArgsConstructor
public class ForecastingDetectorFactory implements DetectorFactory<ForecastingDetector> {

    @NonNull
    private DetectorDocument document;

    @Override
    public ForecastingDetector buildDetector() {
        val uuid = document.getUuid();
        val config = document.getConfig();

        val pointForecasterUuid = UUID.fromString((String) config.get("pointForecaster"));
        val pointForecaster = loadPointForecaster(pointForecasterUuid);

        val intervalForecasterUuid = UUID.fromString((String) config.get("intervalForecaster"));
        val intervalForecaster = loadIntervalForecaster(intervalForecasterUuid);

        val type = OutlierType.valueOf((String) config.get("type"));

        return new ForecastingDetector(uuid, pointForecaster, intervalForecaster, type);
    }

    private PointForecaster loadPointForecaster(UUID uuid) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private IntervalForecaster loadIntervalForecaster(UUID uuid) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
