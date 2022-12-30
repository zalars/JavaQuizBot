package zalars.JavaQuizBot.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "answers")
public class Answer implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "option")
    private String option;

    @Column(name = "is_right")
    private Boolean rightness;

    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;

    public Answer() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }

    public Boolean getRightness() {
        return rightness;
    }

    public Boolean isRight() {
        return rightness;
    }

    public void setRight(Boolean right) {
        rightness = right;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Answer)) return false;
        Answer answer = (Answer) object;
        return getId().equals(answer.getId()) && getOption().equals(answer.getOption()) && getRightness().equals(answer.getRightness()) && getQuestion().equals(answer.getQuestion());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getOption(), getRightness(), getQuestion());
    }
}
