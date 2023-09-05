package eu.luminis.faqlangchain.web;

import java.io.IOException;

import eu.luminis.faqlangchain.service.IngestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ingest")
public class IngestController {
    private final IngestService ingestService;

    public IngestController(IngestService ingestService) {
        this.ingestService = ingestService;
    }

    @GetMapping("/pdf")
    public ResponseEntity<HttpStatus> ingestPDF() throws IOException {
        if (this.ingestService.ingestPDF()) {
            return ResponseEntity.ok(HttpStatus.OK);
        }
        return ResponseEntity.internalServerError().build();
    }
}
