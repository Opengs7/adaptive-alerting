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
package com.expedia.adaptivealerting.anomdetect.detect.outlier.algo;

import com.expedia.adaptivealerting.anomdetect.detect.AnomalyLevel;
import com.expedia.adaptivealerting.anomdetect.detect.outlier.OutlierThresholds;
import com.expedia.adaptivealerting.anomdetect.detect.outlier.OutlierType;
import lombok.val;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AnomalyClassifierTest {
    private AnomalyClassifier classifierUnderTest_leftTailed;
    private AnomalyClassifier classifierUnderTest_rightTailed;
    private AnomalyClassifier classifierUnderTest_twoTailed;

    @Before
    public void setUp() {
        this.classifierUnderTest_leftTailed = new AnomalyClassifier(OutlierType.LEFT_TAILED);
        this.classifierUnderTest_rightTailed = new AnomalyClassifier(OutlierType.RIGHT_TAILED);
        this.classifierUnderTest_twoTailed = new AnomalyClassifier(OutlierType.TWO_TAILED);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testClassify_nullThresholds() {
        classifierUnderTest_twoTailed.classify(null, 0.0);
    }

    @Test
    public void testUpperThresholds() {
        val thresholds = new OutlierThresholds(100.0, 50.0, null, null);

        Assert.assertEquals(AnomalyLevel.NORMAL, classifierUnderTest_leftTailed.classify(thresholds, 150.0));
        assertEquals(AnomalyLevel.NORMAL, classifierUnderTest_leftTailed.classify(thresholds, 75.0));
        assertEquals(AnomalyLevel.NORMAL, classifierUnderTest_leftTailed.classify(thresholds, 25.0));

        assertEquals(AnomalyLevel.STRONG, classifierUnderTest_rightTailed.classify(thresholds, 150.0));
        assertEquals(AnomalyLevel.WEAK, classifierUnderTest_rightTailed.classify(thresholds, 75.0));
        assertEquals(AnomalyLevel.NORMAL, classifierUnderTest_rightTailed.classify(thresholds, 25.0));

        assertEquals(AnomalyLevel.STRONG, classifierUnderTest_twoTailed.classify(thresholds, 150.0));
        assertEquals(AnomalyLevel.WEAK, classifierUnderTest_twoTailed.classify(thresholds, 75.0));
        assertEquals(AnomalyLevel.NORMAL, classifierUnderTest_twoTailed.classify(thresholds, 25.0));
    }

    @Test
    public void testLowerThresholds() {
        val thresholds = new OutlierThresholds(null, null, 50.0, 25.0);

        assertEquals(AnomalyLevel.STRONG, classifierUnderTest_leftTailed.classify(thresholds, 0.0));
        assertEquals(AnomalyLevel.WEAK, classifierUnderTest_leftTailed.classify(thresholds, 35.0));
        assertEquals(AnomalyLevel.NORMAL, classifierUnderTest_leftTailed.classify(thresholds, 100.0));

        assertEquals(AnomalyLevel.NORMAL, classifierUnderTest_rightTailed.classify(thresholds, 0.0));
        assertEquals(AnomalyLevel.NORMAL, classifierUnderTest_rightTailed.classify(thresholds, 35.0));
        assertEquals(AnomalyLevel.NORMAL, classifierUnderTest_rightTailed.classify(thresholds, 100.0));

        assertEquals(AnomalyLevel.STRONG, classifierUnderTest_twoTailed.classify(thresholds, 0.0));
        assertEquals(AnomalyLevel.WEAK, classifierUnderTest_twoTailed.classify(thresholds, 35.0));
        assertEquals(AnomalyLevel.NORMAL, classifierUnderTest_twoTailed.classify(thresholds, 100.0));
    }
}
