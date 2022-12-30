# JavaQuizBot

## A Simple Telegram Bot for Java Quiz

This bot may be useful for those who are learning Java... and Russian.
They should read questions, choose correct answers from suggested options,
push a button and see the result.

You may take this repository as a template and create a bot quizzing on some different topic.
But if you want to, don't forget to change the bot name and token.  

### So how is it made?

The program was written on Java 8 with using Spring Boot and Maven.
All quiz questions and answers as well as users' sessions are saved in a PostgreSQL database,
and thus Spring Data JPA was involved.
An outer database is used for convenient addition of new questions and answers without stopping
the application which, besides, logs all significant runtime events into a file.

This bot communicates with Telegram by the 'long polling' way
(i.e. receives messages after sending requests) and puts all appropriate updates into the blocking queue.
Then those are taken by the quizzing service from another thread and handled with output of the result.

See details in code.
