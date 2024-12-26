package com.javarush.telegram;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;

public class TinderBoltApp extends MultiSessionTelegramBot {
    public static final String TELEGRAM_BOT_NAME = Secure.TELEGRAM_BOT_NAME; //TODO: добавь имя бота в кавычках
    public static final String TELEGRAM_BOT_TOKEN = Secure.TELEGRAM_TOKEN; //TODO: добавь токен бота в кавычках
    public static final String OPEN_AI_TOKEN = Secure.GPT_TOKEN; //TODO: добавь токен ChatGPT в кавычках

    private static final ChatGPTService chatGpt = new ChatGPTService(OPEN_AI_TOKEN);
    private static DialogMode currentMode = DialogMode.DEFAULT;
    private static final ArrayList<String> list = new ArrayList<>();
    private static UserInfo me;
    private static UserInfo she;
    private static int questionCount;

    public TinderBoltApp() {
        super(TELEGRAM_BOT_NAME, TELEGRAM_BOT_TOKEN);
    }

    @Override
    public void onUpdateEventReceived(Update update) {
        //TODO: основной функционал бота будем писать здесь
        String message = getMessageText();

        //region command START
        if (message.equals("/start")) {
            currentMode = DialogMode.MAIN;
            sendPhotoMessage("gpt");
            String text = loadMessage("main");
            sendTextMessage(text);

            showMainMenu("главное меню бота", "/start",
                    "генерация Tinder профиля \uD83D\uDE0E", "/profile",
                    "сообщение для знакомства \uD83E\uDD70", "/opener",
                    "переписка от вашего имени \uD83D\uDE08", "/message",
                    "переписка со звездами \uD83D\uDD25", "/date",
                    "задать вопрос чату GPT \uD83E\uDDE0", "/gpt",
                    "остановить bot \uD83E\uDDE0", "/exit");
            return;
        }
        //endregion
        // region command DATE
        if (message.equals("/date")) {
            currentMode = DialogMode.DATE;
            sendPhotoMessage("date");
            sendTextButtonsMessage(loadMessage("date"),
                    "Ариана Гранде \uD83D\uDD25", "date_grande",
                    "Марго Робби \uD83D\uDD25\uD83D\uDD25", "date_robbie",
                    "Зендея     \uD83D\uDD25\uD83D\uDD25\uD83D\uDD25", "date_zendaya",
                    "Райан Гослинг \uD83D\uDE0E", "date_gosling",
                    "Том Харди \uD83D\uDE0E\uD83D\uDE0E", "date_hardy");
            return;
        }
        if (currentMode == DialogMode.DATE && !message.startsWith("/")) {
            String query = getCallbackQueryButtonKey();
            if (query.startsWith("date_")) {
                sendPhotoMessage(query);
                sendTextMessage("Отличный выбор!\nТвоя задача пригласить девушку/парня на свидание за 5 сообщений");
                String prompt = loadPrompt(query);
                chatGpt.setPrompt(prompt);
                return;
            }

            String answer = chatGpt.addMessage(message);
            sendTextMessage(answer);
            return;
        }
        //endregion
        //region command MESSAGE
        if (message.equals("/message")) {
            currentMode = DialogMode.MESSAGE;
            sendPhotoMessage("message");
            sendTextButtonsMessage("Пришлите в чат вашу переписку",
                    "Следующее сообщение", "message_next",
                    "Пригласить на свидание", "message_date");
            return;
        }

        if (currentMode == DialogMode.MESSAGE && !message.startsWith("/")) {
            String query = getCallbackQueryButtonKey();
            if (query.startsWith("message_")) {
                String userChatHistory = String.join("\n\n", list);

                Message msg = sendTextMessage("chatGPT думает");
                String answer = chatGpt.sendMessage(loadPrompt(query), userChatHistory);
                updateTextMessage(msg, answer);
                return;
            }

            list.add(message);
            return;
        }
        //endregion
        //region command PROFILE
        if (message.equals("/profile")) {
            currentMode = DialogMode.PROFILE;
            sendPhotoMessage("profile");
            me = new UserInfo();
            questionCount = 1;
            sendTextMessage("Сколько вам лет?");
            return;
        }

        if (currentMode == DialogMode.PROFILE && !message.startsWith("/")) {
            switch (questionCount) {
                case 1:
                    me.age = message;
                    questionCount = 2;
                    sendTextMessage("Кем вы работаете?");
                    return;
                case 2:
                    me.occupation = message;
                    questionCount = 3;
                    sendTextMessage("Ваше хобби?");
                    return;
                case 3:
                    me.hobby = message;
                    questionCount = 4;
                    sendTextMessage("Что вам НЕ нравится в людях?");
                    return;
                case 4:
                    me.annoys = message;
                    questionCount = 5;
                    sendTextMessage("Цель знакомства?");
                    return;
                case 5:
                    me.goals = message;
                    String aboutMyself = me.toString();
                    String answer = chatGpt.sendMessage(loadPrompt("profile"), aboutMyself);
                    Message msg = sendTextMessage("Подождите пару секунд...");
                    updateTextMessage(msg, answer);
                    return;
            }
            return;
        }
        //endregion
        //region command HELPER
        if (message.equals("/helper")) {
            currentMode = DialogMode.HELPER;
            sendPhotoMessage("opener");
            she = new UserInfo();
            questionCount = 1;
            sendTextMessage("Пришли информацию о человеке для знакомства:");
            sendTextMessage("Как её зовут?");
            return;
        }

        if (currentMode == DialogMode.HELPER && !message.startsWith("/")) {
            switch (questionCount) {
                case 1:
                    she.name = message;
                    questionCount = 2;
                    sendTextMessage("Сколько ей лет?");
                    return;
                case 2:
                    she.age = message;
                    questionCount = 3;
                    sendTextMessage("Какие у неё есть хобби?");
                    return;
                case 3:
                    she.hobby = message;
                    questionCount = 4;
                    sendTextMessage("Кем она работает?");
                    return;
                case 4:
                    she.occupation = message;
                    questionCount = 5;
                    sendTextMessage("Цель знакомства?");
                    return;
                case 5:
                    she.goals = message;
                    String prompt = loadPrompt("opener");
                    Message msg = sendTextMessage("Прикинем что можно написать...");
                    String answer = chatGpt.sendMessage(prompt, message);
                    updateTextMessage(msg, answer);
                    return;
            }
            return;
        }
        //endregion
        //region command GPT
        if (message.equals("/gpt")) {
            currentMode = DialogMode.GPT;
            sendPhotoMessage("gpt");
            sendTextMessage(loadMessage("gpt"));
            return;
        }

        if (currentMode == DialogMode.GPT && !message.startsWith("/")) {
            String prompt = loadPrompt("gpt");
            String answer = chatGpt.sendMessage(prompt, message);
            Message msg = sendTextMessage("chatGPT думает");
            updateTextMessage(msg, answer);
            return;
        }
        //endregion
        if (message.equals("/exit")){
            currentMode = DialogMode.DEFAULT;
        }
        //region HZ
        sendTextMessage("*Привет!*");
        sendTextMessage("_Привет!_");

        sendTextMessage("Вы написали " + message);

        sendTextButtonsMessage("Выберите режим работы:",
                "Старт", "start",
                "Стоп", "stop");
        //endregion
    }

    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new TinderBoltApp());
    }
}
