package com.datn.document.command.commandhandlers;

/**
 * Base command handler interface
 */
public interface BaseCommandHandler<T> {

    void handle(T command);

}
