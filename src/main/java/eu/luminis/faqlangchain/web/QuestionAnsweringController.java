package eu.luminis.faqlangchain.web;

import eu.luminis.faqlangchain.service.QuestionAnswerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/qa")
public class QuestionAnsweringController {
    private final QuestionAnswerService questionAnswerService;

    public QuestionAnsweringController(QuestionAnswerService questionAnswerService) {
        this.questionAnswerService = questionAnswerService;
    }

    @GetMapping("/ask")
    public ResponseEntity<ChatResponse> ask(@RequestBody ChatRequest request) {
        String answer = questionAnswerService.askQuestion(request.question());
        return ResponseEntity.ok(new ChatResponse(answer));
    }
}
