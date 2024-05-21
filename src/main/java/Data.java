/**
 * @author Xzzz
 * @data 2023/07/27
 */
@lombok.Data
public class Data {

    private String violated_code;//
    private String rule;//
    private String generated_patch;//
    private String real_patch;
    private String project;

    private String file;//
    private String start_line;//
    private String end_line;//
    private String version;
    private String link;

}
