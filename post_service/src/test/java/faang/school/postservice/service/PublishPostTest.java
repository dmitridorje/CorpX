package faang.school.postservice.service;

import faang.school.postservice.client.UserServiceClient;
import faang.school.postservice.config.context.UserContext;
import faang.school.postservice.mapper.PostMapper;
import faang.school.postservice.model.dto.PostDto;
import faang.school.postservice.model.entity.Post;
import faang.school.postservice.publisher.PostViewPublisher;
import faang.school.postservice.publisher.kafka.KafkaPostProducer;
import faang.school.postservice.redis.service.AuthorCacheService;
import faang.school.postservice.redis.service.PostCacheService;
import faang.school.postservice.repository.PostRepository;
import faang.school.postservice.service.impl.PostServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PublishPostTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostMapper postMapper;

    @Mock
    UserContext userContext;

    @Mock
    PostViewPublisher postViewPublisher;

    @Mock
    private KafkaPostProducer kafkaPostProducer;

    @Mock
    private AuthorCacheService authorCacheService;

    @Mock
    private PostCacheService postCacheService;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private PostServiceImpl postService;

    private Post unpublishedPost;
    private Post publishedPost;
    private PostDto publishedPostDto;

    @BeforeEach
    void setUp() {
        unpublishedPost = new Post();
        unpublishedPost.setId(1L);
        unpublishedPost.setPublished(false);
        unpublishedPost.setContent("Here is the unpublished post");

        publishedPost = new Post();
        publishedPost.setId(2L);
        publishedPost.setPublished(true);
        publishedPost.setContent("Here is the already published post");
        publishedPost.setPublishedAt(LocalDateTime.now());

        publishedPostDto = new PostDto();
        publishedPostDto.setId(1L);
        publishedPostDto.setPublished(true);
        publishedPostDto.setContent("Here is the published post");
    }

    @Test
    void shouldPublishPostSuccessfully() {
        when(postRepository.findById(1L)).thenReturn(java.util.Optional.of(unpublishedPost));
        when(postRepository.save(any(Post.class))).thenAnswer(i -> {
            Post savedPost = i.getArgument(0);
            savedPost.setAuthorId(1L);
            unpublishedPost.setPublished(savedPost.isPublished());
            unpublishedPost.setPublishedAt(savedPost.getPublishedAt());
            return savedPost;
        });

        lenient().when(postMapper.toPostDto(any(Post.class))). thenReturn(publishedPostDto);

        PostDto result = postService.publishPost(1L);

        assertNotNull(result);
        assertTrue(result.isPublished());
        assertNotNull(unpublishedPost.getPublishedAt());
        assertTrue(unpublishedPost.isPublished());

        verify(postRepository).save(argThat(post -> post.isPublished() && post.getId() == 1L));
        verify(authorCacheService,times(1)).saveAuthorToCache(any());
        verify(postCacheService,times(1)).savePostToCache(any());

    }

    @Test
    void shouldThrowExceptionWhenPostAlreadyPublished() {
        when(postRepository.findById(2L)).thenReturn(java.util.Optional.of(publishedPost));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            postService.publishPost(2L);
        });

        assertEquals("Post is already published", exception.getMessage());
        verify(postRepository, never()).save(publishedPost);
    }
}