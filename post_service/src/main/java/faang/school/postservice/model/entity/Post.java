package faang.school.postservice.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "post")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "content", nullable = false, length = 4096)
    private String content;

    @Column(name = "author_id")
    private Long authorId;

    @Column(name = "project_id")
    private Long projectId;

    @OneToMany(mappedBy = "post", orphanRemoval = true)
    private List<Like> likes;

    @OneToMany(mappedBy = "post", orphanRemoval = true)
    private List<Comment> comments;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(
            name = "post_hashtag",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "hashtag_id")
    )
    @Builder.Default
    private Set<Hashtag> hashtags = new HashSet<>();

    @ManyToMany(mappedBy = "posts")
    private List<Album> albums;

    @OneToOne(mappedBy = "post", cascade = CascadeType.ALL)
    private Ad ad;

    @OneToMany(mappedBy = "post", orphanRemoval = true)
    private List<Resource> resources;

    @Column(name = "published", nullable = false)
    private boolean published;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "deleted", nullable = false)
    private boolean deleted;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "spell_check_completed", nullable = false)
    @Builder.Default
    private boolean spellCheckCompleted = false;

    @Column(name = "verified")
    private Boolean verified;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "verified_date")
    private LocalDateTime verifiedDate;

    @Version
    @Column(name = "version")
    private int version;

    @Column(name = "number_of_views", nullable = false)
    private int numberOfViews;

    @Column(name = "number_of_likes", nullable = false)
    private int numberOfLikes;

    public void incrementViews() {
        numberOfViews++;
    }

    public void incrementLikes() {
        numberOfLikes++;
    }
}
