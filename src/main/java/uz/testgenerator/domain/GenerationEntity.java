package uz.testgenerator.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "generations")
public class GenerationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "test_id")
    private TestEntity test;

    @Column(nullable = false)
    private int questionCount;

    @Column(nullable = false)
    private int variantCount;

    @Column(nullable = false)
    private boolean shuffleQuestions;

    @Column(nullable = false)
    private boolean shuffleOptions;

    @Column
    private Long seed;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private String status = "DONE";

    public GenerationEntity() {}

    public GenerationEntity(TestEntity test, int questionCount, int variantCount, boolean shuffleQuestions, boolean shuffleOptions, Long seed) {
        this.test = test;
        this.questionCount = questionCount;
        this.variantCount = variantCount;
        this.shuffleQuestions = shuffleQuestions;
        this.shuffleOptions = shuffleOptions;
        this.seed = seed;
    }

    public Long getId() { return id; }
    public TestEntity getTest() { return test; }
    public int getQuestionCount() { return questionCount; }
    public int getVariantCount() { return variantCount; }
    public boolean isShuffleQuestions() { return shuffleQuestions; }
    public boolean isShuffleOptions() { return shuffleOptions; }
    public Long getSeed() { return seed; }
    public Instant getCreatedAt() { return createdAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
