package task_dispatcher;

import agent.Agent;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class TaskDispatcher {

    private static final TaskDispatcher instance = new TaskDispatcher();
    private Map<Long, Set<String>> forChangeTasks = new HashMap<>();
    private Map<Long, Set<String>> toChangeTasks = new HashMap<>();
    private final Lock getTaskForChangeLock = new ReentrantLock();
    private final Lock getTaskToChangeLock = new ReentrantLock();
    private Integer partSize = 0;

    private TaskDispatcher() {}

    public static TaskDispatcher getInstance() {
        return instance;
    }

    public void addTasks(List<String> tasks, List<Agent> agents) {
        forChangeTasks = distribute(agents.stream().map(Agent::getId).collect(Collectors.toList()), tasks);
    }

    public void completeTaskForChange(Long id, String task) {
        getTaskForChangeLock.lock();
        forChangeTasks.get(id).remove(task);
        if (forChangeTasks.isEmpty()) {
            reDistributeFor(id, forChangeTasks);
        }
        getTaskForChangeLock.unlock();
    }

    public void completeTaskToChange(Long id, String task) {
        getTaskToChangeLock.lock();
        toChangeTasks.get(id).remove(task);
        if (toChangeTasks.isEmpty()) {
            reDistributeFor(id, toChangeTasks);
        }
        getTaskToChangeLock.unlock();
    }

    public boolean isForChangeTask(Long id, String value) {
        return forChangeTasks.get(id).contains(value);
    }

    public boolean isToChangeTask(Long id, String value) {
        return toChangeTasks.get(id).contains(value);
    }

    private void reDistributeFor(Long id, Map<Long, Set<String>> map) {
        if (map.keySet().size() <= 1) {
            return;
        }
        int currentLoad = map.values().stream().map(Set::size).reduce(Integer::sum).orElse(0);
        if (currentLoad == 0) {
            return;
        }
        int toFetchSize = (currentLoad / map.keySet().size()-1) / partSize;
        for (Long idInMap : map.keySet()) {
            if (idInMap.equals(id)) {
                continue;
            }
            Set<String> part = map.get(idInMap).stream().limit(toFetchSize).collect(Collectors.toSet());
            map.get(id).addAll(part);
            map.get(idInMap).removeAll(part);
        }
    }

    private Map<Long, Set<String>> distribute(List<Long> idList, List<String> allTasks) {
        Map<Long, Set<String>> result = new HashMap<>();
        partSize = allTasks.size() / idList.size();
        int lastIndexOfTask = -1;
        for (Long id : idList) {
            lastIndexOfTask++;
            result.computeIfAbsent(id, k -> new HashSet<>());
            int count = 0;
            for (int i = lastIndexOfTask; i < allTasks.size(); i++) {
                lastIndexOfTask = i;
                result.get(id).add(allTasks.get(i));
                count++;
                if (count >= partSize) {
                    break;
                }
            }
        }
        return result;
    }

    private void clearTasks() {
        forChangeTasks.clear();
        toChangeTasks.clear();
        partSize = 0;
    }
}
