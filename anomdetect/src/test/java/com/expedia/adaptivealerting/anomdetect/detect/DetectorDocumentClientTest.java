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
package com.expedia.adaptivealerting.anomdetect.detect;

import com.expedia.adaptivealerting.anomdetect.mapper.DetectorMapping;
import com.expedia.adaptivealerting.anomdetect.mapper.DetectorMatchResponse;
import com.expedia.adaptivealerting.anomdetect.util.DocumentNotFoundException;
import com.expedia.adaptivealerting.anomdetect.util.HttpClientWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.http.client.fluent.Content;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.expedia.adaptivealerting.anomdetect.detect.DetectorDocumentClient.FIND_DOCUMENT_PATH;
import static com.expedia.adaptivealerting.anomdetect.detect.DetectorDocumentClient.FIND_MAPPINGS_BY_TAGS_PATH;
import static com.expedia.adaptivealerting.anomdetect.detect.DetectorDocumentClient.FIND_UPDATED_DOCUMENTS_PATH;
import static com.expedia.adaptivealerting.anomdetect.detect.DetectorDocumentClient.FIND_UPDATED_MAPPINGS_PATH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * {@link DetectorDocumentClient} unit tests.
 */
@Slf4j
public class DetectorDocumentClientTest {
    private static final String BASE_URI = "http://example.com";

    private static final UUID DETECTOR_UUID = UUID.randomUUID();
    private static final UUID DETECTOR_UUID_CANT_GET = UUID.randomUUID();
    private static final UUID DETECTOR_UUID_CANT_READ = UUID.randomUUID();
    private static final UUID DETECTOR_UUID_NO_DOCS = UUID.randomUUID();

    private static final int TIME_PERIOD_INVALID = 0;
    private static final int TIME_PERIOD_VALID = 1000;
    private static final int TIME_PERIOD_CANT_GET = 2000;
    private static final int TIME_PERIOD_CANT_READ = 3000;
    private static final int TIME_PERIOD_NO_MAPPINGS = 4000;

    private static final String FIND_DOC_URI = uri(FIND_DOCUMENT_PATH, DETECTOR_UUID);
    private static final String FIND_DOC_URI_CANT_GET = uri(FIND_DOCUMENT_PATH, DETECTOR_UUID_CANT_GET);
    private static final String FIND_DOC_URI_CANT_READ = uri(FIND_DOCUMENT_PATH, DETECTOR_UUID_CANT_READ);
    private static final String FIND_DOC_URI_NO_DOC = uri(FIND_DOCUMENT_PATH, DETECTOR_UUID_NO_DOCS);

    private static final String FIND_UPDATED_DOCS_URI = uri(FIND_UPDATED_DOCUMENTS_PATH, TIME_PERIOD_VALID);
    private static final String FIND_UPDATED_DOCS_URI_CANT_GET = uri(FIND_UPDATED_DOCUMENTS_PATH, TIME_PERIOD_CANT_GET);
    private static final String FIND_UPDATED_DOCS_URI_CANT_READ = uri(FIND_UPDATED_DOCUMENTS_PATH, TIME_PERIOD_CANT_READ);

    private static final String FIND_MAPPINGS_URI = BASE_URI + FIND_MAPPINGS_BY_TAGS_PATH;

    private static final String FIND_UPDATED_MAPPINGS_URI = uri(FIND_UPDATED_MAPPINGS_PATH, TIME_PERIOD_VALID);
    private static final String FIND_UPDATED_MAPPINGS_URI_CANT_GET = uri(FIND_UPDATED_MAPPINGS_PATH, TIME_PERIOD_CANT_GET);
    private static final String FIND_UPDATED_MAPPINGS_URI_CANT_READ = uri(FIND_UPDATED_MAPPINGS_PATH, TIME_PERIOD_CANT_READ);
    private static final String FIND_UPDATED_MAPPINGS_URI_NO_MAPPINGS = uri(FIND_UPDATED_MAPPINGS_PATH, TIME_PERIOD_NO_MAPPINGS);


    private DetectorDocumentClient clientUnderTest;

    // ================================================================================
    // Dependencies
    // ================================================================================

    @Mock
    private HttpClientWrapper httpClient;

    @Mock
    private ObjectMapper objectMapper;

    // ================================================================================
    // Test objects
    // ================================================================================

    // Metric tags for updated detector mappings
    private List<Map<String, String>> tags = Collections.singletonList(Collections.singletonMap("tags", "valid"));
    private List<Map<String, String>> tags_cantPost = Collections.singletonList(Collections.singletonMap("tags", "cantPost"));
    private List<Map<String, String>> tags_cantRead = Collections.singletonList(Collections.singletonMap("tags", "cantRead"));

    @Mock
    private Content docContent;

    @Mock
    private Content docContent_cantRead;

    @Mock
    private Content docContent_noDoc;

    @Mock
    private Content updatedDocsContent;

    @Mock
    private Content updatedDocsContent_cantRead;

    @Mock
    private Content mappingContent;

    @Mock
    private Content mappingContent_cantRead;

    @Mock
    private Content updatedMappingContent;

    @Mock
    private Content updatedMappingContent_cantRead;

    @Mock
    private Content updatedMappingContent_noMappings;

    private byte[] docBytes = "docBytes".getBytes();
    private byte[] docBytes_cantRead = "docBytes_cantRead".getBytes();
    private byte[] docBytes_noDocs = "docBytes_noDocs".getBytes();
    private byte[] updatedDocsBytes = "updatedDocsBytes".getBytes();
    private byte[] updatedDocsBytes_cantRead = "updatedDocsBytes_cantRead".getBytes();
    private byte[] mappingBytes = "mappingBytes".getBytes();
    private byte[] mappingBytes_cantRead = "mappingBytes_cantRead".getBytes();
    private byte[] updatedMappingBytes = "updatedMappingBytes".getBytes();
    private byte[] updatedMappingBytes_cantRead = "updatedMappingBytes_cantRead".getBytes();
    private byte[] updatedMappingBytes_noMappings = "updatedMappingBytes_noMappings".getBytes();

    private String tagsBody = "tagsBody";
    private String tagsBody_cantPost = "tagsBody_cantPost";
    private String tagsBody_cantRead = "tagsBody_cantRead";

    private DetectorDocument[] docs = {};
    private List<DetectorMapping> mappings = new ArrayList<>();

    @Mock
    private DetectorDocument doc;

    @Mock
    private DetectorMatchResponse detectorMatchResponse;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        initFindDetectorDocument();
        initFindUpdatedDetectorDocuments();
        initFindMatchingDetectorMappings();
        initFindUpdatedDetectorMappings();
        this.clientUnderTest = new DetectorDocumentClient(httpClient, BASE_URI, objectMapper);
    }

    // ================================================================================
    // findDetectorDocument
    // ================================================================================

    @Test
    public void testFindDetectorDocument() {
        val result = clientUnderTest.findDetectorDocument(DETECTOR_UUID);
        assertNotNull(result);
    }

    @Test(expected = DetectorException.class)
    public void testFindDetectorDocument_cantGet() {
        clientUnderTest.findDetectorDocument(DETECTOR_UUID_CANT_GET);
    }

    @Test(expected = DetectorException.class)
    public void testFindDetectorDocument_cantRead() {
        clientUnderTest.findDetectorDocument(DETECTOR_UUID_CANT_READ);
    }

    @Test(expected = DocumentNotFoundException.class)
    public void testFindDetectorDocument_noDocs() {
        clientUnderTest.findDetectorDocument(DETECTOR_UUID_NO_DOCS);
    }

    // ================================================================================
    // findUpdatedDetectorDocuments
    // ================================================================================

    @Test(expected = IllegalArgumentException.class)
    public void testFindUpdatedDetectorDocuments_timePeriodInvalid() {
        clientUnderTest.findUpdatedDetectorDocuments(TIME_PERIOD_INVALID);
    }

    @Test(expected = DetectorException.class)
    public void testFindUpdatedDetectorDocuments_cantGet() {
        clientUnderTest.findUpdatedDetectorDocuments(TIME_PERIOD_CANT_GET);
    }

    @Test(expected = DetectorException.class)
    public void testFindUpdatedDetectorDocuments_cantRead() {
        clientUnderTest.findUpdatedDetectorDocuments(TIME_PERIOD_CANT_READ);
    }

    // ================================================================================
    // findMatchingDetectorMappings
    // ================================================================================

    @Test
    public void testFindMatchingDetectorMappings() {
        val result = clientUnderTest.findMatchingDetectorMappings(tags);
        assertEquals(detectorMatchResponse, result);
    }

    @Test(expected = DetectorException.class)
    public void testFindMatchingDetectorMappings_cantGet() {
        clientUnderTest.findMatchingDetectorMappings(tags_cantPost);
    }

    @Test(expected = DetectorException.class)
    public void testFindMatchingDetectorMappings_cantRead() {
        clientUnderTest.findMatchingDetectorMappings(tags_cantRead);
    }

    // ================================================================================
    // findUpdatedDetectorMappings
    // ================================================================================

    @Test
    public void testFindUpdatedDetectorMappings() {
        val mappings = clientUnderTest.findUpdatedDetectorMappings(TIME_PERIOD_VALID);
        assertNotNull(mappings);
        assertSame(mappings, mappings);
    }

    @Test(expected = DetectorException.class)
    public void testFindUpdatedDetectorMappings_cantGet() {
        clientUnderTest.findUpdatedDetectorMappings(TIME_PERIOD_CANT_GET);
    }

    @Test(expected = DetectorException.class)
    public void testFindUpdatedDetectorMappings_cantRead() {
        clientUnderTest.findUpdatedDetectorMappings(TIME_PERIOD_CANT_READ);
    }

    @Test(expected = DetectorException.class)
    public void testFindUpdatedDetectorMappings_noMappings() {
        clientUnderTest.findUpdatedDetectorMappings(TIME_PERIOD_NO_MAPPINGS);
    }

    // ================================================================================
    // Helpers
    // ================================================================================

    private static String uri(String path, Object param) {
        return String.format(BASE_URI + path, param);
    }

    private void initFindDetectorDocument() throws IOException {
        when(httpClient.get(FIND_DOC_URI)).thenReturn(docContent);
        when(docContent.asBytes()).thenReturn(docBytes);
        when(objectMapper.readValue(docBytes, DetectorDocument.class)).thenReturn(doc);

        when(httpClient.get(FIND_DOC_URI_CANT_GET)).thenThrow(new IOException());

        when(httpClient.get(FIND_DOC_URI_CANT_READ)).thenReturn(docContent_cantRead);
        when(docContent_cantRead.asBytes()).thenReturn(docBytes_cantRead);
        when(objectMapper.readValue(docBytes_cantRead, DetectorDocument.class)).thenThrow(new IOException());

        when(httpClient.get(FIND_DOC_URI_NO_DOC)).thenReturn(docContent_noDoc);
        when(docContent_noDoc.asBytes()).thenReturn(docBytes_noDocs);
        when(objectMapper.readValue(docBytes_noDocs, DetectorDocument.class)).thenReturn(null);
    }

    private void initFindUpdatedDetectorDocuments() throws IOException {
        when(httpClient.get(FIND_UPDATED_DOCS_URI)).thenReturn(updatedDocsContent);
        when(updatedDocsContent.asBytes()).thenReturn(updatedDocsBytes);
        when(objectMapper.readValue(updatedDocsBytes, DetectorDocument[].class)).thenReturn(docs);

        when(httpClient.get(FIND_UPDATED_DOCS_URI_CANT_GET)).thenThrow(new IOException());

        when(httpClient.get(FIND_UPDATED_DOCS_URI_CANT_READ)).thenReturn(updatedDocsContent_cantRead);
        when(updatedDocsContent_cantRead.asBytes()).thenReturn(updatedDocsBytes_cantRead);
        when(objectMapper.readValue(updatedDocsBytes_cantRead, DetectorDocument[].class)).thenThrow(new IOException());
    }

    private void initFindMatchingDetectorMappings() throws IOException {
        when(objectMapper.writeValueAsString(tags)).thenReturn(tagsBody);
        when(httpClient.post(FIND_MAPPINGS_URI, tagsBody)).thenReturn(mappingContent);
        when(mappingContent.asBytes()).thenReturn(mappingBytes);
        when(objectMapper.readValue(mappingBytes, DetectorMatchResponse.class)).thenReturn(detectorMatchResponse);

        when(objectMapper.writeValueAsString(tags_cantPost)).thenReturn(tagsBody_cantPost);
        when(httpClient.post(FIND_MAPPINGS_URI, tagsBody_cantPost)).thenThrow(new IOException());

        when(objectMapper.writeValueAsString(tags_cantRead)).thenReturn(tagsBody_cantRead);
        when(httpClient.post(FIND_MAPPINGS_URI, tagsBody_cantRead)).thenReturn(mappingContent_cantRead);
        when(mappingContent_cantRead.asBytes()).thenReturn(mappingBytes_cantRead);
        when(objectMapper.readValue(mappingBytes_cantRead, DetectorMatchResponse.class)).thenThrow(new IOException());
    }

    private void initFindUpdatedDetectorMappings() throws IOException {
        when(httpClient.get(FIND_UPDATED_MAPPINGS_URI)).thenReturn(updatedMappingContent);
        when(updatedMappingContent.asBytes()).thenReturn(updatedMappingBytes);
        when(objectMapper.readValue(eq(updatedMappingBytes), any(TypeReference.class))).thenReturn(mappings);

        when(httpClient.get(FIND_UPDATED_MAPPINGS_URI_CANT_GET)).thenThrow(new IOException());

        when(httpClient.get(FIND_UPDATED_MAPPINGS_URI_CANT_READ)).thenReturn(updatedMappingContent_cantRead);
        when(updatedMappingContent_cantRead.asBytes()).thenReturn(updatedMappingBytes_cantRead);
        when(objectMapper.readValue(eq(updatedMappingBytes_cantRead), any(TypeReference.class))).thenThrow(new IOException());

        when(httpClient.get(FIND_UPDATED_MAPPINGS_URI_NO_MAPPINGS)).thenReturn(updatedMappingContent_noMappings);
        when(updatedMappingContent_noMappings.asBytes()).thenReturn(updatedMappingBytes_noMappings);
        when(objectMapper.readValue(eq(updatedMappingBytes_noMappings), any(TypeReference.class))).thenReturn(null);
    }
}
