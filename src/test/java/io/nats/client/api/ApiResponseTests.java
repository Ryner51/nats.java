// Copyright 2020 The NATS Authors
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at:
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package io.nats.client.api;

import io.nats.client.JetStreamApiException;
import io.nats.client.support.Status;
import org.junit.jupiter.api.Test;

import static io.nats.client.api.ApiResponse.NO_TYPE;
import static io.nats.client.api.Error.*;
import static io.nats.client.utils.ResourceUtils.dataAsString;
import static org.junit.jupiter.api.Assertions.*;

public class ApiResponseTests {

    static class TestApiResponse extends ApiResponse<TestApiResponse> {
        TestApiResponse(String json) {
            super(json);
        }

        public TestApiResponse() { }
    }

    @Test
    public void testNotError() {
        TestApiResponse jsApiResp = new TestApiResponse(dataAsString("ConsumerInfo.json"));
        assertFalse(jsApiResp.hasError());
        assertNull(jsApiResp.getError());
    }

    @Test
    public void testErrorResponse() {
        String text = dataAsString("ErrorResponses.json.txt");
        String[] jsons = text.split("~");

        TestApiResponse jsApiResp = new TestApiResponse(jsons[0]);
        assertTrue(jsApiResp.hasError());
        assertEquals("code_and_desc_response", jsApiResp.getType());
        assertEquals(500, jsApiResp.getErrorCode());
        assertEquals("the description", jsApiResp.getDescription());
        assertEquals("the description (500)", jsApiResp.getError());
        JetStreamApiException jsApiEx = new JetStreamApiException(jsApiResp);
        assertEquals(500, jsApiEx.getErrorCode());
        assertEquals("the description", jsApiEx.getErrorDescription());

        jsApiResp = new TestApiResponse(jsons[1]);
        assertTrue(jsApiResp.hasError());
        assertEquals("zero_and_desc_response", jsApiResp.getType());
        assertEquals(0, jsApiResp.getErrorCode());
        assertEquals("the description", jsApiResp.getDescription());
        assertEquals("the description (0)", jsApiResp.getError());
        jsApiEx = new JetStreamApiException(jsApiResp);
        assertEquals(0, jsApiEx.getErrorCode());
        assertEquals("the description", jsApiEx.getErrorDescription());

        jsApiResp = new TestApiResponse(jsons[2]);
        assertTrue(jsApiResp.hasError());
        assertEquals("non_zero_code_only_response", jsApiResp.getType());
        assertEquals(500, jsApiResp.getErrorCode());
        assertEquals("Unknown JetStream Error (500)", jsApiResp.getError());
        jsApiEx = new JetStreamApiException(jsApiResp);
        assertEquals(500, jsApiEx.getErrorCode());

        jsApiResp = new TestApiResponse(jsons[3]);
        assertTrue(jsApiResp.hasError());
        assertEquals("no_code_response", jsApiResp.getType());
        assertEquals(NOT_SET, jsApiResp.getErrorCode());
        assertEquals("no code", jsApiResp.getDescription());
        assertEquals("no code", jsApiResp.getError());
        jsApiEx = new JetStreamApiException(jsApiResp);
        assertEquals(NOT_SET, jsApiEx.getErrorCode());
        assertEquals(NOT_SET, jsApiEx.getApiErrorCode());
        assertEquals("no code", jsApiEx.getErrorDescription());

        jsApiResp = new TestApiResponse(jsons[4]);
        assertTrue(jsApiResp.hasError());
        assertEquals("empty_response", jsApiResp.getType());
        assertEquals(NOT_SET, jsApiResp.getErrorCode());
        assertEquals("Unknown JetStream Error", jsApiResp.getError());
        jsApiEx = new JetStreamApiException(jsApiResp);
        assertEquals(NOT_SET, jsApiEx.getErrorCode());
        assertEquals(NOT_SET, jsApiEx.getApiErrorCode());

        TestApiResponse notErrorResponse = new TestApiResponse(jsons[5]);
        assertFalse(notErrorResponse.hasError());
        assertEquals("not_error_response", notErrorResponse.getType());
        //noinspection ThrowableNotThrown
        assertThrows(NullPointerException.class, () -> new JetStreamApiException(notErrorResponse));

        jsApiResp = new TestApiResponse(jsons[6]);
        assertTrue(jsApiResp.hasError());
        assertEquals(NO_TYPE, jsApiResp.getType());
        assertEquals(NO_TYPE, jsApiResp.getType()); // coverage!

        jsApiResp = new TestApiResponse(jsons[7]);
        assertTrue(jsApiResp.hasError());
        assertEquals("code_desc_err_response", jsApiResp.getType());
        assertEquals(500, jsApiResp.getErrorCode());
        assertEquals("the description", jsApiResp.getDescription());
        assertEquals("the description [12345]", jsApiResp.getError());
        assertEquals(12345, jsApiResp.getApiErrorCode());
        jsApiEx = new JetStreamApiException(jsApiResp);
        assertEquals(500, jsApiEx.getErrorCode());
        assertEquals("the description", jsApiEx.getErrorDescription());
        assertEquals(12345, jsApiEx.getApiErrorCode());

        jsApiResp = new TestApiResponse(jsons[8]);
        assertTrue(jsApiResp.hasError());
        assertEquals("no-code_desc_err_response", jsApiResp.getType());
        assertEquals(NOT_SET, jsApiResp.getErrorCode());
        assertEquals("the description", jsApiResp.getDescription());
        assertEquals("the description", jsApiResp.getError());
        jsApiEx = new JetStreamApiException(jsApiResp);
        assertEquals(NOT_SET, jsApiEx.getErrorCode());
        assertEquals("the description", jsApiEx.getErrorDescription());
        assertEquals(12345, jsApiEx.getApiErrorCode());

        jsApiResp = new TestApiResponse();
        assertEquals(NOT_SET, jsApiResp.getErrorCode());
        assertEquals(NOT_SET, jsApiResp.getApiErrorCode());
        assertNull(jsApiResp.getDescription());
    }

    @SuppressWarnings("SimplifiableAssertion")
    @Test
    public void testConvert() {
        assertTrue(JsNoMessageFoundErr == Error.convert(new Status(404, "four-oh-four")));
        assertTrue(JsBadRequestErr == Error.convert(new Status(408, "four-oh-eight")));
        Error e = Error.convert(new Status(499, "four-nine-nine"));
        assertEquals(499, e.getCode());
        assertEquals(NOT_SET, e.getApiErrorCode());
        assertEquals("four-nine-nine", e.getDescription());
    }
}
