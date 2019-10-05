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

import com.expedia.adaptivealerting.anomdetect.util.DocumentNotFoundException;
import com.expedia.adaptivealerting.anomdetect.util.HttpClientWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.io.IOException;
import java.util.UUID;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.notNull;

/**
 * Client for managing forecasters in the Model Service.
 */
@RequiredArgsConstructor
public class ForecasterDocumentClient {
    static final String FIND_PATH = "/api/v2/forecasters/findByUuid?uuid=%s";

    @NonNull
    private HttpClientWrapper httpClient;

    @NonNull
    private String baseUri;

    @NonNull
    private ObjectMapper objectMapper;

    public ForecasterDocument find(UUID uuid) throws IOException {
        notNull(uuid, "uuid can't be null");

        val uri = String.format(uriTemplate(baseUri, FIND_PATH), uuid);
        val content = httpClient.get(uri);
        val document = objectMapper.readValue(content.asBytes(), ForecasterDocument.class);

        if (document == null) {
            throw new DocumentNotFoundException("Forecaster not found: uuid=" + uuid);
        }

        return document;
    }

    static String uriTemplate(String baseUri, String path) {
        return baseUri + path;
    }
}
