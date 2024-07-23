package sim.explainer.library.service;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import sim.explainer.library.exception.ErrorCode;
import sim.explainer.library.exception.JSimPiException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExplanationConverterService {
    private static OpenAiService openAiService = null;
    private static int apiTimeout = 45;

    private static String apiKey;

    private static final String GPT_MODEL = "gpt-4o-mini";

    private static final String SYSTEM_TASK_MESSAGE = """
            You are an explainer master. Everything you say will be easy to understand for everyone.
            Don't say anything else. Respond only with the content in String format with no code block.
                        
            The user will provide an explanation in JSON format, which consists of the following keys:
            - comparingConcept1: The first concept name to compare
            - comparingConcept2: The second concept name to compare
            - deg: The similarity degree.
            - pri: A set of pairs of primitives between comparingConcept1's primitive concepts and comparingConcept2's primitive concepts that derives "deg".
            - exi: A set of pairs of existentials between comparingConcept1's primitive existentials and comparingConcept2's primitive existentials that derives "deg".
            - emb: A map of pairs that create the set of pairs of embeddings in the pre-trained word embeddings vector space that derive "deg".
                        
            Respond only with the explanation in String format, 
            which is an explanation in an easy-to-understand form but keeping the names as they are.
            don't omit any names of anything in 'pri', 'exi', 'emb', explain everything.
                        
            Don't add anything else in the end after you respond with explanation.
                        
            input:
            {
                "pri": ["(Place, Place)"],
                "deg": 0.8259457964,
                "exi": [
                    "(some canSail Kayaking, some canWalk Trekking)",
                    "(some canWalk Trekking, some canWalk Trekking)"
                ],
                "comparingConcept2": "Mangrove",
                "emb": {"(some canSail Kayaking, some canWalk Trekking)": [{
                    "first": "canTravelWithSail",
                    "second": "canMoveWithLegs"
                }]},
                "comparingConcept1": "ActivePlace"
            }
            
            reason:
            because it compares the primitive concept 'Place' with 'Place' so the primitive concept is the same, which increases the similarity degree.
            Additionally, the comparison of the existentials 'some canWalk Trekking' with 'some canWalk Trekking' further increases the similarity degree.
            For 'some canSail Kayaking' and 'some canWalk Trekking', since they are different, the embeddings are examined.
            The embeddings show a similarity between 'canTravelWithSail' for 'Kayaking' and 'canMoveWithLegs' for 'Trekking', which also contributes to the higher similarity degree.
            
            output:
            The comparison between 'ActivePlace' and 'Mangrove' results in a similarity degree of 0.8259. Both are recognized as 'Place' that allow 'can walk with Trekking' and 'can sail with Kayaking'. The embedding comparison highlights a similar functional basis between 'canTravelWithSail' for 'Kayaking' and 'canMoveWithLegs' for 'Trekking', reinforcing their comparability.
                        
            input:
            {
                "pri": ["(Kayaking, Trekking)"],
                "deg": 0.97014,
                "exi": [],
                "comparingConcept2": "Trekking",
                "emb": {"(Kayaking, Trekking)": [{
                    "first": "Kayaking",
                    "second": "Trekking"
                }]},
                "comparingConcept1": "Kayaking"
            }
                        
            reason:
            because it compares the primitive concept 'Kayaking' with 'Trekking', which are different. Therefore, the embeddings are examined.
            The embeddings show a similarity between 'Kayaking' for 'Kayaking' and 'Trekking' for 'Trekking', which results in a higher similarity degree.
                        
            output:
            The comparison between 'Kayaking' and 'Trekking' results in a similarity degree of 0.9701. Both activities share a primitive concept and are similar in the embedding vector space, highlighting a similar functional basis between 'Kayaking' and 'Trekking', which reinforces their comparability.
                        
            input:
            {
                "pri": ["(Trekking, Trekking)"],
                "deg": 1,
                "exi": [],
                "comparingConcept2": "Trekking",
                "emb": {},
                "comparingConcept1": "Trekking"
            }
                        
            reason:
            because it compares the primitive concept 'Trekking' with 'Trekking', so the primitive concept is the same, resulting in the highest possible similarity degree.
                        
            output:
            Trekking compared with itself naturally results in the highest similarity degree possible, 1.0, indicating complete identity between the concepts.
            """;

    public ExplanationConverterService() {
    }

    public ExplanationConverterService(String apiKey) {
        this.apiKey = apiKey;

        openAiService = new OpenAiService(apiKey, Duration.ofSeconds(this.apiTimeout));
        System.out.println("Connected to OpenAI!");
    }

    public void setApiTimeout(int apiTimeout) {
        ExplanationConverterService.apiTimeout = apiTimeout;

        openAiService = new OpenAiService(apiKey, Duration.ofSeconds(this.apiTimeout));
        System.out.println("Connected to OpenAI!");
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;

        openAiService = new OpenAiService(apiKey, Duration.ofSeconds(this.apiTimeout));
        System.out.println("Connected to OpenAI!");
    }

    public static JSONObject convertExplanation(JSONObject explanation) {
        JSONObject result = new JSONObject();

        result.put("comparingConcept1", explanation.getString("comparingConcept1"));
        result.put("comparingConcept2", explanation.getString("comparingConcept2"));
        result.put("deg", explanation.getBigDecimal("deg"));
        result.put("pri", explanation.getJSONArray("pri"));
        result.put("exi", explanation.getJSONArray("exi"));
        result.put("emb", explanation.getJSONObject("emb"));

        String response = sendMessage(result.toString(2));

        result.put("explanation", response);

        ArrayList<JSONObject> children_explanation = new ArrayList<>();
        for (Object child : explanation.getJSONArray("children")) {
            JSONObject result_child = convertExplanation((JSONObject) child);
            children_explanation.add(result_child);
        }

        result.put("children", children_explanation);

        return result;
    }

    private static String sendMessage(String message) {
        if (openAiService == null) {
            throw new JSimPiException("Please Provide an OpenAI API Key.", ErrorCode.ExplanationConverterService_NoConfiguration);
        }

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model(GPT_MODEL)
                .temperature(0.8)
                .messages(
                        List.of(
                                new ChatMessage("system", SYSTEM_TASK_MESSAGE),
                                new ChatMessage("user", message)))
                .build();

        StringBuilder builder = new StringBuilder();

        openAiService.createChatCompletion(chatCompletionRequest).getChoices().forEach(choice -> {
            builder.append(choice.getMessage().getContent());
        });

        return builder.toString();
    }
}
