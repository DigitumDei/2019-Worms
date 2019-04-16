package za.co.entelect.challenge.botrunners;

import za.co.entelect.challenge.engine.exceptions.InvalidRunnerState;
import za.co.entelect.challenge.config.BotMetaData;

public class BotRunnerFactory {
    public static BotRunner createBotRunner(BotMetaData botMetaData, int timeoutInMilliseconds) throws InvalidRunnerState {
        switch (botMetaData.getBotLanguage()){
            case JAVA:
                return new JavaBotRunner(botMetaData, timeoutInMilliseconds);
            case DOTNETCORE:
                return new DotNetCoreBotRunner(botMetaData, timeoutInMilliseconds);
            case JAVASCRIPT:
                return new JavaScriptBotRunner(botMetaData, timeoutInMilliseconds);
            case PYTHON3:
                return new Python3BotRunner(botMetaData, timeoutInMilliseconds);
            default:
                break;
        }
        throw new InvalidRunnerState("Invalid bot language");
    }
}
