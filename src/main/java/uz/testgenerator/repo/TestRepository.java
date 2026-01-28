package uz.testgenerator.repo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uz.testgenerator.domain.TestEntity;

public interface TestRepository extends JpaRepository<TestEntity, Long> {
  @Query("""
      select t.id, t.title, t.createdAt, count(q.id)
      from TestEntity t
      left join QuestionEntity q on q.test = t
      group by t.id, t.title, t.createdAt
      order by t.id desc
    """)
  List<Object[]> findAllWithQuestionCount();
}
