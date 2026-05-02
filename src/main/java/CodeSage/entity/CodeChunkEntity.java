package CodeSage.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "code_chunks")
public class CodeChunkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "file_path")
    private String filePath;

    @Column(columnDefinition = "vector(768)")
    private float[] embedding;

    // getters
    public Long getId() { return id; }
    public String getContent() { return content; }
    public String getFilePath() { return filePath; }
    public float[] getEmbedding() { return embedding; }

    // setters
    public void setContent(String content) { this.content = content; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public void setEmbedding(float[] embedding) { this.embedding = embedding; }
}