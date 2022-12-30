package zalars.JavaQuizBot.services;

import com.vdurmont.emoji.EmojiParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import zalars.JavaQuizBot.entities.Answer;
import zalars.JavaQuizBot.entities.Question;
import zalars.JavaQuizBot.entities.UserSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/*
 * Asks questions, handles answers, estimates results (all in a separate thread),
 * i.e. communicates with user
 */
@Service
public class Quizzer implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(Quizzer.class);

    private final Bot bot;
    private final UpdatesKeeper updatesKeeper;
    private final DatabaseAgent databaseAgent;
    private final InlineKeyboardMarkup answerButtons;
    private long chatId;
    private UserSession session;

    public Quizzer(Bot bot, UpdatesKeeper updatesKeeper, DatabaseAgent databaseAgent) {
        log.info("(Quizzer-constr.) Creating quizzer");
        this.bot = bot;
        this.updatesKeeper = updatesKeeper;
        this.databaseAgent = databaseAgent;
        this.answerButtons = createAnswerButtons();

        launchItselfInNewThread();
    }

    private InlineKeyboardMarkup createAnswerButtons() {
        List<InlineKeyboardButton> buttonsRow = new ArrayList<>();
        for (int i = 0; i <= 2; i++) {
            InlineKeyboardButton optionButton = new InlineKeyboardButton();
            optionButton.setText(EmojiParser.parseToUnicode(Emojis.DIGIT[i]));
            optionButton.setCallbackData(String.valueOf(i));
            buttonsRow.add(optionButton);
        }
        List<List<InlineKeyboardButton>> buttonsMarkup = new ArrayList<>();
        buttonsMarkup.add(buttonsRow);

        InlineKeyboardMarkup answerButtons = new InlineKeyboardMarkup();
        answerButtons.setKeyboard(buttonsMarkup);

        return answerButtons;
    }

    private void launchItselfInNewThread() {
        Thread quizzerThread = new Thread(this, "Quizzer");
        quizzerThread.setDaemon(true);
        quizzerThread.start();
    }

    @Override
    public void run() {
        log.info("(run) Thread 'Quizzer' launched in a separate thread");
        Update update;
        try {
            while ((update = this.updatesKeeper.takeUpdate()) != null) {
                if (update.hasCallbackQuery()) {
                    handleUserAnswer(update);
                } else {
                    handleUserTextMessage(update);
                }
            }
        } catch (InterruptedException e) {
            log.error("(run) Thread 'Quizzer' interrupted: ", e);
            throw new RuntimeException();
        }
    }

    private void handleUserAnswer(Update userAnswer) {
        this.chatId = userAnswer.getCallbackQuery().getMessage().getChatId();
        log.info("(handleUserAnswer) Chat <{}>: start an answer handling", this.chatId);

        this.session = this.databaseAgent.loadUserSessionByChatId(this.chatId);
        if (this.session == null) {
            log.warn("(handleUserAnswer) Chat <{}>: session lost", this.chatId);
            String lostSessionMessage = "Ой! " + Emojis.ANGUISHED
                    + "\nПроизошла ошибка: ваша сессия  потеряна."
                    + "\nНо вы можете снова начать тест - командой /start";
            sendTextMessage(EmojiParser.parseToUnicode(lostSessionMessage), null);
            return;
        }
        estimateAnswer(userAnswer);
        rateUser();
        checkAllQuestionsPassed();
    }

    private void handleUserTextMessage(Update userTextMessage) {
        this.chatId = userTextMessage.getMessage().getChatId();
        log.info("(handleUserTextMessage) Chat <{}>: start a user's text message handling", this.chatId);

        String userText = userTextMessage.getMessage().getText();
        switch (userText) {
            case "/start":
                createNewSession();
                log.info("(handleUserTextMessage) Chat <{}>: a new quiz started", this.chatId);
                askQuestion();
                break;
            case "/help":
                showHelp();
                break;
            default:
                log.info("(handleUserTextMessage) Chat <{}> - user typed this: \"{}\"", this.chatId, userText);
        }
    }

    private void createNewSession() {
        this.session = new UserSession();
        this.session.setChatId(this.chatId);
        this.session.setPassedQuestions(0);
        this.session.setRightAnswers(0);
        this.session.putUntestedQuestionsIds(IntStream.rangeClosed(1, (int) this.databaseAgent.countQuestions())
                .boxed()
                .collect(Collectors.toList()));

        this.databaseAgent.saveUserSession(this.session);
        log.info("(createAndSaveNewSession) Chat <{}>: a new session created", this.chatId);
    }

    private void sendTextMessage(String textToSend, InlineKeyboardMarkup keyboardMarkup) {
        SendMessage textMessage = new SendMessage(String.valueOf(this.chatId), textToSend);
        textMessage.setReplyMarkup(keyboardMarkup);

        log.info("(sendTextMessage) Chat <{}>: sending a text message", this.chatId);
        this.bot.sendReply(textMessage);
    }

    private void askQuestion() {
        log.info("(askQuestion) Chat <{}>: preparing a question", this.chatId);

        List<Integer> listOfUntestedQuestionsIds = this.session.obtainListUntestedQuestionsIds();
        int randomIndex = new Random().nextInt(listOfUntestedQuestionsIds.size());
        int questionId = listOfUntestedQuestionsIds.remove(randomIndex);
        Question nextQuestion = this.databaseAgent.fetchQuestionById(questionId);
        this.session.setCurrentQuestionId(nextQuestion.getId());

        List<Answer> optionList = nextQuestion.getOptionList();

        StringBuilder displayText = new StringBuilder();
        displayText.append(Emojis.GEM).append("\n").append(nextQuestion.getIssue()).append("\n");
        for (int i = 0; i <= 2; i++) {
            displayText.append("\n").append(Emojis.DIGIT[i])
                    .append(optionList.get(i).getOption());
        }
        this.session.putUntestedQuestionsIds(listOfUntestedQuestionsIds);
        this.databaseAgent.saveUserSession(this.session);

        sendTextMessage(EmojiParser.parseToUnicode(displayText.toString()), this.answerButtons);
    }

    private void showHelp() {
        log.info("(showHelp) Chat <{}>: request the help issues", this.chatId);
        String helpText = "Пояснения:\n" + Emojis.PIN + "вопросы по разным темам задаются в случайном порядке;\n" +
                Emojis.PIN + "вариант ответа выбирается нажатием на соответствующую кнопку, после чего " +
                "внизу этого же сообщения с вопросом будет видно, был ли ответ верным;\n" +
                Emojis.PIN + "команда /start начинает тест заново, обнуляя статистику правильных ответов;\n" +
                Emojis.PIN + "до ввода вышеназванной команды серия вопросов-ответов образует сессию;\n" +
                Emojis.PIN + "в рамках сессии перейти к следующему вопросу, оставив без ответа предыдущий, невозможно";
        sendTextMessage(EmojiParser.parseToUnicode(helpText), null);
    }

    private void estimateAnswer(Update userAnswer) {
        log.info("(estimateAnswer) Chat <{}>: estimating the answer", this.chatId);

        CallbackQuery callback = userAnswer.getCallbackQuery();
        int answerOption = Integer.parseInt(callback.getData());
        String editedText = callback.getMessage().getText() + "\n__________\n\nВаш ответ:  "
                            + Emojis.DIGIT[answerOption] + "  -  ";

        Question currentQuestion = this.databaseAgent.fetchQuestionById(this.session.getCurrentQuestionId());
        int rightAnswer = currentQuestion.getOptionList().stream()
                                                         .map(Answer::isRight)
                                                         .collect(Collectors.toList())
                                                         .indexOf(true);
        if (answerOption == rightAnswer) {
            editedText += "верно!  " + Emojis.RIGHT;
            this.session.increaseRightAnswers();
        } else {
            editedText += "увы, неверно!  " + Emojis.WRONG + "\n\nПравильный ответ:  " + Emojis.DIGIT[rightAnswer];
        }
        this.session.increasePassedQuestions();

        sendEditedQuestionMessage(callback.getMessage().getMessageId(), editedText);
    }

    private void sendEditedQuestionMessage(int editedMessageId, String editedText) {
        EditMessageText editedQuestionMessage = new EditMessageText(EmojiParser.parseToUnicode(editedText));
        editedQuestionMessage.setChatId(this.chatId);
        editedQuestionMessage.setMessageId(editedMessageId);

        log.info("(sendEditedQuestionMessage) Chat <{}>: sending an edited message with estimation", this.chatId);
        this.bot.sendReply(editedQuestionMessage);
    }

    private void rateUser() {
        log.info("(rateUser) Chat <{}>: current rating", this.chatId);
        int rightAnswers = this.session.getRightAnswers();
        StringBuilder gems = new StringBuilder();
        IntStream.range(0, rightAnswers).mapToObj(i -> Emojis.GEM).forEach(gems::append);
        String estimation = String.format("%s%nТекущая оценка %n%s  - правильных ответов:%n%d из %d  (%d%%)",
                Emojis.CHART, gems, rightAnswers, this.session.getPassedQuestions(), getRating());
        sendTextMessage(EmojiParser.parseToUnicode(estimation), null);
    }

    private int getRating() {
        return (int) Math.round((double) this.session.getRightAnswers() / this.session.getPassedQuestions() * 100);
    }

    private void checkAllQuestionsPassed() {
        log.info("(checkAllQuestionsPassed) Chat <{}>: checking if all questions passed", this.chatId);
        boolean allQuestionsPassed = this.session.getPassedQuestions() == this.databaseAgent.countQuestions();
        if (allQuestionsPassed) {
            showQuizResult();
        } else {
            askQuestion();
        }
    }

    private void showQuizResult() {
        int rating = getRating();
        log.info("(showQuizResult) Chat <{}>: All questions answered, result is {}",
                this.chatId, rating);
        String ratingEstimation = rating == 100 ? "высочайший результат! Вы великолепны! " + Emojis.STARS :
                rating > 74 ? "очень хорошо! Еще немного подучить, и будет супер! " + Emojis.SMILE :
                        rating > 49 ? "средний результат! " + Emojis.RELIEVED + "Но есть куда расти! "  :
                                rating > 24 ? "плоховато! " + Emojis.CONFUSED + "Но вы можете лучше!" :
                                        rating > 0 ? "вы меня расстроили... " + Emojis.PENSIVE :
                                                "как так! Вы вообще учились?! " + Emojis.RAGE;
        String quizResult = String.format(" %s%nВы ответили на все вопросы - за одно это вас можно "
                        + "поздравить! %s%n%nВ итоге вы набрали  %d%% от лучшего результата%n- %s%n%n"
                        + "Хотите ещё раз пройти тест?%nКоманда /start запускает новый тест",
                Emojis.CONGRATULATION, Emojis.WINK, rating, ratingEstimation);

        sendTextMessage(EmojiParser.parseToUnicode(quizResult), null);
    }

}
