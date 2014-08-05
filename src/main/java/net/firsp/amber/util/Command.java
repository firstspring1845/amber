package net.firsp.amber.util;

public class Command {

    String n;
    Runnable cmd;

    public Command(String name, Runnable command){
        n = name;
        cmd = command;
    }

    @Override
    public String toString(){
        return n;
    }

    public void execute(){
        cmd.run();
    }


}
