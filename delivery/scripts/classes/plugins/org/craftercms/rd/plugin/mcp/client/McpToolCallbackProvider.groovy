package plugins.org.craftercms.rd.plugin.mcp.server.client

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
