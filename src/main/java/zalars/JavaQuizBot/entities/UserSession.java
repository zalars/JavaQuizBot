package zalars.JavaQuizBot.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Entity
@Table(name = "user_sessions")
public class UserSession implements Serializable {

    @Id
    @Column(name = "chat_id")
    private Long chatId;

    @Column(name = "untested_questions_ids")
    private String untestedQuestionsIds;

    @Column(name = "passed_questions")
    private Integer passedQuestions;

    @Column(name = "right_answers")
    private Integer rightAnswers;

    @Column(name = "current_question_id")
    private Integer currentQuestionId;

    public UserSession() {}

    public List<Integer> obtainListUntestedQuestionsIds() {
        return Arrays.stream(this.untestedQuestionsIds.trim().split(" "))
                        .map(Integer::parseInt)
                        .collect(Collectors.toList());
    }

    public void putUntestedQuestionsIds(List<Integer> listUntestedQuestionsIds) {
        StringBuilder sb = new StringBuilder();
        listUntestedQuestionsIds.stream().map(i -> i + " ").forEach(sb::append);
        this.untestedQuestionsIds = sb.toString();
    }

    public void increasePassedQuestions() {
        this.passedQuestions++;
    }

    public void increaseRightAnswers() {
        this.rightAnswers++;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public String getUntestedQuestionsIds() {
        return untestedQuestionsIds;
    }

    public Integer getPassedQuestions() {
        return passedQuestions;
    }

    public void setPassedQuestions(Integer passedQuestions) {
        this.passedQuestions = passedQuestions;
    }

    public Integer getRightAnswers() {
        return rightAnswers;
    }

    public void setRightAnswers(Integer rightAnswers) {
        this.rightAnswers = rightAnswers;
    }

    public Integer getCurrentQuestionId() {
        return currentQuestionId;
    }

    public void setCurrentQuestionId(Integer currentQuestionId) {
        this.currentQuestionId = currentQuestionId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof UserSession)) return false;
        UserSession session = (UserSession) object;
        return getChatId().equals(session.getChatId())
                        && Objects.equals(getUntestedQuestionsIds(), session.getUntestedQuestionsIds())
                        && getPassedQuestions().equals(session.getPassedQuestions())
                        && getRightAnswers().equals(session.getRightAnswers());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getChatId(), getUntestedQuestionsIds(), getPassedQuestions(), getRightAnswers());
    }
}
