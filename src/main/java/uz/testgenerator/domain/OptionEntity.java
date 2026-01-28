package uz.testgenerator.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "options")
public class OptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private QuestionEntity question;

    @Column(nullable = false, columnDefinition = "text")
    private String text;

    /** 0..3 (A..D) in ORIGINAL order */
    @Column(nullable = false)
    private int originalIndex;

    public OptionEntity() {}

    public OptionEntity(String text, int originalIndex) {
        this.text = text;
        this.originalIndex = originalIndex;
    }

    public Long getId() { return id; }
    public QuestionEntity getQuestion() { return question; }
    public void setQuestion(QuestionEntity question) { this.question = question; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public int getOriginalIndex() { return originalIndex; }
    public void setOriginalIndex(int originalIndex) { this.originalIndex = originalIndex; }
}
