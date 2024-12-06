package faang.school.postservice.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import faang.school.postservice.model.enums.ResourceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResourceDto {
    private Long id;
    private String name;
    private String size;
    private String type;
    private ResourceStatus status;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
