package com.javarush.telegram;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class TinderBoltApp extends MultiSessionTelegramBot {
    public static final String TELEGRAM_BOT_NAME = Secure.TELEGRAM_BOT_NAME; //TODO: добавь имя бота в кавычках
    public static final String TELEGRAM_BOT_TOKEN = Secure.TELEGRAM_TOKEN; //TODO: добавь токен бота в кавычках
    public static final String OPEN_AI_TOKEN = Secure.GPT_TOKEN; //TODO: добавь токен ChatGPT в кавычках

    public TinderBoltApp() {
        super(TELEGRAM_BOT_NAME, TELEGRAM_BOT_TOKEN);
    }

    @Override
    public void onUpdateEventReceived(Update update) {
        //TODO: основной функционал бота будем писать здесь
        String message = getMessageText();

        if (message.equals("/start")){
            sendPhotoMessage("main");
            String text = loadMessage("main");
            sendTextMessage(text);
            return;
        }

        sendTextMessage("*Привет!*");
        sendTextMessage("_Привет!_");

        sendTextButtonsMessage("Выберите режим работы:",
                "Старт", "start",
                "Стоп", "stop");
    }

    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new TinderBoltApp());
    }
}
