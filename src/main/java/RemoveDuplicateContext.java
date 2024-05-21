import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class RemoveDuplicateContext {

    public static void main(String[] args) {
        String jsonFilePath = "E:\\Java_project\\Test\\src\\files\\simbody\\simbody.json";

//        if (args.length >= 1) {
//            jsonFilePath = args[0];
//        }
//        else{
//            System.out.println("Usage: java RemoveDuplicateContext <jsonFilePath>");
//            return;
//        }


        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // 读取 JSON 文件
            JsonNode rootNode = objectMapper.readTree(new File(jsonFilePath));

            if (rootNode.isArray()) {
                ArrayNode jsonArray = (ArrayNode) rootNode;
                removeDuplicateContext(jsonArray);

                // 将修改后的 JSON 数据写回文件
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(jsonFilePath), jsonArray);
                System.out.println("Duplicate contexts removed and saved to file.");
            } else {
                System.out.println("Input JSON is not an array.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void removeDuplicateContext(ArrayNode jsonArray) {
        // 用于存储已经出现的 context
        Set<String> contextSet = new HashSet<>();

        for (int i = jsonArray.size() - 1; i >= 0; i--) {
            JsonNode jsonNode = jsonArray.get(i);
            if (jsonNode.has("violated_code")) {   //可以修改context为其他字段
                String context = jsonNode.get("violated_code").asText();
                if (contextSet.contains(context)) {
                    jsonArray.remove(i); // 删除重复的数据
                } else {
                    contextSet.add(context);
                }
            }
        }
    }
}
