import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import entity.Error;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JSONUtil {
    //线程数
    private static int NUM_THREADS = 12;

    public static void main(String[] args) {

        String newXml = "E:\\Java_project\\idea_test\\modified_files\\modified_file6.xml";
        String output = "E:\\Java_project\\CodeSmellDatasets1\\src\\test\\test_files\\test6_1.json";


        if (args.length >= 3) {
            newXml = args[0];
            output = args[1];
            NUM_THREADS= Integer.parseInt(args[2]);
        }
        else{
            System.out.println("参数不够！");
            return;
        }

        processConcurrently(newXml, output);

    }

    public static void processConcurrently(String xmlPath, String output) {
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

        try {
            Set<Error> concurrentErrors = ConcurrentHashMap.newKeySet();
            Document parse = parse(new File(xmlPath));
            Element rootElement = parse.getRootElement();
            List<Element> errors = rootElement.element("errors").elements();
            Pattern pattern = Pattern.compile("^Rule.*");

            for (Element element : errors) {
                String msg = element.attributeValue("msg");
                Matcher matcher = pattern.matcher(msg);
                if (matcher.matches()) {
                    executor.submit(() -> {
                        Error error = new Error();
                        error.setMsg(msg);

//                        System.out.println(msg);

                        Element location = element.element("location");
                        String fileAdr = location.attribute("file").getValue();
                        error.setFilePath(fileAdr);

//                        System.out.println(fileAdr);

                        Integer line = Integer.valueOf(location.attribute("line").getValue());
                        error.setLine(line);

//                        System.out.println(line);
                        System.out.println(error.getFilePath());

                        if (isCFile(error.getFilePath())){

                            ExtractCodeBlockForC extractCodeBlock = new ExtractCodeBlockForC(error.getFilePath());
                            String extractCodeBlockForC = extractCodeBlock.getCodeBlockByAst(error.getLine());
                            if ("200".equals(extractCodeBlockForC))
                            {
                                String code = extractCodeBlock.getCode();
                                Integer startLine = extractCodeBlock.getStartLine();
                                Integer endLine = extractCodeBlock.getEndLine();
                                error.setContext(code);
                                error.setBeginLine(startLine);
                                error.setEndLine(endLine);

                            }else {
                                try {
                                    String code =getAcode(fileAdr,line);
                                    error.setContext(code);
                                    error.setBeginLine(line);
                                    error.setEndLine(line);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }

                        }else if (isCppFile(error.getFilePath()))
                        {
                            ExtractCodeBlockForCpp extractCodeBlock = new ExtractCodeBlockForCpp(error.getFilePath());
                            String existCodeBlockByAst = extractCodeBlock.getCodeBlockByAst(error.getLine());
                            if ("200".equals(existCodeBlockByAst))
                            {

                                String code = extractCodeBlock.getCode();
                                Integer startLine = extractCodeBlock.getStartLine();
                                Integer endLine = extractCodeBlock.getEndLine();
                                error.setContext(code);
                                error.setBeginLine(startLine);
                                error.setEndLine(endLine);
                            }else {

                                try {
                                    String code =getAcode(fileAdr,line);
                                    error.setContext(code);
                                    error.setBeginLine(line);
                                    error.setEndLine(line);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }


                        concurrentErrors.add(error);
                    });
                }
            }

            executor.shutdown();
            while (!executor.isTerminated()) {
                // 等待所有线程完成
            }

            String jsonString = JSON.toJSONString(concurrentErrors);
            try (PrintWriter printWriter = new PrintWriter(output)) {
                printWriter.println(jsonString);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<Error> getErrorList(String path)  {
        // 获取文件的输入流对象
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(path);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        int len;
        byte[] bytes = new byte[1024];
        StringBuilder stringBuffer = new StringBuilder();
        while (true) {
            try {
                if (!((len = fileInputStream.read(bytes)) != -1)) break;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            // 添加字符串到缓冲区
            stringBuffer.append(new String(bytes, 0, len));
        }
        // 关闭资源
        try {
            fileInputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // 使用fastjson将字符串转换为JSON
        List<Error> errors = JSONObject.parseArray(stringBuffer.toString(), Error.class);

        return errors;

    }

    public static String getAcode(String path, Integer line) throws IOException {
        return readCppFile(path).get(line-1);
    }

    public static List<String> readCppFile(String filePath) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    public static Document parse(File file) throws org.dom4j.DocumentException {
        SAXReader reader = new SAXReader();
        return reader.read(file);
    }
    public static boolean isCppFile(String filename) {
        return filename.endsWith(".cpp") || filename.endsWith(".cc") || filename.endsWith(".cxx")
                || filename.endsWith(".h") || filename.endsWith(".hpp") || filename.endsWith(".inl")
                || filename.endsWith("ipp");
    }

    public static boolean isCFile(String filename) {
        return filename.endsWith(".c");
    }
}
