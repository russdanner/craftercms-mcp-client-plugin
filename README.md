# Model Context Protocol (MCP) Client Integration for CrafterCMS

## Overview

This plugin a SpringAI-based chat service configured with an (Model Context Protocol) MCP client. This serves as a foundation for an **AI-enabled application host**, orchestrating Large Language Models while securely exposing content, services, and tools through MCP.

* Built on **Spring AI’s orchestration framework**
* Uses **OpenAI** as the default LLM provider
* Designed to be **vendor-agnostic** — other LLM providers can be swapped in via Spring AI with minimal changes
* Demonstrates **end-to-end MCP integration** from UI → LLM → MCP server → CrafterCMS context

<img width="771" height="503" alt="image" src="https://github.com/user-attachments/assets/8c742137-d009-49e3-8064-d6730e392cb8" />

---

## What MCP Enables

The **Model Context Protocol (MCP)** provides a standardized way for LLMs to:

* Discover available **tools**, **resources**, and **capabilities**
* Request structured data and perform actions through well-defined interfaces
* Operate with **explicit, auditable context** rather than implicit prompt stuffing

Within CrafterCMS, MCP enables:

* Secure access to CMS content, metadata, and services
* Controlled execution of server-side tools
* Clear separation between:

  * **LLM reasoning**
  * **Context exposure**
  * **Application logic**

This architecture makes AI integrations **safer, more maintainable, and more extensible** than traditional prompt-only approaches.

---

## Architecture Summary

At a high level, the plugin consists of:

* An **MCP server integration** that exposes CrafterCMS context and capabilities to the LLM
* A **Spring AI chat client** responsible for:

  * Prompt orchestration
  * Tool invocation
  * LLM provider abstraction
* A **FreeMarker-based chat UI** embedded in CrafterCMS

---

## Installation & Configuration

### Prerequisites

* A valid **OpenAI API key**

### Steps

1. Configure the following environment variable with your OpenAI API key:

   ```
   crafter_openai
   ```

2. Install the plugin into your CrafterCMS project.

Optional parameters (shown here with defaults):
```xml
<configuration>
    <mcpServerUrl>/api/plugins/org/craftercms/rd/plugin/mcp/server/craftermcp/mcp.json</mcpServerUrl>
    <aiSystemPrompt>You are a helpful assistant who answers questions in a professional manner.</aiSystemPrompt>
    <aiModelId>o4-mini-2025-04-16</aiModelId>
</configuration>
```
3. To enable the example chat UI, include the provided FreeMarker template:

   ```ftl
   <#include '/templates/plugins/org/craftercms/rd/plugin/mcp/client/ai-chat.ftl' />
   ```

Once included, the chat interface will be available within your site and connected to the LLM through Spring AI and MCP.
