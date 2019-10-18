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
package com.expedia.adaptivealerting.anomdetect.detect.outlier.algo.constant;

import com.expedia.adaptivealerting.anomdetect.detect.outlier.OutlierType;
import com.expedia.adaptivealerting.anomdetect.detect.AbstractDetectorFactoryTest;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Slf4j
public class ConstantThresholdDetectorFactoryTest extends AbstractDetectorFactoryTest {
    private static final double TOLERANCE = 0.001;

    @Test(expected = RuntimeException.class)
    public void testInit_nullDocument() {
        new ConstantThresholdDetectorFactory(null);
    }

    @Test
    public void testBuildDetector() {
        val document = readDocument("constant-threshold");
        val factoryUnderTest = new ConstantThresholdDetectorFactory(document);
        val detector = factoryUnderTest.buildDetector();
        val params = detector.getParams();
        val thresholds = params.getThresholds();

        assertNotNull(detector);
        assertEquals(ConstantThresholdDetector.class, detector.getClass());
        assertEquals("e2e290a0-d1c1-471e-9d72-79d43282cfbd", detector.getUuid().toString());
        assertEquals(OutlierType.RIGHT_TAILED, params.getType());
        assertEquals(16666.0, thresholds.getUpperStrong(), TOLERANCE);
        assertEquals(2161.0, thresholds.getUpperWeak(), TOLERANCE);
    }

    @Test(expected = RuntimeException.class)
    public void testBuildDetector_invalidUuid() {
        readDocument("constant-threshold-invalid-uuid");
    }

    @Test(expected = RuntimeException.class)
    public void testBuildDetector_invalidParams() {
        val document = readDocument("constant-threshold-invalid-params");
        val factory = new ConstantThresholdDetectorFactory(document);
        factory.buildDetector();
    }
}
