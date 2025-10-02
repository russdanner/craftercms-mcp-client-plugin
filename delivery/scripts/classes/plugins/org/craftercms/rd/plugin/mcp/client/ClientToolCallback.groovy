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

import org.springframework.ai.tool.ToolCallback
import org.springframework.ai.tool.definition.ToolDefinition


public class ClientToolCallback implements ToolCallback {

    def name
    def description
    def inputSchema
    def client 

    String getName() {
        return name
    }

    String getDescription() {
        return description ?: "MCP tool: ${name}"
    }

    ToolDefinition getToolDefinition() {
        // Create a basic tool definition from MCP tool schema
        return ToolDefinition.builder()
            .name(name)
            .description(description ?: "MCP tool: ${name}")
            .inputSchema(inputSchema ?: [:])
            .build()
    }

    String call(String arguments) {

        try {            
            // Parse arguments JSON
            def argMap = [:]
            
            if (arguments && arguments.trim()) {
                argMap = new groovy.json.JsonSlurper().parseText(arguments)
            }

            
            def result = client.callTool(name, argMap)
            def response = result.content ?: result.output ?: result.toString()
            
            return response
            
        } catch (Exception e) {
            return "Error calling tool: ${e.message}"
        }
    }
    
}