package CodeSage.model;

public class CodeFile {

    private String path;
    private String content;

    public CodeFile(String path, String content) {
        this.path = path;
        this.content = content;
    }

    public String getPath() { return path; }
    public String getContent() { return content; }
}