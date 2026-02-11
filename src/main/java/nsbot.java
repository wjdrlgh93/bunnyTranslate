import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.Command.Type;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
       if (DISCORD_TOKEN == null || DEEPL_API_KEY == null) {
            System.err.println("❌ 에러: .env 파일이 없거나 키가 설정되지 않았습니다!");
            return;
        }

        try {
           JDA jda = JDABuilder.createDefault(DISCORD_TOKEN)
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                    .addEventListeners(new nsbot())
                    .build();
           jda.awaitReady();
           jda.updateCommands().addCommands(
                    Commands.context(Command.Type.MESSAGE, "한국어 -> 日本語"),
                    Commands.context(Command.Type.MESSAGE, "日本語 -> 한국어"),
                    Commands.context(Command.Type.MESSAGE, "日本語 -> English")
            ).queue();

            System.out.println("✅ 봇이 정상적으로 실행되었습니다! (우클릭 메뉴 등록 완료)");

        } catch (InterruptedException e) {
            System.out.println("❌ 실행 중 인터럽트 발생!");
            e.printStackTrace();
        }
    }

   @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        String message = event.getMessage().getContentRaw();
       if (message.startsWith("!81")) {
            String originalText = message.substring(4); // "!81 " 뒤의 글자만 가져오기
            translateAndReply(event, originalText, "JA", "🇯🇵");
        }
       else if (message.startsWith("!82")) {
            String originalText = message.substring(4); // "!82 " 뒤의 글자만 가져오기
            translateAndReply(event, originalText, "KO", "🇰🇷");
        }
    }
  @Override
    public void onMessageContextInteraction(MessageContextInteractionEvent event) {
       String targetMessage = event.getTarget().getContentRaw();

        if (targetMessage.isEmpty()) {
            event.reply("❌ 번역할 텍스트가 없습니다!").setEphemeral(true).queue();
            return;
        }

        String targetLang;
        String flag;
       if (event.getName().equals("한국어 -> 日本語")) {
            targetLang = "JA";
            flag = "🇯🇵";
        } else if (event.getName().equals("日本語 -> 한국어")) {
            targetLang = "KO";
            flag = "🇰🇷";
        } else if (event.getName().equals("日本語 -> English")) {
           targetLang = "EN-US"; flag = "🇺🇸"; // ⬅️ 영어 로직 추가
        } else {
            return; // 등록되지 않은 메뉴면 무시
        }
      // 지연 응답 (기존과 동일하게 모두에게 보임)
       event.deferReply().queue();

        String translatedText = callDeepL(targetMessage, targetLang);

        if (translatedText != null) {
            event.getHook().sendMessage(flag + " 번역 결과:\n" + translatedText).queue();
        } else {
            event.getHook().sendMessage("❌ 번역 실패! 관리자에게 문의하세요.").queue();
        }
    }
  private void translateAndReply(MessageReceivedEvent event, String text, String targetLang, String flagEmoji) {
        event.getChannel().sendMessage("🔄 번역 중...").queue(responseMsg -> {
            String translatedText = callDeepL(text, targetLang);
            if (translatedText != null) {
                responseMsg.editMessage(flagEmoji + ": " + translatedText).queue();
            } else {
                responseMsg.editMessage("❌ 번역 실패! API 키나 한도를 확인해주세요.").queue();
            }
        });
    }
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