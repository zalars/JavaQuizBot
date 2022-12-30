package zalars.JavaQuizBot.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.Serializable;

@Component
public class Bot extends TelegramLongPollingBot {

    private static final Logger log = LoggerFactory.getLogger(Bot.class);

    private final UpdatesKeeper updatesKeeper;

    public Bot(UpdatesKeeper updatesKeeper) {
        super();
        log.info("(Bot-constr.) Creating and registering the bot");
        this.updatesKeeper = updatesKeeper;
        register();
    }

    private void register() {
        try {
            new TelegramBotsApi(DefaultBotSession.class).registerBot(this);
        } catch (TelegramApiException e) {
            log.error("(register) Registering failed: ", e);
            throw new RuntimeException();
        }
    }

    @Override
    public String getBotUsername() {
        return "JavaQuizBot";
    }

    @Override
    public String getBotToken() {
        return "-----token-----";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery() || update.hasMessage() && update.getMessage().hasText()) {
            log.info("(onUpdateReceived) Putting an appropriate update in the queue");
            try {
                updatesKeeper.putUpdate(update);
            } catch (InterruptedException e) {
                throw new RuntimeException();
            }
        }
    }

    /*
     * Adopted generic is for sending messages of two types: SendMessage and EditMessageText
     */
    public <T extends Serializable, Reply extends BotApiMethod<T>> void sendReply(Reply reply) {
        try {
            execute(reply);
        } catch (TelegramApiException e) {
            log.error("(sendReply) Sending a message failed: ", e);
            throw new RuntimeException();
        }
    }

}
