import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.github.cdimascio.dotenv.Dotenv;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class nsbot extends ListenerAdapter {

    private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

    private static final String DISCORD_TOKEN = dotenv.get("DISCORD_TOKEN");
    private static final String DEEPL_API_KEY = dotenv.get("DEEPL_API_KEY");


    private static final String DEEPL_URL = "https://api-free.deepl.com/v2/translate";

    public static void main(String[] args) {
        try {
            JDABuilder.createDefault(DISCORD_TOKEN)
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                    .addEventListeners(new nsbot())
                    .build();
            System.out.println("✅ 봇이 정상적으로 실행되었습니다!");
        } catch (Exception e) {
            System.out.println("❌ 실행 중 오류 발생! 토큰을 확인하세요.");
            e.printStackTrace();
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        String message = event.getMessage().getContentRaw();

        // 1. [한 -> 일]
        if (message.startsWith("!81")) {
            String originalText = message.substring(4);
            translateAndReply(event, originalText, "JA", "🇯🇵");
        }
        // 2. [일 -> 한]
        else if (message.startsWith("!82")) {
            String originalText = message.substring(4);
            translateAndReply(event, originalText, "KO", "🇰🇷");
        }
    }


    private void translateAndReply(MessageReceivedEvent event, String text, String targetLang, String flagEmoji) {
        event.getChannel().sendMessage("🔄 번역 중...").queue(responseMsg -> {

            // API 호출
            String translatedText = callDeepL(text, targetLang);

            if (translatedText != null) {
                // 성공 시
                responseMsg.editMessage(flagEmoji + ": " + translatedText).queue();
            } else {
                // 실패 시
                responseMsg.editMessage("❌ 번역 실패! API 키나 한도를 확인해주세요.").queue();
            }
        });
    }

    // DeepL API 통신 함수
    private String callDeepL(String text, String targetLang) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String encodedText = java.net.URLEncoder.encode(text, StandardCharsets.UTF_8);


            String requestBody = "text=" + encodedText + "&target_lang=" + targetLang;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(DEEPL_URL))

                    .header("Authorization", "DeepL-Auth-Key " + DEEPL_API_KEY)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();
                return jsonObject.getAsJsonArray("translations")
                        .get(0).getAsJsonObject()
                        .get("text").getAsString();
            } else {
                System.out.println("🚨 API 오류 발생: " + response.statusCode());
                System.out.println("메시지: " + response.body());
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}