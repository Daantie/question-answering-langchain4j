package eu.luminis.faqlangchain.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.inprocess.InProcessEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.store.embedding.weaviate.WeaviateEmbeddingStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static dev.langchain4j.model.inprocess.InProcessEmbeddingModelType.*;
import static dev.langchain4j.model.openai.OpenAiModelName.*;
import static java.time.Duration.*;

@Configuration
public class QuestionAnsweringConfig {

    @Value("${openai.apiKey}")
    private String openaiApiKey;

    @Qualifier("openaiModel")
    @Bean
    public EmbeddingModel openaiEmbeddingModel() {
        return OpenAiEmbeddingModel.builder()
                .apiKey(openaiApiKey)
                .modelName(TEXT_EMBEDDING_ADA_002)
                .build();
    }

    @Qualifier("inMemoryModel")
    @Bean
    public EmbeddingModel inMemoryEmbeddingModel() {
        return new InProcessEmbeddingModel(ALL_MINILM_L6_V2);
    }

    @Qualifier("openaiChatModel")
    @Bean
    public ChatLanguageModel openaiChatModel() {
        return OpenAiChatModel.builder()
                .apiKey(openaiApiKey)
                .modelName(GPT_3_5_TURBO)
                .temperature(0.7)
                .timeout(ofSeconds(15))
                .maxRetries(3)
                .logResponses(true)
                .logRequests(true)
                .build();
    }

    @Qualifier("inMemoryEmbeddingStore")
    @Bean
    public EmbeddingStore<TextSegment> inMemoryEmbeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }

    @Qualifier("weaviateEmbeddingStore")
    @Bean
    public EmbeddingStore<TextSegment> weaviateEmbeddingStore(@Value("${weaviate.apiKey}") String apiKey,
                                                              @Value("${weaviate.host}") String host) {
        return WeaviateEmbeddingStore.builder()
                .apiKey(apiKey)
                .scheme("https")
                .host(host)
                .build();
    }
}
