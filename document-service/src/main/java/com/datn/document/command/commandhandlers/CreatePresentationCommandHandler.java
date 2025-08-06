package com.datn.document.command.commandhandlers;

import com.datn.document.command.commands.CreatePresentationCommand;
import org.springframework.stereotype.Component;

/**
 * Handler for CreatePresentationCommand
 */
@Component
public class CreatePresentationCommandHandler implements BaseCommandHandler<CreatePresentationCommand> {
    
    @Override
    public void handle(CreatePresentationCommand command) {
        // Command handling logic will be implemented here
    }
    
}
