public class GitHubUrlParser {
    public static void main(String[] args) {
        String githubUrl = "https://github.com/apache/thrift.git";

        String result=git_process(githubUrl);
        System.out.println(result);
    }

    static String git_process(String githubUrl)
    {
        String[] urlParts = githubUrl.split("/");
        String projectNameWithGit = urlParts[urlParts.length - 1];
        // 去掉 ".git" 后缀
        String projectName = projectNameWithGit.replace(".git", "");

        return projectName;
    }

}
