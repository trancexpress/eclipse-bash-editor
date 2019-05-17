package de.jcup.basheditor.debug.launch;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TerminalLaunchContextBuilder {

    private InternalTerminalCommandStringBuilder itc = new InternalTerminalCommandStringBuilder();
    private CommandStringToCommandListConverter toListConverter = new CommandStringToCommandListConverter();
    private CommandStringVariableReplaceSupport variableReplaceSupport = new CommandStringVariableReplaceSupport();

    private File file;
    private String terminalCommand;
    private String params;
    
    private boolean waitingAlways;
    private boolean waitingOnErrors;
    private String starterTemplate;
    private TerminalLaunchContextBuilder() {
        
    }
    public static TerminalLaunchContextBuilder builder() {
        return new TerminalLaunchContextBuilder();
    }
    
    public TerminalLaunchContextBuilder file(File file) {
        this.file = file;
        return this;
    }
    
    public TerminalLaunchContextBuilder terminalCommand(String command) {
        this.terminalCommand=command;
        return this;
    }
    public TerminalLaunchContextBuilder starterCommand(String starterTemplate) {
        this.starterTemplate=starterTemplate;
        return this;
    }
    
    public TerminalLaunchContextBuilder params(String params) {
        this.params=params;
        return this;
    }
    
    public TerminalLaunchContextBuilder waitingAlways(boolean waitingAlways) {
        this.waitingAlways=waitingAlways;
        return this;
    }
    
    public TerminalLaunchContextBuilder waitingOnErrors(boolean waitingOnErrors) {
        this.waitingOnErrors=waitingOnErrors;
        return this;
    }
    
    public TerminalLaunchContext build() {
        TerminalLaunchContext context = new TerminalLaunchContext();
        context.file = file;
        context.params = params;
        context.terminalCommand = terminalCommand;
        context.waitAlways = waitingAlways;
        context.waitOnErrors = waitingOnErrors;
        context.switchToWorkingDirNecessary=true;
        context.commandString="";
        context.commands=new ArrayList<String>();
        context.startTemplate=starterTemplate;
        
        try{
            /* build command list */
            String internalCommand = itc.build(context);
            
            Map<String,String> map = new  HashMap<String,String>();
            map.put(TerminalCommandVariable.CMD_CALL.getId(), internalCommand);
            map.put(TerminalCommandVariable.CMD_TITLE.getId(), "Bash Editor DEBUG Session:"+file.getName());
            
            context.commandString= variableReplaceSupport.replaceVariables(context.terminalCommand, map);
            map.put(TerminalCommandVariable.CMD_TERMINAL.getId(), context.commandString);
            
            
            List<String> list = toListConverter.convert(context.startTemplate);
            for (String command: list) {
                context.commands.add(variableReplaceSupport.replaceVariables(command, map));
            }
            
        }catch(RuntimeException e) {
            context.exception=e;
        }
        return context;
    }
}
