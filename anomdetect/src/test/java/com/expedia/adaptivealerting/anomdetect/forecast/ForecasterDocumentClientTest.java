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
package com.expedia.adaptivealerting.anomdetect.forecast;

import com.expedia.adaptivealerting.anomdetect.util.HttpClientWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.http.client.fluent.Content;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class ForecasterDocumentClientTest {
    private static final String BASE_URI = "http://modelservice";
    private static final UUID FORECASTER_UUID = UUID.randomUUID();

    private ForecasterDocumentClient clientUnderTest;

    @Mock
    private HttpClientWrapper httpClient;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Content forecasterContent;

    private byte[] forecasterContentBytes;

    @Mock
    private ForecasterDocument forecasterDocument;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        initTestObjects();
        initDependencies();
        this.clientUnderTest = new ForecasterDocumentClient(httpClient, BASE_URI, objectMapper);
    }

    @Test
    public void testFind() throws IOException {
        val actualDocument = clientUnderTest.find(FORECASTER_UUID);
        assertEquals(forecasterDocument, actualDocument);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFind_nullUuid() throws IOException {
        clientUnderTest.find(null);
    }

    private void initTestObjects() {
        this.forecasterContentBytes = "someForecasterContent".getBytes();
    }

    private void initDependencies() throws Exception {
        val findUriTemplate = ForecasterDocumentClient.uriTemplate(BASE_URI, ForecasterDocumentClient.FIND_PATH);
        val findUri = String.format(findUriTemplate, FORECASTER_UUID);
        when(httpClient.get(findUri)).thenReturn(forecasterContent);
        when(forecasterContent.asBytes()).thenReturn(forecasterContentBytes);
        when(objectMapper.readValue(forecasterContentBytes, ForecasterDocument.class)).thenReturn(forecasterDocument);
    }
}
