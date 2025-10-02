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

@Grab(group='org.springframework.ai', module='spring-ai-mcp', version='1.0.0', initClass=false, systemClassLoader=true)

import org.springframework.ai.tool.ToolCallback
import org.springframework.ai.tool.ToolCallbackProvider

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Tool Callback Provider that integrates our MCP client with Spring AI
 */
class McpToolCallbackProvider implements ToolCallbackProvider {
    private final McpSyncClient mcpClient

    private static final Logger logger = LoggerFactory.getLogger(McpToolCallbackProvider.class)

    McpToolCallbackProvider( mcpClient) {
        this.mcpClient = mcpClient
    }

    ToolCallback[] getToolCallbacks() {
        if (!mcpClient.isInitialized()) {
            logger.warn("MCP client not initialized, returning empty tool list")
            return new ToolCallback[0]
        }


        def toolResults = []
        
        try {
            def toolsList = mcpClient.listTools()

            def tools = toolsList.tools ?: []
            
            logger.info("Found ${tools.size()} tools from MCP server")
        
            tools.each { tool ->
                def toolCb = new ClientToolCallback()
                logger.info(" --->  ${tool.name}")

                toolCb.name = tool.name
                toolCb.description = tool.description
                toolCb.client = mcpClient
 
                def jsonBuilder = new groovy.json.JsonBuilder(tool.inputSchema)
                toolCb.inputSchema = jsonBuilder.toPrettyString()
                toolResults.add(toolCb)
            }
            System.out.println(toolResults)
            return toolResults
            
        } catch (Exception e) {
            logger.error("Error listing MCP tools: ${e.message}")
            return new ToolCallback[0]
        }
    }
}
