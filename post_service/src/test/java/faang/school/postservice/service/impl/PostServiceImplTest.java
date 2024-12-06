package faang.school.postservice.service.impl;

import faang.school.postservice.config.context.UserContext;
import faang.school.postservice.model.dto.PostDto;
import faang.school.postservice.exception.DataValidationException;
import faang.school.postservice.mapper.PostMapper;
import faang.school.postservice.model.entity.Post;
import faang.school.postservice.publisher.PostViewPublisher;
import faang.school.postservice.repository.PostRepository;
import faang.school.postservice.service.BatchProcessService;
import faang.school.postservice.util.moderation.ModerationDictionary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PostServiceImplTest {
    @Mock
    UserContext userContext;

    @Mock
    PostViewPublisher postViewPublisher;

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostMapper postMapper;

    @Mock
    ModerationDictionary moderationDictionary;

    @Mock
    private BatchProcessService batchProcessService;

    @InjectMocks
    private PostServiceImpl postService;

    private Post post;
    private PostDto postDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        post = new Post();
        post.setId(1L);
        post.setContent("Test post");

        postDto = new PostDto();
        postDto.setId(1L);
        postDto.setContent("Test post DTO");

        ReflectionTestUtils.setField(postService, "correcterBatchSize", 100);

        ReflectionTestUtils.setField(postService, "moderationBatchSize", 2);
    }

    @Test
    void testGetAllPostsByHashtagId() {
        Pageable pageable = mock(Pageable.class);
        Page<Post> postPage = new PageImpl<>(List.of(post));

        when(postRepository.findByHashtagsContent(anyString(), any(Pageable.class))).thenReturn(postPage);
        when(postMapper.toPostDto(any(Post.class))).thenReturn(postDto);

        Page<PostDto> result = postService.getAllPostsByHashtagId("#hashtag", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(postRepository, times(1)).findByHashtagsContent(anyString(), any(Pageable.class));
        verify(postMapper, times(1)).toPostDto(any(Post.class));
        verify(postViewPublisher,times(1)).publish(any());
    }

    @Test
    void testGetPostByIdInternal_PostExists() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        Post result = postService.getPostByIdInternal(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(postRepository, times(1)).findById(1L);
        verify(postViewPublisher,times(1)).publish(any());
    }

    @Test
    void testGetPostByIdInternal_PostNotFound() {
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        DataValidationException exception = assertThrows(DataValidationException.class, () -> {
            postService.getPostByIdInternal(1L);
        });

        assertEquals("'Post not in database' error occurred while fetching post", exception.getMessage());
        verify(postRepository, times(1)).findById(1L);
        verify(postViewPublisher,times(0)).publish(any());
    }

    @Test
    void testUpdatePostInternal() {
        when(postRepository.save(any(Post.class))).thenReturn(post);

        Post result = postService.updatePostInternal(post);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(postRepository, times(1)).save(post);
    }

    @Test
    void testGetUserPublishedPosts() {
        Long authorId = 1L;

        post.setPublished(true);
        post.setDeleted(false);

        when(postRepository.findByAuthorIdWithLikes(authorId)).thenReturn(List.of(post));
        when(postMapper.toPostDto(any(Post.class))).thenReturn(postDto);

        List<PostDto> result = postService.getUserPublishedPosts(authorId);

        assertEquals(1, result.size());
        assertEquals(postDto.getId(), result.get(0).getId());
        assertEquals(postDto.getPublishedAt(), result.get(0).getPublishedAt());
        verify(postRepository, times(1)).findByAuthorIdWithLikes(authorId);
        verify(postMapper, times(1)).toPostDto(any(Post.class));
        verify(postViewPublisher,times(1)).publish(any());
    }

    @Test
    void testGetProjectPublishedPosts() {
        Long projectId = 2L;

        post.setPublished(true);
        post.setDeleted(false);

        when(postRepository.findByProjectIdWithLikes(projectId)).thenReturn(List.of(post));
        when(postMapper.toPostDto(any(Post.class))).thenReturn(postDto);

        List<PostDto> result = postService.getProjectPublishedPosts(projectId);

        assertEquals(1, result.size());
        assertEquals(postDto.getId(), result.get(0).getId());
        assertEquals(postDto.getPublishedAt(), result.get(0).getPublishedAt());
        verify(postRepository, times(1)).findByProjectIdWithLikes(projectId);
        verify(postMapper, times(1)).toPostDto(any(Post.class));
        verify(postViewPublisher,times(1)).publish(any());
    }

    @Test
    @DisplayName("Should successfully correct spelling for unpublished posts in batches")
    public void testCorrectSpellingInUnpublishedPosts_Success() {
        List<Post> mockPosts = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            Post post = new Post();
            post.setContent("This is post No. " + i);
            post.setPublished(false);
            post.setDeleted(false);
            post.setScheduledAt(LocalDateTime.now().plusDays(1));
            mockPosts.add(post);
        }

        when(postRepository.findReadyForSpellCheck()).thenReturn(mockPosts);
        doAnswer(invocation -> {
            List<Post> postsBatch = invocation.getArgument(0);
            postsBatch.forEach(post -> {
                String content = post.getContent();
                post.setContent(content + " corrected\n[Automatic correction of spelling and grammatical errors" +
                        " has been applied to the post text. Please review the text before publishing!]");
                post.setSpellCheckCompleted(true);
            });
            return CompletableFuture.completedFuture(null);
        }).when(batchProcessService).processBatch(anyList());

        postService.correctSpellingInUnpublishedPosts();

        verify(batchProcessService, times(5)).processBatch(anyList());

        for (Post post : mockPosts) {
            assertTrue(post.getContent().endsWith("corrected\n" +
                    "[Automatic correction of spelling and grammatical errors has been applied to the post text." +
                    " Please review the text before publishing!]"));
            assertTrue(post.isSpellCheckCompleted());
        }
    }

    @Test
    @DisplayName("Should do nothing when no unpublished posts are found")
    public void testCorrectSpellingInUnpublishedPosts_NoPosts() {
        when(postRepository.findReadyToPublish()).thenReturn(Collections.emptyList());

        postService.correctSpellingInUnpublishedPosts();

        verify(batchProcessService, never()).processBatch(anyList());
    }

    @Test
    @DisplayName("Should return split list of unverified posts")
    void testFindAndSplitUnverifiedPosts_Success() {
        List<Post> unverifiedPosts = List.of(
                Post.builder().id(1L).build(),
                Post.builder().id(2L).build(),
                Post.builder().id(3L).build()
        );

        when(postRepository.findAllByVerifiedDateIsNull()).thenReturn(unverifiedPosts);

        List<List<Post>> result = postService.findAndSplitUnverifiedPosts();

        assertEquals(2, result.size());
        assertEquals(2, result.get(0).size());
        assertEquals(1, result.get(1).size());

        verify(postRepository, times(1)).findAllByVerifiedDateIsNull();
    }

    @Test
    @DisplayName("Should return empty list when no unverified posts are found")
    void testFindAndSplitUnverifiedPosts_NoPostsFound() {
        when(postRepository.findAllByVerifiedDateIsNull()).thenReturn(Collections.emptyList());

        List<List<Post>> result = postService.findAndSplitUnverifiedPosts();

        assertTrue(result.isEmpty(), "Expected an empty list of unverified posts");

        verify(postRepository, times(1)).findAllByVerifiedDateIsNull();
    }

    @Test
    @DisplayName("Should verify posts for swear words")
    void testVerifyPostsForSwearWords_Success() {
        List<Post> unverifiedPostsBatch = List.of(
                Post.builder().id(1L).content("This is clean content")
                        .verified(null).verifiedDate(null).build(),
                Post.builder().id(2L).content("This is bad content")
                        .verified(null).verifiedDate(null).build()
        );

        when(moderationDictionary.containsSwearWords("This is clean content")).thenReturn(true);
        when(moderationDictionary.containsSwearWords("This is bad content")).thenReturn(false);

        CompletableFuture<Void> future = postService.verifyPostsForSwearWords(unverifiedPostsBatch);
        future.join();

        assertFalse(unverifiedPostsBatch.get(0).getVerified());
        assertNotNull(unverifiedPostsBatch.get(0).getVerifiedDate());
        assertTrue(unverifiedPostsBatch.get(1).getVerified());
        assertNotNull(unverifiedPostsBatch.get(1).getVerifiedDate());

        verify(postRepository, times(1)).saveAll(unverifiedPostsBatch);
    }
}
