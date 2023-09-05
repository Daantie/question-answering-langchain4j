package eu.luminis.faqlangchain.service;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.openai.OpenAiChatModel;
import eu.luminis.faqlangchain.web.ChatRequest;
import eu.luminis.faqlangchain.web.ChatResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ChatService {
    private final OpenAiChatModel model;

    public ChatService(@Value("${openai.apiKey}") String openaiApiKey) {
        this.model = OpenAiChatModel.withApiKey(openaiApiKey);
    }

    public ChatResponse chat(ChatRequest chatRequest) {
        AiMessage answer = model.sendUserMessage(chatRequest.question());
        return new ChatResponse(answer.text());
    }
}
