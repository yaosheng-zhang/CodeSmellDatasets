import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;

public class Add_Version {

    public static void main(String[] args) {
        String jsonFilePath = "E:\\Java_project\\Test\\src\\files\\openpilot\\openpilot.json";  // 指定 JSON 文件的路径

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(new File(jsonFilePath));

            // 遍历每个对象并添加 version 字段
            for (JsonNode node : rootNode) {
                if (node.has("start_line")) {
                    String startLine = node.get("start_line").asText();
                    String version = "v0.7.2-10096-gf5071411c";
                    ((ObjectNode) node).put("version", version);
                }
            }

            // 将更新后的 JSON 数据写回文件
            objectMapper.writeValue(new File(jsonFilePath), rootNode);
            System.out.println("JSON file updated successfully.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
