package zalars.JavaQuizBot.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import zalars.JavaQuizBot.entities.Question;
import zalars.JavaQuizBot.entities.UserSession;
import zalars.JavaQuizBot.repositories.QuestionRepository;
import zalars.JavaQuizBot.repositories.UserSessionRepository;

/*
 * Communicates with the PostgreSQL database
 */

@Service
public class DatabaseAgent {

    private static final Logger log = LoggerFactory.getLogger(DatabaseAgent.class);

    private final QuestionRepository questionRepository;

    private final UserSessionRepository sessionRepository;

    public DatabaseAgent(QuestionRepository questionRepository, UserSessionRepository sessionRepository) {
        log.info("(DatabaseAgent-constr.) Creating dataManager");
        this.questionRepository = questionRepository;
        this.sessionRepository = sessionRepository;
    }

    public Question fetchQuestionById(int questionId) {
        try {
            return questionRepository.findById(questionId).orElse(null);
        } catch (Exception e) {
            log.error("(fetchQuestionById) Finding a question in DB failed: ", e);
            throw new RuntimeException();
        }
    }

    public long countQuestions() {
        try {
            return questionRepository.count();
        } catch (Exception e) {
            log.error("(countQuestions) Counting questions in DB failed: ", e);
            throw new RuntimeException();
        }
    }

    public void saveUserSession(UserSession session) {
        try {
            sessionRepository.save(session);
        } catch (Exception e) {
            log.error("(saveUserSession) Saving a session in DB failed: ", e);
            throw new RuntimeException();
        }
    }

    public UserSession loadUserSessionByChatId(long chatId) {
        try {
            return sessionRepository.findById(chatId).orElse(null);
        } catch (Exception e) {
            log.error("(loadUserSessionByChatId) Loading a session from DB failed: ", e);
            throw new RuntimeException();
        }
    }

}
