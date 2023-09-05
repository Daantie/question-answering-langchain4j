package eu.luminis.faqlangchain.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class QuestionAnswerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(QuestionAnswerService.class);
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;
    private final ChatLanguageModel chatLanguageModel;
    private final PromptTemplate promptTemplate;

    public QuestionAnswerService(@Qualifier("openaiModel") EmbeddingModel embeddingModel,
                                 @Qualifier("openaiChatModel") ChatLanguageModel chatLanguageModel,
                                 @Qualifier("inMemoryEmbeddingStore") EmbeddingStore<TextSegment> embeddingStore) {
        this.embeddingModel = embeddingModel;
        this.chatLanguageModel = chatLanguageModel;
        this.embeddingStore = embeddingStore;

        this.promptTemplate = PromptTemplate.from(
                "Answer the following question to the best of your abilities: \"{{question}}\"\n\n" +
                        "Base your answer on the following information:\n{{information}}");
    }

    public String askQuestion(final String question) {
        Embedding queryEmbedding = embeddingModel.embed(question);
        List<EmbeddingMatch<TextSegment>> relevant = embeddingStore.findRelevant(queryEmbedding, 4, 0.8);

        Map<String, Object> variables = new HashMap<>();
        variables.put("question", question);
        variables.put("information", relevant.stream().map(match -> match.embedded().text()).collect(Collectors.joining("\n\n")));
        Prompt prompt = promptTemplate.apply(variables);

        LOGGER.info("Sending following prompt to LLM:\n{}", prompt.text());

        AiMessage aiMessage = chatLanguageModel.sendUserMessage(prompt.toUserMessage());
        return aiMessage.text();
    }

}
