package faang.school.postservice.service.comment;

import faang.school.postservice.client.UserServiceClient;
import faang.school.postservice.mapper.comment.CommentMapperImpl;
import faang.school.postservice.model.dto.CommentDto;
import faang.school.postservice.model.dto.UserDto;
import faang.school.postservice.model.entity.Comment;
import faang.school.postservice.model.entity.Post;
import faang.school.postservice.model.event.CommentEvent;
import faang.school.postservice.publisher.CommentEventPublisher;
import faang.school.postservice.publisher.kafka.KafkaCommentProducer;
import faang.school.postservice.redis.service.impl.AuthorCacheServiceImpl;
import faang.school.postservice.repository.CommentRepository;
import faang.school.postservice.repository.PostRepository;
import faang.school.postservice.service.impl.CommentServiceImpl;
import faang.school.postservice.validator.comment.CommentServiceValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class CommentServiceTest {
    @Spy
    private CommentMapperImpl commentMapper = new CommentMapperImpl();

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentServiceValidator validator;

    @Mock
    private CommentEventPublisher commentEventPublisher;

    @Mock
    private PostRepository postRepository;

    @Mock
    private KafkaCommentProducer kafkaCommentProducer;

    @Mock
    AuthorCacheServiceImpl authorCacheService;

    @InjectMocks
    private CommentServiceImpl commentService;

    @Captor
    private ArgumentCaptor<Comment> commentCaptor;

    private CommentDto commentDto;
    private Comment comment;
    private Post post;
    private Long userId;

    @BeforeEach
    void setUp() {
        post = new Post();
        post.setId(10L);
        commentDto = new CommentDto();
        commentDto.setPostId(10L);
        commentDto.setAuthorId(5L);
        commentDto.setContent("Some content");
        long commentId = 10L;
        comment = new Comment();
        comment.setId(commentId);
        comment.setPost(post);
        userId = 5L;
    }

    @Test
    void createComment() {
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        when(postRepository.findById(any(Long.class))).thenReturn(Optional.of(post));

        commentService.createComment(commentDto, userId);

        verify(commentRepository).save(commentCaptor.capture());
        verify(postRepository).findById(any(Long.class));
        verify(commentEventPublisher).publish(any(CommentEvent.class));
        verify(authorCacheService,times(1)).saveAuthorToCache(commentDto.getAuthorId());

        Comment savedComment = commentCaptor.getValue();
        assertAll(
                () -> assertEquals(commentDto.getContent(), savedComment.getContent()),
                () -> assertEquals(commentDto.getAuthorId(), savedComment.getAuthorId()),
                () -> assertEquals(commentDto.getPostId(), savedComment.getPost().getId())
        );
    }

    @Test
    void getComment() {
        Long postId = 10L;
        List<Comment> comments = createComments(postId);
        when(commentRepository.findAllByPostId(postId)).thenReturn(comments);

        List<CommentDto> commentDtos = commentService.getComment(postId);

        verify(commentRepository, times(1)).findAllByPostId(postId);
        assertAll(
                () -> assertEquals(2, commentDtos.size()),
                () -> assertEquals(postId, commentDtos.get(0).getPostId()),
                () -> assertEquals(postId, commentDtos.get(1).getPostId()),
                () -> assertTrue(commentDtos.get(0).getUpdatedAt().isAfter(commentDtos.get(1).getUpdatedAt()))
        );
    }

    @Test
    void deleteCommentSuccessful() {
        Long commentId = 10L;

        commentService.deleteComment(commentId);

        verify(commentRepository, times(1)).deleteById(commentId);
        assertDoesNotThrow(() -> commentService.deleteComment(commentId));
    }

    @Test
    void updateComment() {
        Long commentId = 10L;
        Post post = new Post();
        post.setId(15L);
        comment.setAuthorId(commentDto.getAuthorId());
        comment.setPost(post);

        when(userServiceClient.getUser(commentDto.getAuthorId())).thenReturn(new UserDto(commentDto.getAuthorId(),
                "User1", "email@somedomain.com", null));
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        commentService.updateComment(commentId, commentDto, userId);

        verify(userServiceClient, times(1)).getUser(commentDto.getAuthorId());
        verify(commentRepository, times(1)).save(commentCaptor.capture());
        Comment savedComment = commentCaptor.getValue();
        assertAll(
                () -> assertEquals(commentDto.getContent(), savedComment.getContent()),
                () -> assertEquals(commentDto.getAuthorId(), savedComment.getAuthorId())
        );
    }

    private List<Comment> createComments(Long postId) {
        Post post = new Post();
        post.setId(postId);
        List<Comment> comments = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            comments.add(new Comment());
        }
        comments.get(0).setPost(post);
        comments.get(0).setUpdatedAt(LocalDateTime.now());
        comments.get(1).setPost(post);
        comments.get(1).setUpdatedAt(LocalDateTime.now().plusMinutes(1));
        return comments;
    }
}