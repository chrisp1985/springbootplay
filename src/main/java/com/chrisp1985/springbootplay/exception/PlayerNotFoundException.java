package com.chrisp1985.springbootplay.exception;

public class PlayerNotFoundException extends RuntimeException {

    public PlayerNotFoundException(String name) {
        super("No player found with name: " + name);
    }
}
