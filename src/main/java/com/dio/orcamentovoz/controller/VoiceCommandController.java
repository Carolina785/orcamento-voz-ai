package com.dio.orcamentovoz.controller;

import com.dio.orcamentovoz.ai.FinanceTools;
import com.dio.orcamentovoz.dto.TextCommandRequest;
import com.dio.orcamentovoz.dto.VoiceCommandResponse;
import jakarta.validation.Valid;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Fluxo principal da API:
 * audio -> texto (transcricao) -> IA entende a intencao -> tool calling
 * executa uma funcao real -> resposta final para a pessoa usuaria.
 */
@RestController
@RequestMapping("/api/voice-commands")
public class VoiceCommandController {

    private static final String SYSTEM_PROMPT = """
            Voce e um assistente financeiro pessoal. Interprete o comando do usuario
            e utilize as ferramentas disponiveis para criar transacoes ou consultar
            informacoes financeiras. Responda sempre de forma curta, clara e em portugues.
            """;

    private final OpenAiAudioTranscriptionModel transcriptionModel;
    private final ChatClient chatClient;

    public VoiceCommandController(OpenAiAudioTranscriptionModel transcriptionModel,
                                   ChatClient.Builder chatClientBuilder,
                                   FinanceTools financeTools) {
        this.transcriptionModel = transcriptionModel;
        this.chatClient = chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultTools(financeTools)
                .build();
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<VoiceCommandResponse> processarAudio(@RequestParam("audio") MultipartFile audio) throws IOException {
        ByteArrayResource audioResource = new ByteArrayResource(audio.getBytes()) {
            @Override
            public String getFilename() {
                return audio.getOriginalFilename() != null ? audio.getOriginalFilename() : "audio.mp3";
            }
        };

        AudioTranscriptionResponse transcriptionResponse =
                transcriptionModel.call(new AudioTranscriptionPrompt(audioResource));
        String textoTranscrito = transcriptionResponse.getResult().getOutput();

        String respostaIA = executar(textoTranscrito);

        return ResponseEntity.ok(new VoiceCommandResponse(textoTranscrito, respostaIA));
    }

    // Endpoint auxiliar: permite testar o mesmo fluxo de IA sem precisar gravar audio.
    @PostMapping(value = "/texto", consumes = "application/json")
    public ResponseEntity<VoiceCommandResponse> processarTexto(@Valid @RequestBody TextCommandRequest request) {
        String respostaIA = executar(request.comando());
        return ResponseEntity.ok(new VoiceCommandResponse(request.comando(), respostaIA));
    }

    private String executar(String comando) {
        return chatClient.prompt()
                .user(comando)
                .call()
                .content();
    }
}
