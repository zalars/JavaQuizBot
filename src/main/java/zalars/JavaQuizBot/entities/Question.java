package zalars.JavaQuizBot.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "questions")
public class Question implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "issue")
    private String issue;

    // EAGER fetching: 'SELECT question' will bring it with all linked answers
    @OneToMany(mappedBy = "question", fetch = FetchType.EAGER)
    private List<Answer> optionList;

    public Question() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getIssue() {
        return issue;
    }

    public void setIssue(String issue) {
        this.issue = issue;
    }

    public List<Answer> getOptionList() {
        return optionList;
    }

    public void setOptionList(List<Answer> optionList) {
        this.optionList = optionList;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Question)) return false;
        Question question = (Question) object;
        return getId().equals(question.getId()) && getIssue().equals(question.getIssue()) && getOptionList().equals(question.getOptionList());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getIssue(), getOptionList());
    }
}
