package CodeSage.repository;

import CodeSage.entity.CodeChunkEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CodeChunkRepository extends JpaRepository<CodeChunkEntity, Long> {

    @Query(value = """
    SELECT content, file_path
    FROM code_chunks
    ORDER BY embedding <-> CAST(:embedding AS vector)
    LIMIT :limit
    """, nativeQuery = true)
    List<Object[]> searchRaw(@Param("embedding") String embedding,
                             @Param("limit") int limit);
}