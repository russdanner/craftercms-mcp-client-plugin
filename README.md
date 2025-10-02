# LLM Chat Client with Model Context Protocol (MCP) Integration for CrafterCMS

## Overview
This plugin installs a LLM chat client example that has an integration with an MCP server.
- The client is based on Spring AI's AI orchestration framework.
- The assumed LLM implementation is OpenAI. That said, through the Spring AI framework, it's easy to swap in other vendors/APIs.

<img width="771" height="503" alt="image" src="https://github.com/user-attachments/assets/8c742137-d009-49e3-8064-d6730e392cb8" />

## Installation & Configuration

1. Configure the following environment variable with your OpenAI key `crafter_openai`
2. Install this plugin into the project.
3. For a simple UI example, include the example chat client template
```
<#include '/templates/plugins/org/craftercms/rd/plugin/mcp/client/ai-chat.ftl' />
```
