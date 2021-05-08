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

    private final Map<Long, Set<String>> successForChangeSet = new HashMap<>();

    private final Map<String, String> changelog = new HashMap<>();
    private final Set<String> toChangeSet = new HashSet<>();
    private final Map<String, Integer> unSuccessToChangeSet = new HashMap<>();
    private final Lock getTaskForChangeLock = new ReentrantLock();
    private final Lock getTaskToChangeLock = new ReentrantLock();
    private boolean changeToMode = false;
    private Integer partSize = 0;

    private TaskDispatcher() {}

    public static TaskDispatcher getInstance() {
        return instance;
    }

    public void addTasks(List<String> tasks, List<Agent> agents) {
        changeToMode = false;
        clearTasks();
        forChangeTasks = distribute(agents.stream().map(Agent::getId).collect(Collectors.toList()), tasks);
    }

    public void addTasks(List<String> tasksForChange, List<String> tasksToChange, List<Agent> agents) {
        changeToMode = true;
        clearTasks();
        forChangeTasks = distribute(agents.stream().map(Agent::getId).collect(Collectors.toList()), tasksForChange);
        toChangeTasks = distribute(agents.stream().map(Agent::getId).collect(Collectors.toList()), tasksToChange);
        toChangeSet.addAll(tasksToChange);
    }

    public void closeTaskForChange(Long id, String task) {
        getTaskForChangeLock.lock();
        successForChangeSet.computeIfAbsent(id, k -> new HashSet<>());
        successForChangeSet.get(id).add(task);
        forChangeTasks.get(id).remove(task);
        if (forChangeTasks.get(id).isEmpty()) {
            reDistributeFor(id, forChangeTasks);
        }
        getTaskForChangeLock.unlock();
    }

    public boolean possiblySuccess(Long id, String task) {
        if (successForChangeSet.get(id) == null) {
            return false;
        }
        return successForChangeSet.get(id).contains(task);
    }

    public void reStore(Long id, String number) {
        getTaskForChangeLock.lock();
        forChangeTasks.get(id).add(number);
        String newNumb = changelog.get(number);
        if (newNumb != null && changeToMode) {
            toChangeTasks.get(id).add(newNumb);
            changelog.remove(number);
        }
        getTaskForChangeLock.unlock();
    }

    public void closeTaskToChange(Long id, String task) {
        getTaskToChangeLock.lock();
        toChangeTasks.get(id).remove(task);
        if (toChangeTasks.get(id).isEmpty()) {
            reDistributeFor(id, toChangeTasks);
        }
        getTaskToChangeLock.unlock();
    }

    public boolean hasForChangeTask(Long id) {
        return forChangeTasks.get(id).size() > 0;
    }

    public boolean isForChangeTask(Long id, String value) {
        return forChangeTasks.get(id).contains(value);
    }

    public boolean isChangeToMode() {
        return changeToMode;
    }

    public void addToChangelog(String from, String to) {
        changelog.put(from, to);
    }

    public Map<String, String> getChangelog() {
        return changelog;
    }

    public String getTaskToChange(Long id) {
        return toChangeTasks.get(id).stream().findFirst().orElse(null);
    }

    private void reDistributeFor(Long id, Map<Long, Set<String>> map) {
        int currentLoad = map.values().stream().map(Set::size).reduce(Integer::sum).orElse(0);
        if (currentLoad == 0) {
            return;
        }
        for (Long idInMap : map.keySet()) {
            if (idInMap.equals(id)) {
                continue;
            }
            int limit = map.get(idInMap).size() / 4;
            Set<String> part = map.get(idInMap).stream().limit(limit).collect(Collectors.toSet());
            if (!part.isEmpty()) {
                map.get(id).addAll(part);
                map.get(idInMap).removeAll(part);
            }
        }
    }

    private Map<Long, Set<String>> distribute(List<Long> idList, List<String> allTasks) {
        Map<Long, Set<String>> result = new HashMap<>();
        partSize = allTasks.size() / idList.size();
        int lastIndexOfTask = -1;
        int idCount = 0;
        for (Long id : idList) {
            lastIndexOfTask++;
            idCount++;
            result.computeIfAbsent(id, k -> new HashSet<>());
            int count = 0;
            for (int i = lastIndexOfTask; i < allTasks.size(); i++) {
                lastIndexOfTask = i;
                result.get(id).add(allTasks.get(i));
                count++;
                if (count >= partSize && idCount != idList.size()) {
                    break;
                }
            }
        }
        return result;
    }

    public Set<String> getToChangeSet() {
        return toChangeSet;
    }

    public void addUnSuccessToChange(String toChange) {
        unSuccessToChangeSet.putIfAbsent(toChange, 0);
        unSuccessToChangeSet.put(toChange, unSuccessToChangeSet.get(toChange)+1);
    }

    public Integer countOfUnSuccessToChange(String toChange) {
        return unSuccessToChangeSet.get(toChange);
    }

    public Set<String> getUnSuccessToChangeSet() {
        return unSuccessToChangeSet.keySet();
    }

    private void clearTasks() {
        forChangeTasks.clear();
        toChangeTasks.clear();
        toChangeSet.clear();
        unSuccessToChangeSet.clear();
        successForChangeSet.clear();
        partSize = 0;
    }
}
