import com.alibaba.fastjson.JSON;
//import entity.Data;
import com.alibaba.fastjson.JSONException;
import entity.Error;
import org.apache.commons.io.FileUtils;
import org.dom4j.DocumentException;
import utils.LogUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author Xzzz
 * @data 2023/07/18
 */
public class DataCollect {

    //    static List<String >  result =new ArrayList<>();
    private static  String STATE_FILE = "E:\\Java_project\\CodeSmellDatasets1\\src\\test\\files\\state5.txt";
    private static  String JSON_FILE = "E:\\Java_project\\CodeSmellDatasets1\\src\\test\\files\\output5.json";
    private static String git_link="null";
    private static String filePath = "E:\\Java_project\\CodeSmellDatasets1\\src\\test\\files\\opendbc5.json";
    private static String version="null";
    private static int NUM_THREADS = 10;

    private static final Object fileWriteLock = new Object();// 定义一个锁对象用于同步

    public static void main(String[] args) throws DocumentException, IOException {

        if (args.length >= 6) {
            STATE_FILE = args[0];
            JSON_FILE = args[1];
            filePath=args[2];
            git_link=args[3];
            version=args[4];
            NUM_THREADS=Integer.parseInt(args[5]);
        }
        else{
            System.out.println("error:缺少参数");
            System.exit(0);
        }

        List<Error> errors = JSONUtil.getErrorList(JSON_FILE);
        // 设置线程池大小
        ExecutorService executorService = Executors.newFixedThreadPool(NUM_THREADS);

        Path path = Paths.get(STATE_FILE);
        File file = new File(filePath);

        AtomicInteger processedCount = new AtomicInteger(readState()); // 读取上次处理的数量
        StringBuilder json =new StringBuilder();
        String symble ="";

//            FileWriter writer = new FileWriter(filePath, true);
        try  {
            for (int i = processedCount.get(); i < errors.size(); i++) {
                FileWriter writer = new FileWriter(filePath, true);
                final int datasetId = i;
                executorService.execute(() -> {
                    try {
                        String result = processDataset(datasetId, errors.get(datasetId));
                        Data data = new Data();
                        data.setViolated_code(errors.get(datasetId).getContext());
                        data.setRule(errors.get(datasetId).getMsg());
                        data.setGenerated_patch(result);
                        data.setFile(errors.get(datasetId).getFilePath());
                        data.setStart_line(String.valueOf(errors.get(datasetId).getBeginLine()));
                        data.setEnd_line(String.valueOf(errors.get(datasetId).getEndLine()));
                        data.setReal_patch("");
                        data.setProject(GitHubUrlParser.git_process(git_link));
                        data.setLink(git_link);
                        data.setVersion(version);

                        synchronized (fileWriteLock) {
                            String jsonData = JSON.toJSONString(data);

                            if(isJsonFileEmpty(filePath)) {      //如果是空，则加'['
                                jsonData = "[" + jsonData + ",\n";
                            }
                            else if (processedCount.get() >= errors.size()-2){   //倒数的1个
                                jsonData = jsonData + "]";
                            }
                            else{
                                jsonData = jsonData + ",\n";
                            }

//                            if (datasetId == 0) {
//                                jsonData = "[" + jsonData + ",\n";
//                            } else if (datasetId == errors.size() - 1) {
//                                jsonData = jsonData + "]";
//                            } else {
//                                jsonData = jsonData + ",\n";
//                            }
                            writer.write(jsonData); // 追加写入数据

                            // 更新成功处理的数量，并写入状态到文件中
                            processedCount.getAndIncrement();
                            writeState(processedCount.get());

                            // 如果所有数据都已经处理完毕，退出程序
                            if (processedCount.get() >= errors.size()) {
                                System.out.println("所有数据已处理完毕，程序结束。");
                                System.exit(0);
                            }
                            writer.close(); // 关闭文件写入器
                        }
                    } catch (JSONException e) {
                        // 输入太多字符，GPT无法接收，处理下一条数据
                        processedCount.getAndIncrement();
                        writeState(processedCount.get());
                        System.err.println("JSON Exception occurred: " + e.getMessage());
                    } catch (IOException e) {
                        System.err.println("IOException occurred:");
                        e.printStackTrace();
                    }
                });
            }
        } catch (IOException e) {
            System.err.println("IOException occurred:");
            e.printStackTrace();
        }

        executorService.shutdown();
        while (!executorService.isTerminated()) {
            // 等待所有线程完成
        }

    }


    public static boolean isJsonFileEmpty(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line);
            }

            if (jsonContent.length() == 0) {
                // JSON 文件为空
                return true;
            }
            else
                return false;
        } catch (IOException e) {
            // 处理文件读取异常
            e.printStackTrace();
            return false;
        }
    }

    private static String processDataset(int datasetId, Error error)  {
        // 处理数据集合

        String prompt = getPrompt(error);
        LogUtils.logDebug(prompt);
        Generator generator = new Generator();
        String codeFromModle = generator.getCodeFromModle(prompt);
        LogUtils.logDebug(codeFromModle);

        return codeFromModle;
    }

    private static int readState() {
        File file = new File(STATE_FILE);
        if (!file.exists()) {
            return 0;
        }
        try {
            String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            return Integer.parseInt(content.trim());
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static void writeState(int processedCount) {
        File file = new File(STATE_FILE);
        try {
            FileUtils.write(file, String.valueOf(processedCount), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static String getPrompt(Error error){

        return "I want you to act as a code reviewer for C/C++.I will provide a rule from Misra c/c++ and a code snippet that violates that rule," +
                "You just need to return the refactoring code as plain text and without any explanation."+
                "Please help me refactor the snippet on the given code according to the Misra rule : " +error.getMsg()+
                "\nThe Code snippets："+error.getContext();

//                "你的任务是根据MISRA规范对下面代码进行重构，" +
//                "" +
//
//
//                "\n返回的结果中只需要出现重构后的代码，非代码部分必须以JAVA注释 // 的方式出现";
    }

}

