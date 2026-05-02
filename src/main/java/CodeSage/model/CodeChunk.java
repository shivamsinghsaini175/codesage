package CodeSage.model;

public class CodeChunk {

    private String content;
    private String filePath;

    public CodeChunk(String content, String filePath) {
        this.content = content;
        this.filePath = filePath;
    }

    public String getContent() { return content; }
    public String getFilePath() { return filePath; }
}