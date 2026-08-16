package com.dio.orcamentovoz.dto;

/**
 * @param textoTranscrito     o que o Whisper entendeu do audio enviado
 * @param respostaAssistente  a resposta final gerada pela IA, ja depois de executar as tools
 */
public record VoiceCommandResponse(String textoTranscrito, String respostaAssistente) {
}
