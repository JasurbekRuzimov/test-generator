package uz.testgenerator.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "generated_variants")
public class GeneratedVariantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "generation_id")
    private GenerationEntity generation;

    @Column(nullable = false)
    private int variantNo;

    @Column(nullable = false)
    private String pdfPath;

    @Column(nullable = false, columnDefinition = "text")
    private String answerKeyText; // e.g. "1-B, 2-D, ..."

    public GeneratedVariantEntity() {}

    public GeneratedVariantEntity(GenerationEntity generation, int variantNo, String pdfPath, String answerKeyText) {
        this.generation = generation;
        this.variantNo = variantNo;
        this.pdfPath = pdfPath;
        this.answerKeyText = answerKeyText;
    }

    public Long getId() { return id; }
    public GenerationEntity getGeneration() { return generation; }
    public int getVariantNo() { return variantNo; }
    public String getPdfPath() { return pdfPath; }
    public String getAnswerKeyText() { return answerKeyText; }
}
