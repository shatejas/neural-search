/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.neuralsearch.processor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.apache.http.HttpHeaders;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.opensearch.client.Response;
import org.opensearch.common.xcontent.XContentHelper;
import org.opensearch.common.xcontent.XContentType;
import org.opensearch.neuralsearch.BaseNeuralSearchIT;

import com.google.common.collect.ImmutableList;

public class SparseEncodingProcessIT extends BaseNeuralSearchIT {

    private static final String INDEX_NAME = "sparse_encoding_index";

    private static final String PIPELINE_NAME = "pipeline-sparse-encoding";

    @Before
    public void setUp() throws Exception {
        super.setUp();
        updateClusterSettings();
    }

    public void testSparseEncodingProcessor() throws Exception {
        String modelId = null;
        try {
            modelId = prepareSparseEncodingModel();
            createPipelineProcessor(modelId, PIPELINE_NAME, ProcessorType.SPARSE_ENCODING);
            createSparseEncodingIndex();
            ingestDocument();
            assertEquals(1, getDocCount(INDEX_NAME));
        } finally {
            wipeOfTestResources(INDEX_NAME, PIPELINE_NAME, modelId, null);
        }
    }

    private void createSparseEncodingIndex() throws Exception {
        createIndexWithConfiguration(
            INDEX_NAME,
            Files.readString(Path.of(classLoader.getResource("processor/SparseEncodingIndexMappings.json").toURI())),
            PIPELINE_NAME
        );
    }

    private void ingestDocument() throws Exception {
        String ingestDocument = "{\n"
            + "  \"title\": \"This is a good day\",\n"
            + "  \"description\": \"daily logging\",\n"
            + "  \"favor_list\": [\n"
            + "    \"test\",\n"
            + "    \"hello\",\n"
            + "    \"mock\"\n"
            + "  ],\n"
            + "  \"favorites\": {\n"
            + "    \"game\": \"overwatch\",\n"
            + "    \"movie\": null\n"
            + "  }\n"
            + "}\n";
        Response response = makeRequest(
            client(),
            "POST",
            INDEX_NAME + "/_doc?refresh",
            null,
            toHttpEntity(ingestDocument),
            ImmutableList.of(new BasicHeader(HttpHeaders.USER_AGENT, "Kibana"))
        );
        Map<String, Object> map = XContentHelper.convertToMap(
            XContentType.JSON.xContent(),
            EntityUtils.toString(response.getEntity()),
            false
        );
        assertEquals("created", map.get("result"));
    }

}
