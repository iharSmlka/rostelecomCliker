package runner;

import agent.Agent;
import agent.impl.AgentImpl;
import task_dispatcher.TaskDispatcher;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        Agent agent = new AgentImpl();
        Thread agentThread = new Thread(agent);
        List<String> tasks = new ArrayList<>();
        tasks.add("79581915591");
        List<String> tasksToChange = new ArrayList<>();
        tasksToChange.add("79910946052");
        tasksToChange.add("79910946057");
        List<Agent> agents = new ArrayList<>();
        agents.add(agent);
        TaskDispatcher.getInstance().addTasks(tasks, tasksToChange, agents);
        agentThread.start();
        agentThread.join();
    }
}
