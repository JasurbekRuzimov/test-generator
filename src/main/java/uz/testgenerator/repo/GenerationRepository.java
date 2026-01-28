package uz.testgenerator.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.testgenerator.domain.GenerationEntity;

import java.util.List;

public interface GenerationRepository extends JpaRepository<GenerationEntity, Long> {
    List<GenerationEntity> findTop10ByOrderByCreatedAtDesc();
}
