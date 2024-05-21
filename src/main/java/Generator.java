import com.plexpt.chatgpt.ChatGPT;
import com.plexpt.chatgpt.entity.chat.ChatCompletion;
import com.plexpt.chatgpt.entity.chat.ChatCompletionResponse;
import com.plexpt.chatgpt.entity.chat.Message;
import com.plexpt.chatgpt.util.Proxys;

import java.net.Proxy;
import java.util.Arrays;
import java.util.List;

public class Generator {

    private static final List<String> API_KEY_LIST=Arrays.asList(
//           "sk-zw6Cw4E8AS32Zkgj8c1714698e1a40F7Bb94Fc9f1a8493Fa",
            "sk-zxBfsB7T2LE44vUt3cC6E05dCfC84bE7Ba6c98E0930eCfEc"


                                                            );
    private static final String API_HOST="https://openkey.cloud";


    private static final String SYSTEM="You are a professional c/c++ code reviewer";





    public String  getCodeFromModle(String code){
        //国内需要代理 国外不需要

//        Proxy proxy = Proxys.http("127.0.0.1", 1080);

        ChatGPT chatGPT = ChatGPT.builder()
                .apiKeyList(API_KEY_LIST)
//                .proxy(proxy)
                .timeout(900)
                .apiHost(API_HOST) //反向代理地址
                .build()
                .init();

        Message system = getSystem();
        Message message = getMessage(code);

        ChatCompletion chatCompletion = ChatCompletion.builder()
                .model(ChatCompletion.Model.GPT_3_5_TURBO_16K.getName())
                .messages(Arrays.asList(system, message))
                .temperature(0)
                .presencePenalty(0)
                .frequencyPenalty(0)
                .build();

        ChatCompletionResponse response = chatGPT.chatCompletion(chatCompletion);
        Message res = response.getChoices().get(0).getMessage();
        return res.getContent();
    }

    private Message getMessage(String code) {
        return Message.of(code);
    }

    private  Message getSystem() {
         return Message.ofSystem(SYSTEM);
    }



}
