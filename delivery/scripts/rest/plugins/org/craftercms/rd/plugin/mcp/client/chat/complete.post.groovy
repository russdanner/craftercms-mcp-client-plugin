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
@Grab(group='org.springframework.ai', module='spring-ai-client-chat', version='1.0.0', initClass=false, systemClassLoader=true)
@Grab(group='org.springframework.ai', module='spring-ai-openai', version='1.0.0', initClass=false, systemClassLoader=true)

import groovy.json.JsonSlurper

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor

import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.ai.openai.api.OpenAiApi

import org.springframework.util.LinkedMultiValueMap

import org.springframework.http.HttpHeaders
import org.springframework.http.client.ClientHttpResponse

import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClient.Builder
import org.springframework.web.client.DefaultResponseErrorHandler

import org.springframework.web.reactive.function.client.WebClient

import plugins.org.craftercms.rd.plugin.mcp.client.McpSyncClient
import plugins.org.craftercms.rd.plugin.mcp.client.McpToolCallbackProvider


// Get configuration
def mcpServerUrl = pluginConfig.getString('mcpServerUrl') 
def aiSystemPrompt = pluginConfig.getString('aiSystemPrompt')
def aiModelId = "".equals(pluginConfig.getString('aiModelId')) ? "" : pluginConfig.getString('aiModelId') 

// Get secrets
def openAIKey = System.getenv("crafter_openai")

// Collect input values
def jsonSlurper = new JsonSlurper()
def requestBody = jsonSlurper.parseText(request.reader.text)
def query = requestBody.message
def siteId = siteContext.siteName

// We may need a preview token if we plan to call a local MCP Server while running in preview mode.
def previewToken = getCookieValue(request, "crafterPreview")

if (!query) {
    logger.error("Message field is missing from request")
    return [error: "Message field is required"]
}

try {
    // Initialize MCP client
    def mcpClient = buildMcpClient(siteId, previewToken, mcpServerUrl, request)
    
    // Initialize MCP client
    def mcpClientInitResult = mcpClient.initialize()

    // Initialize OpenAI ChatClient with our custom MCP tool provider
    def chatModel = buildOpenAiChatModel(openAIKey, aiModelId)
    def toolCallbackProvider = new McpToolCallbackProvider(mcpClient)
    
    // Add our MCP server tools provider to the chat client
    def chatClient = ChatClient.builder(chatModel)
        .defaultToolCallbacks(toolCallbackProvider)
        .build()

    // aiSystemPrompt
    
    // Execute chat request
    def chatResponse = chatClient.prompt()
        .user(query)
        .call()
        .content()

    return [response: chatResponse]

} catch (Exception err) {
    logger.error("Error processing request: ${err.message}")
    return [error: "Internal server error: ${err.message}"]
}

/**
 * Build OpenAI Chat Model with proper configuration
 */
def buildOpenAiChatModel(openAIKey, modelId) {

    if (!openAIKey) {
        throw new IllegalStateException("OpenAI API key not found in environment variable 'crafter_chatgpt'")
    }

    def restClientBuilder = RestClient.builder()
    restClientBuilder.defaultHeaders { headers ->
        headers.set(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate")
        headers.set(HttpHeaders.CONTENT_TYPE, "application/json")
        headers.set(HttpHeaders.ACCEPT, "application/json")
    }

    def webClientBuilder = WebClient.builder()
    def responseErrorHandler = new DefaultResponseErrorHandler()
    
    def headers = new LinkedMultiValueMap<String, String>()
    headers.add("Content-Type", "application/json")
    headers.add("Accept", "application/json")

    def openAiApi = OpenAiApi.builder()
        .baseUrl("https://api.openai.com/v1")
        .apiKey(openAIKey)
        .completionsPath("/chat/completions")
        // .headers(headers)
        .restClientBuilder(restClientBuilder)
        .webClientBuilder(webClientBuilder)
        .responseErrorHandler(responseErrorHandler)
        .build()

    // adjust model params as needed
    def openAiChatOptions = OpenAiChatOptions.builder()
        .model(modelId)
        //.temperature(0.7)
        //.maxTokens(1000)
        .build()

    return OpenAiChatModel.builder()
        .openAiApi(openAiApi)
        .defaultOptions(openAiChatOptions)
        .build()
}

/**
 * Build MCP client with synchronous HTTP configuration
 */
def buildMcpClient(currentSiteId, previewToken, mcpServerUrl, request) {
    def siteId = currentSiteId
    def serverScheme = request.getScheme()
    def serverName = request.getServerName()
    def serverPort = request.getServerPort()
    mcpServerUrl = "".equals(mcpServerUrl) ? "$serverScheme://$serverName:$serverPort/" : mcpServerUrl

    def restClient = RestClient.builder()
        .baseUrl(mcpServerUrl)
        .defaultHeaders { headers ->
            headers.set(HttpHeaders.CONTENT_TYPE, "application/json")
            headers.set(HttpHeaders.ACCEPT, "application/json;q=1.0, text/event-stream;q=0.9")
            headers.set("X-Crafter-Site", siteId)

            if(modePreview) {
                headers.set("X-Crafter-Preview", previewToken)
            }
            //else {
            //    headers.set("Authorization", "Bearer: TheWheelsOnTheBusGoRoundAndRound-AndRoundAndRoundAndRoundAndRound")
            //}
        }
        .build()

    return new McpSyncClient(restClient, mcpServerUrl)
}

/**
 * Return the value of a given cookie
 */
def getCookieValue(request, name) {
    if (request.getCookies() != null) {
        for (def cookie : request.getCookies()) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue()
            }
        }
    }
    return null; // not found
}
