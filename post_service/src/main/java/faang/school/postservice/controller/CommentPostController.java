package faang.school.postservice.controller;

import faang.school.postservice.config.context.UserContext;
import faang.school.postservice.model.dto.CommentPostDto;
import faang.school.postservice.model.entity.Comment;
import faang.school.postservice.service.comment.CommentPostServiceImpl;
import faang.school.postservice.util.MapperUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/comments")
public class CommentPostController {

    private final CommentPostServiceImpl service;
    private final UserContext userContext;

    @GetMapping("/{postId}")
    public List<CommentPostDto> getAllCommentsByPostId(@PathVariable(value = "postId") Long postId) {
        var comments = service.findCommentsByPostId(postId);
        return MapperUtil.convertList(comments, CommentPostDto.class);
    }

    @GetMapping("/{commentId}")
    public CommentPostDto getComment(@PathVariable(value = "commentId") Long commentId) {
        Comment comment = service.getById(commentId);
        return MapperUtil.convertClass(comment, CommentPostDto.class);
    }

    @PostMapping("/{postId}")
    public CommentPostDto createComment(@PathVariable(value = "postId") Long postId,
                                        @Valid @RequestBody CommentPostDto commentDto) {
        var authorId = userContext.getUserId();
        Comment entity = MapperUtil.convertClass(commentDto, Comment.class);
        entity.setAuthorId(authorId);
        var comment = service.create(postId, entity);
        return MapperUtil.convertClass(comment, CommentPostDto.class);
    }

    @PutMapping("/{commentId}")
    public CommentPostDto updateComment(@PathVariable(value = "commentId") Long commentId,
                                        @Valid @RequestBody CommentPostDto commentDto) {
        var authorId = userContext.getUserId();
        Comment entity = MapperUtil.convertClass(commentDto, Comment.class);
        entity.setAuthorId(authorId);
        var comment = service.update(commentId, entity);
        return MapperUtil.convertClass(comment, CommentPostDto.class);
    }

    @DeleteMapping("/{commentId}")
    public void delete(@PathVariable(value = "commentId") Long commentId) {
        service.delete(commentId);
    }
}
