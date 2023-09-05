# Question Answering with Langchain4j

## How to start
1. Change the qualifiers in `IngestService` and `QuestionAnswerService` to the models and stores of your liking. The currently configured beans for models and stores can be found in `QuestionAnsweringConfig`.
2. Make sure your API keys and other configuration is correct in `application.yaml`.
3. Run the application.
4. Use the  `http://localhost:8080/ingest/pdf` endpoint to ingest the `faq.pdf` PDF file. This is the Devoxx Belgium FAQ page. This makes use of the Unstructured.io API to convert the PDF to text.
5. Use the `http://localhost:8080/qa/ask` endpoint to ask a question and get an answer. See payload below.

Example question payload:
```json
{
    "question": "What is the address of the venue?"
}
```
Example response:
```json
{
    "answer": "The address of the venue is Groenendaallaan 394, 2030 Antwerp, Belgium."
}
```
