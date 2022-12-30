package zalars.JavaQuizBot.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/*
 * Contains a blocking queue which is handled in two different threads
 */
@Component
public class UpdatesKeeper {

    private static final Logger log = LoggerFactory.getLogger(UpdatesKeeper.class);

    private final BlockingQueue<Update> queue;

    public UpdatesKeeper() {
        log.info("(UpdatesKeeper-constr.) - Creating updatesKeeper");
        this.queue = new LinkedBlockingQueue<>();
    }

    public void putUpdate(Update update) throws InterruptedException {
        this.queue.put(update);
    }

    public Update takeUpdate() throws InterruptedException {
        return this.queue.take();
    }
}
