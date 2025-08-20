package com.datn.datnbe.document.command.commandhandlers;

import com.datn.datnbe.document.command.commands.CreatePresentationCommand;
import org.springframework.stereotype.Component;

/**
 * Handler for CreatePresentationCommand
 */
@Deprecated
@Component
public class CreatePresentationCommandHandler implements BaseCommandHandler<CreatePresentationCommand> {

    @Override
    public void handle(CreatePresentationCommand command) {
        // Command handling logic will be implemented here
    }

}
