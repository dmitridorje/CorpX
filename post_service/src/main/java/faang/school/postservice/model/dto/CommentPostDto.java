package faang.school.postservice.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CommentPostDto {

    private Long id;
    @NotBlank
    @Size(max = 4096, message = "content must be no more than 4096 characters")
    private String content;
    @NotNull
    private Long authorId;
    @NotNull
    private Long postId;
}
