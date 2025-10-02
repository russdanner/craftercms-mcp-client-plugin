# LLM Chat Client with Model Context Protocol (MCP) Integration for CrafterCMS

## Overview
Installs LLM Chat client example that has an integration with an MCP server
The client is based on Spring AI. The assumed LLM implementation is OpenAI (however this is easily changed out for other vendors/APIs.)

## Installation & Configuration

1. Configure the following environment variable with your OpenAI key `crafter_openai`
2. Install this plugin into the project.
3. For a simple UI example, include the example chat client template
```
<#include '/templates/plugins/org/craftercms/rd/plugin/mcp/client/ai-chat.ftl' />
```