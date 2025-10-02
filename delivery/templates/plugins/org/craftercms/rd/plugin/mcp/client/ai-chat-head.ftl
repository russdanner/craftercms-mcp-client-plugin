<#import "/templates/system/common/crafter.ftl" as crafter />

<style>
    html, body {
        color: #333;
        height: 100%;
        background: linear-gradient(135deg, #ece9e6, #ffffff);
        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
    }

    main {
        max-width: 800px;
        padding: 40px;
        background: rgba(255, 255, 255, 0.4);
        backdrop-filter: blur(12px);
        border-radius: 20px;
        margin: 80px auto;
        box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08);
    }

    .chat-container {
        margin-top: 20px;
        background: rgba(255, 255, 255, 0.7);
        border-radius: 16px;
        padding: 20px;
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
    }

    .chat-messages {
        max-height: 400px;
        overflow-y: auto;
        margin-bottom: 15px;
        padding: 15px;
        border-radius: 10px;
        background: rgba(255, 255, 255, 0.9);
    }

    .chat-message {
        margin: 8px 0;
        padding: 12px 16px;
        border-radius: 12px;
        line-height: 1.4;
        font-size: 15px;
        animation: fadeIn 0.3s ease;
    }

    .chat-message.user {
        background: #007bff;
        color: white;
        text-align: right;
        align-self: flex-end;
    }

    .chat-message.bot {
        background: #f1f3f5;
        text-align: left;
        color: #333;
    }

    .chat-input {
        display: flex;
        gap: 10px;
    }

    .chat-input input {
        flex: 1;
        padding: 12px 14px;
        border: 1px solid #ddd;
        border-radius: 10px;
        font-size: 15px;
        outline: none;
        transition: border 0.2s;
    }

    .chat-input input:focus {
        border-color: #007bff;
    }

    .chat-input button {
        padding: 12px 20px;
        border: none;
        background: #007bff;
        color: white;
        border-radius: 10px;
        cursor: pointer;
        font-size: 15px;
        transition: background 0.2s, transform 0.1s;
    }

    .chat-input button:hover {
        background: #0056b3;
        transform: translateY(-1px);
    }

    /* Typing indicator animation */
    .typing-indicator {
        display: inline-flex;
        gap: 4px;
        margin: 5px 0;
    }

    .typing-indicator span {
        width: 8px;
        height: 8px;
        background: #555;
        border-radius: 50%;
        display: inline-block;
        animation: bounce 1.4s infinite ease-in-out;
    }

    .typing-indicator span:nth-child(1) {
        animation-delay: 0s;
    }
    .typing-indicator span:nth-child(2) {
        animation-delay: 0.2s;
    }
    .typing-indicator span:nth-child(3) {
        animation-delay: 0.4s;
    }

    @keyframes bounce {
        0%, 80%, 100% { transform: scale(0.6); }
        40% { transform: scale(1); }
    }

    @keyframes fadeIn {
        from { opacity: 0; transform: translateY(5px); }
        to { opacity: 1; transform: translateY(0); }
    }
</style>

<script>
    async function sendMessage() {
        const input = document.getElementById('chatInput');
        const messages = document.getElementById('chatMessages');
        const message = input.value.trim();

        if (!message) return;

        // Display user message
        const userMessage = document.createElement('div');
        userMessage.className = 'chat-message user';
        userMessage.textContent = message;
        messages.appendChild(userMessage);
        input.value = '';
        messages.scrollTop = messages.scrollHeight;

        // Show typing indicator
        const typingMessage = document.createElement('div');
        typingMessage.className = 'chat-message bot';
        typingMessage.innerHTML = `
            <div class="typing-indicator">
                <span></span><span></span><span></span>
            </div>`;
        messages.appendChild(typingMessage);
        messages.scrollTop = messages.scrollHeight;

        try {
            const response = await fetch('/api/plugins/org/craftercms/rd/plugin/mcp/client/chat/complete?crafterSite=${siteContext.siteName}', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ message }),
            });

            if (!response.ok) throw new Error('Chat service error');

            const data = await response.json();
            messages.removeChild(typingMessage); // remove typing indicator

            const botMessage = document.createElement('div');
            botMessage.className = 'chat-message bot';
            botMessage.textContent = data.response || 'No response from chat service';
            messages.appendChild(botMessage);
            messages.scrollTop = messages.scrollHeight;
        } catch (error) {
            messages.removeChild(typingMessage);
            const errorMessage = document.createElement('div');
            errorMessage.className = 'chat-message bot';
            errorMessage.textContent = 'Error: ' + error.message;
            messages.appendChild(errorMessage);
            messages.scrollTop = messages.scrollHeight;
        }
    }

    // Allow sending with Enter key
    document.getElementById('chatInput').addEventListener('keypress', (e) => {
        if (e.key === 'Enter') sendMessage();
    });
</script>
