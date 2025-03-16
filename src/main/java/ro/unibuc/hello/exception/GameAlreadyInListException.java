package ro.unibuc.hello.exception;

public class GameAlreadyInListException extends RuntimeException {

    private static final String gameAlreadyInListTemplate = "Game: %s already in %s";

    public GameAlreadyInListException(String game, String listType) {
        super(String.format(gameAlreadyInListTemplate, game, listType));
    }

}