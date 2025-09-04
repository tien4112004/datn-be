package com.datn.datnbe.document.command.commandhandlers;

/** Base command handler interface */
@Deprecated
public interface BaseCommandHandler<T> {

    void handle(T command);
}
