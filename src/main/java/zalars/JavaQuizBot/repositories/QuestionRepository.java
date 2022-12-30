package zalars.JavaQuizBot.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import zalars.JavaQuizBot.entities.Question;

public interface QuestionRepository extends JpaRepository<Question, Integer> {
}
