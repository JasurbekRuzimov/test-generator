package uz.testgenerator.repo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.testgenerator.domain.QuestionEntity;

public interface QuestionRepository extends JpaRepository<QuestionEntity, Long> {
  List<QuestionEntity> findByTestId(Long testId);
}
