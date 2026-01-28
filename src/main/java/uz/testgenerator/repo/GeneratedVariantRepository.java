package uz.testgenerator.repo;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.testgenerator.domain.GeneratedVariantEntity;

import java.util.List;

public interface GeneratedVariantRepository extends JpaRepository<GeneratedVariantEntity, Long> {
    Optional<GeneratedVariantEntity> findByGenerationIdAndVariantNo(Long generationId, int variantNo);

    List<GeneratedVariantEntity> findByGenerationIdOrderByVariantNoAsc(Long generationId);
}
