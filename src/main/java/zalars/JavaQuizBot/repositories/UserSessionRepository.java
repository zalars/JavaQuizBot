package zalars.JavaQuizBot.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import zalars.JavaQuizBot.entities.UserSession;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
}
