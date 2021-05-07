package session;

import agent.Agent;
import agent.impl.AgentImpl;
import lombok.AllArgsConstructor;
import selenium_utils.impl.WebDriverCreator;
import task_dispatcher.TaskDispatcher;
import utils.StringUtils;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class SessionController {

    @AllArgsConstructor
    private enum Vars {
        SCROLL("Пауза_при_скроллинге", 1),
        NUMBS_RADIO_PANEL_LOAD("Пауза_при_загрузке_панели_выбора_номеров", 10),
        CLOSE_SERVICE_MANAGER("Пауза_после_закрытия_окна_управления_услугой", 2),
        CLOSE_PHONE_CHANGE("Пауза_после_закрытия_окна_смены_номера", 2),
        SEND_KEY("Пауза_после_ввода_текста", 1),
        CLICK_ON_PHONE_CHANGE("Пауза_после_нажатия_на_смену_номера", 3),
        SUBMIT_PHONE_CHANGE("Пауза_после_подтверждения_смены_номера", 5),
        LOAD_LOGIN_PAGE("Пауза_для_загрузки_окна_логина", 2),
        AFTER_LOGIN("Пауза_после_логина", 10),
        LOAD_CHANGE_NUMBER("Пауза_при_загрузке_окна_смены_номера", 5),
        LOAD_SERVICE_MANAGER("Пауза_для_загрузки_окна_управления_услугой", 2),
        LOAD_SERVICES("Пауза_при_загрузке_списка_услуг", 10),
        CHOOSE_ROW("Пауза_при_выборе_номера", 5),
        SET_PAGINATION("Пауза_после_установки_пагинации", 5);
        String keyInFile;
        Integer val;

        void set(int val) {
            this.val = val;
        }
    }

    private static SessionController instance = new SessionController();
    private List<Agent> agents = new ArrayList<>();
    private String toChangeFilePath = null;

    public static SessionController getInstance() {
        return instance;
    }

    public void startSession(int agentsCount, String login, String password, String forChangeNumbsFilePath, String toChangeNumbsFilePath) {
        toChangeFilePath = null;
        if (forChangeNumbsFilePath == null || forChangeNumbsFilePath.isEmpty() || agentsCount == 0) {
            return;
        }
        setParams("params.txt");
        List<Agent> agents = createAgents(agentsCount, login, password);
        List<String> forChange = getLinesFromFile(forChangeNumbsFilePath).stream().map(StringUtils::getPhoneNumb).collect(Collectors.toList());
        if (toChangeNumbsFilePath != null && !toChangeNumbsFilePath.isEmpty()) {
            toChangeFilePath = toChangeNumbsFilePath;
            List<String> toChange = getLinesFromFile(toChangeNumbsFilePath).stream().map(StringUtils::getPhoneNumb).collect(Collectors.toList());
            TaskDispatcher.getInstance().addTasks(forChange, toChange, agents);
        } else {
            TaskDispatcher.getInstance().addTasks(forChange, agents);
        }
        joinThreads(startThreads(agents));
    }

    public void emergencyEndSession() {
        WebDriverCreator.getInstance().closeAllDrivers();
    }

    private void setParams(String filePath) {
        List<String> params = getLinesFromFile(filePath);
        for (String str : params) {
            String[] parts = str.split("=");
            for (Vars var : Vars.values()) {
                if (var.keyInFile.equals(parts[0])) {
                    var.set(Integer.parseInt(parts[1]));
                }
            }
        }
    }

    public void createLogFile() throws Exception {
        Map<String, String> changelog = TaskDispatcher.getInstance().getChangelog();
        StringBuilder result = new StringBuilder();
        for (String key : changelog.keySet()) {
            result.append(key).append(" -> ").append(changelog.get(key)).append("\n");
        }
        createFileWithText(UUID.randomUUID().toString(), result.toString());
    }

    public void rewriteToChangeFile() throws Exception {
        if (toChangeFilePath == null) {
            return;
        }
        Set<String> toChangeSet = TaskDispatcher.getInstance().getToChangeSet();
        Map<String, String> changelog = TaskDispatcher.getInstance().getChangelog();
        Set<String> changed = new HashSet<>(changelog.values());
        Set<String> notChanged = new HashSet<>(toChangeSet);
        notChanged.removeAll(changed);
        StringBuilder result = new StringBuilder();
        for (String str : notChanged) {
            result.append(str).append("\n");
        }
        reWriteFile(toChangeFilePath, result.toString());
    }

    private void joinThreads(List<Thread> threads) {
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private List<Thread> startThreads(List<Agent> agents) {
        List<Thread> threads = new ArrayList<>();
        for (Agent agent : agents) {
            Thread thread = new Thread(agent);
            threads.add(thread);
            thread.start();
        }
        return threads;
    }

    private List<Agent> createAgents(int size, String login, String password) {
        List<Agent> result = new ArrayList<>();
        for (long i = 0L; i < size; i++) {
            Agent agent = new AgentImpl(i,
                    login,
                    password,
                    Vars.SCROLL.val,
                    Vars.NUMBS_RADIO_PANEL_LOAD.val,
                    Vars.CLOSE_SERVICE_MANAGER.val,
                    Vars.CLOSE_PHONE_CHANGE.val,
                    Vars.SEND_KEY.val,
                    Vars.CLICK_ON_PHONE_CHANGE.val,
                    Vars.SUBMIT_PHONE_CHANGE.val,
                    Vars.LOAD_LOGIN_PAGE.val,
                    Vars.AFTER_LOGIN.val,
                    Vars.LOAD_CHANGE_NUMBER.val,
                    Vars.LOAD_SERVICE_MANAGER.val,
                    Vars.LOAD_SERVICES.val,
                    Vars.CHOOSE_ROW.val,
                    Vars.SET_PAGINATION.val,
                    10);
            result.add(agent);
        }
        return result;
    }

    private List<String> getLinesFromFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new RuntimeException("Файла не существует!");
        }
        List<String> result = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                result.add(line);
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void createFileWithText(String filePath, String text) throws Exception {
        File newFile = new File(filePath);
        if (newFile.createNewFile()) {
            try (FileWriter myWriter = new FileWriter(newFile)) {
                myWriter.write(text);
            }
        }
    }

    private void reWriteFile(String filePath, String text) throws Exception {
        File file = new File(filePath);
        try (FileWriter writer = new FileWriter(file, false)) {
            writer.write(text);
        }
    }
}
