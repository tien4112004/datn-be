package com.datn.document.command.commandHandlers;

/**
 * Base command handler interface
 */
public interface BaseCommandHandler<T> {
    
    void handle(T command);
    
}
