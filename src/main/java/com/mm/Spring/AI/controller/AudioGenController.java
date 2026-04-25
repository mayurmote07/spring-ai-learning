package com.mm.Spring.AI.controller;

import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.tts.TextToSpeechOptions;
import org.springframework.ai.audio.tts.TextToSpeechPrompt;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class AudioGenController {

    private final OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel;

    private final OpenAiAudioSpeechModel openAiAudioSpeechModel;

    public AudioGenController(OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel, OpenAiAudioSpeechModel openAiAudioSpeechModel) {
        this.openAiAudioTranscriptionModel = openAiAudioTranscriptionModel;
        this.openAiAudioSpeechModel = openAiAudioSpeechModel;
    }

     /*
     * Converts uploaded audio files to text using OpenAI's Whisper model.
     * Supports multiple audio formats (MP3, WAV, M4A, FLAC, etc.) and can transcribe in multiple languages.
     *
     * This endpoint accepts an audio file, sends it to the OpenAI Whisper API with specified options
     * (language, response format), and returns the transcribed text in the requested format.
     *
     * Key configuration:
     *   - Language: Set to Spanish ("es") — adjust as needed for other languages
     *   - Response format: SRT (SubRip subtitle format) — shows timing information
     *   Alternative formats: JSON, TEXT, VTT (WebVTT), etc.
     *
     * @param speech  the audio file to transcribe (multipart form data)
     * @return        transcribed text in the specified response format
     */
    @PostMapping("/api/stt")
    public String speechToText(@RequestParam MultipartFile speech) {

//        return openAiAudioTranscriptionModel.call(speech.getResource());

        OpenAiAudioTranscriptionOptions options = OpenAiAudioTranscriptionOptions.builder()
                .language("es")
                .responseFormat(OpenAiAudioApi.TranscriptResponseFormat.SRT)
                .build();

        AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(speech.getResource(), options);

        return openAiAudioTranscriptionModel.call(prompt).getResult().getOutput();

    }

    /*
     * Converts input text to natural-sounding speech audio using OpenAI's Text-to-Speech (TTS) model.
     * Generates high-quality audio output in various voice options with configurable speech speed.
     *
     * This endpoint accepts plain text, synthesizes it into audio using the specified voice options,
     * and returns the audio data as a byte array suitable for streaming or downloading.
     *
     * Key configuration:
     *   - Voice: "alloy" — one of the available voices (alloy, echo, fable, onyx, nova, shimmer)
     *   - Speed: 1.5 — playback speed multiplier (0.25 to 4.0), higher values mean faster speech
     *   - Alternative voices offer different tones and characteristics for various use cases
     *
     * @param text  the input text to convert to speech
     * @return      byte array containing the audio file data (can be streamed or saved as MP3)
     */
    @PostMapping("/api/tts")
    public byte[] textToSpeech(@RequestParam String text) {

        TextToSpeechOptions options = TextToSpeechOptions.builder()
                .voice("alloy")
                .speed(1.5)
                .build();

        TextToSpeechPrompt prompt = new TextToSpeechPrompt(text, options);

         return openAiAudioSpeechModel.call(prompt).getResult().getOutput();
    }

}
