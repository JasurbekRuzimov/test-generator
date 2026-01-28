package uz.testgenerator.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questions")
public class QuestionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id")
    private TestEntity test;

    @Column(nullable = false, columnDefinition = "text")
    private String text;

    @Column(nullable = false)
    private int correctIndex;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("originalIndex ASC")
    private List<OptionEntity> options = new ArrayList<>();

    public QuestionEntity() {}

    public QuestionEntity(String text, int correctIndex) {
        this.text = text;
        this.correctIndex = correctIndex;
    }

    public void addOption(OptionEntity o) {
        o.setQuestion(this);
        this.options.add(o);
    }

    public Long getId() { return id; }
    public TestEntity getTest() { return test; }
    public void setTest(TestEntity test) { this.test = test; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public int getCorrectIndex() { return correctIndex; }
    public void setCorrectIndex(int correctIndex) { this.correctIndex = correctIndex; }
    public List<OptionEntity> getOptions() { return options; }
}
