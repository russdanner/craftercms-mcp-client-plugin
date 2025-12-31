/**
* MIT License
*
* Copyright (c) 2018-2025 Crafter Software Corporation. All Rights Reserved.
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in all
* copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
* SOFTWARE.
*/
package plugins.org.craftercms.rd.plugin.mcp.client

import com.fasterxml.jackson.databind.ObjectMapper

import org.springframework.web.client.RestClient

import groovy.json.JsonSlurper

import java.nio.charset.StandardCharsets

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * MCP Sync Client that implements synchronous HTTP communication
 */
class McpSyncClient {

    private static final Logger logger = LoggerFactory.getLogger(McpSyncClient.class)

    private final RestClient restClient
    private final ObjectMapper objectMapper
    private boolean initialized = false
    private String serverUrl

    McpSyncClient(restClient, serverUrl) {
        this.restClient = restClient
        this.objectMapper = new ObjectMapper()
        this.serverUrl = serverUrl
    }

    /**
     * Initialize session with the server
     */
    def initialize() {
        def request = [
            jsonrpc: "2.0",
            method: "initialize",
            params: [
                clientInfo: [
                    name: "mcp-client",
                    version: "1.0.0"
                ],
                clientCapabilities: [
                    roots: true,
                    sampling: true
                ]
            ],
            id: UUID.randomUUID().toString()
        ]

        logger.debug("Sending initialize request: ${objectMapper.writeValueAsString(request)}")
        
        def response = restClient.post()
            .uri(this.serverUrl)
            .body(request)
            .retrieve()

        def result = parseResponse(response)
        
        this.initialized = true
    }

    /** 
     * List tools found on the server
     */
    def listTools() {
        
        if (!initialized) {
            throw new IllegalStateException("Client not initialized")
        }

        def request = [
            jsonrpc: "2.0",
            method: "tools/list",
            params: [:],
            id: UUID.randomUUID().toString()
        ]

        logger.info("Sending listTools request: ${objectMapper.writeValueAsString(request)}")
        
        def response = restClient.post()
            .uri(this.serverUrl)
            .body(request)
            .retrieve()

        return parseResponse(response)
    }

    def callTool(String toolName, Map parameters) {

        if (!initialized) {
            throw new IllegalStateException("Client not initialized")
        }

        def request = [
            jsonrpc: "2.0",
            method: "tools/call",
            params: [
                name: toolName,
                arguments: parameters
            ],
            id: UUID.randomUUID().toString()
        ]

        logger.debug("Sending callTool request: ${objectMapper.writeValueAsString(request)}")
        
        def response = restClient.post()
            .uri(this.serverUrl)
            .body(request)
            .retrieve()

        return parseResponse(response)
    }

    /**
     * Indicates if client has been initialized
     */
    boolean isInitialized() {
        return this.initialized
    }
    
    /**
     * parse the request response
     */
    protected Object parseResponse(response) {
        
        def entity = response.toEntity(byte[])
        def headers = entity.headers
        def contentType = headers?.getFirst("Content-Type")
        def responseText = new String(entity.body, StandardCharsets.UTF_8)
        def retObj = [:]

        if("text/event-stream".equals(contentType)) {
            responseText = responseText.substring(responseText.indexOf("data: ")+6)            
            println("JSON \n\n $responseText")
        }

        def responseJson = new JsonSlurper().parseText(responseText)

        if(responseJson.body) {
            if (responseJson.body.error) {
                throw new RuntimeException("List tools failed: ${response.body.error.message}")
            }
            else {
                retObj = responseJson.result       
            }
        }
        else {
            retObj = responseJson.result       
        }

        return retObj        
    } 
}
