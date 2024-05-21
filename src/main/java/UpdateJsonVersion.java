import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;

public class UpdateJsonVersion {
    public static void main(String[] args) {
//        if (args.length < 1) {
//            System.out.println("Usage: java UpdateJsonVersion <jsonFilePath>");
//            return;
//        }

//        String jsonFilePath = args[0];
        String jsonFilePath = "E:\\Java_project\\Test\\src\\files\\simbody\\simbody.json";

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(new File(jsonFilePath));

            if (rootNode.isArray()) {
                for (JsonNode jsonNode : rootNode) {
                    if (jsonNode instanceof ObjectNode) {
                        ObjectNode objectNode = (ObjectNode) jsonNode;
                        // 在这里进行你想要的操作，例如更新 version 字段
                        objectNode.put("version", "Simbody-3.6-210-g252dfb59");
                    }
                }

                objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(jsonFilePath), rootNode);

                System.out.println("Version field updated successfully.");
            } else {
                System.out.println("Root node is not an array.");
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    }
