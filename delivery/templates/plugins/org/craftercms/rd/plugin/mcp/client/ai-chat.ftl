<#import "/templates/system/common/crafter.ftl" as crafter />

<#include '/templates/plugins/org/craftercms/rd/plugin/mcp/client/ai-chat-head.ftl' />

<div class="chat-container">
    <div class="chat-messages" id="chatMessages"></div>
    <div class="chat-input">
        <input type="text" id="chatInput" placeholder="Type your message..." />
        <button onclick="sendMessage()">Send</button>
    </div>
</div>

