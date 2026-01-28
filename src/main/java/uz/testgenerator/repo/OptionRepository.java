package uz.testgenerator.repo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.testgenerator.domain.OptionEntity;

public interface OptionRepository extends JpaRepository<OptionEntity, Long> {
  List<OptionEntity> findByQuestionId(Long questionId);
}
