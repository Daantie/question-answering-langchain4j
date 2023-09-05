package eu.luminis.faqlangchain.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.Duration;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class IngestService {
    private static final Logger LOGGER = LoggerFactory.getLogger(IngestService.class);
    private final WebClient webClient;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;

    public IngestService(@Value("${unstructured.apiKey}") String unstructuredApiKey,
                         @Qualifier("openaiModel") EmbeddingModel embeddingModel,
                         @Qualifier("inMemoryEmbeddingStore") EmbeddingStore<TextSegment> embeddingStore) {
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
        this.webClient = WebClient.builder()
                .baseUrl("https://api.unstructured.io/general/v0/")
                .defaultHeader("unstructured-api-key", unstructuredApiKey)
                .build();
    }

    public boolean ingestPDF() throws FileNotFoundException {
        LOGGER.info("Ingesting PDF");
        File file = ResourceUtils.getFile("classpath:data/faq.pdf");
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("files", new FileSystemResource(file));
        builder.part("strategy", "ocr_only");
        builder.part("ocr_languages", "eng");

        Mono<Object> mono = webClient.post()
                .uri("general")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchangeToMono(response -> {
                    if (response.statusCode().equals(HttpStatus.OK)) {
                        return response.bodyToMono(UnstructuredResponse[].class);
                    } else {
                        LOGGER.error("Something went wrong when uploading file to Unstructured API. Received status code {}", response.statusCode());
                        return response.bodyToMono(JsonNode.class);
                    }
                });

        Object response = mono.block(Duration.ofMinutes(1));
        if (response instanceof JsonNode jsonNode) {
            LOGGER.error("Response: {}", jsonNode);
            return false;
        }
        if (response instanceof UnstructuredResponse[] unstructuredResponses) {
            String text = Arrays.stream(unstructuredResponses).map(UnstructuredResponse::getText).collect(Collectors.joining(" "));

            Document document = Document.from(text);
            DocumentSplitter documentSplitter = DocumentSplitters.recursive(300);
            EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                    .documentSplitter(documentSplitter)
                    .embeddingModel(embeddingModel)
                    .embeddingStore(embeddingStore)
                    .build();
            ingestor.ingest(document);
            LOGGER.info("Ingestion of PDF finished");
            return true;
        }
        return false;
    }
}
