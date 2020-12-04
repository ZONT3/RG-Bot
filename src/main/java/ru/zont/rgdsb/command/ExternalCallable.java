package ru.zont.rgdsb.command;

import ru.zont.rgdsb.tools.Commands;

public interface ExternalCallable {
    void call(Commands.Input input);
}
