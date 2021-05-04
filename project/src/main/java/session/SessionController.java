package session;

import agent.Agent;
import agent.impl.AgentImpl;
import selenium_utils.impl.WebDriverCreator;
import task_dispatcher.TaskDispatcher;
import utils.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class SessionController {

    private static SessionController instance = new SessionController();
    private Integer scrollSleep = 1;
    private Integer numbsRadioPanelLoadSleep = 10;
    private Integer sleepAfterCloseServiceManagement = 2;
    private Integer sleepAfterClosePhoneChange = 2;
    private Integer sleepAfterSendKeyToPhoneNumbTextBox = 1;
    private Integer sleepAfterClickOnPhoneChange = 3;
    private Integer sleepAfterSubmitPhoneChange = 5;
    private Integer sleepForLoadLoginPage = 2;
    private Integer sleepAfterLogin = 10;
    private Integer sleepForLoadChangeNumber = 5;
    private Integer sleepForLoadServiceManagement = 2;
    private Integer sleepForLoadServices = 10;
    private Integer sleepAfterChooseRow = 5;
    private Integer sleepAfterSetPagination = 5;
    private List<Agent> agents = new ArrayList<>();

    public static SessionController getInstance() {
        return instance;
    }

    public SessionController setScrollSleep(int scrollSleep) {
        this.scrollSleep = scrollSleep;
        return this;
    }

    public SessionController setNumbsRadioPanelLoadSleep(int numbsRadioPanelLoadSleep) {
        this.numbsRadioPanelLoadSleep = numbsRadioPanelLoadSleep;
        return this;
    }

    public SessionController setSleepAfterCloseServiceManagement(int sleepAfterCloseServiceManagement) {
        this.sleepAfterCloseServiceManagement = sleepAfterCloseServiceManagement;
        return this;
    }

    public SessionController setSleepAfterClosePhoneChange(int sleepAfterClosePhoneChange) {
        this.sleepAfterClosePhoneChange = sleepAfterClosePhoneChange;
        return this;
    }

    public SessionController setSleepAfterSendKeyToPhoneNumbTextBox(int sleepAfterSendKeyToPhoneNumbTextBox) {
        this.sleepAfterSendKeyToPhoneNumbTextBox = sleepAfterSendKeyToPhoneNumbTextBox;
        return this;
    }

    public SessionController setSleepAfterClickOnPhoneChange(int sleepAfterClickOnPhoneChange) {
        this.sleepAfterClickOnPhoneChange = sleepAfterClickOnPhoneChange;
        return this;
    }

    public SessionController setSleepAfterSubmitPhoneChange(int sleepAfterSubmitPhoneChange) {
        this.sleepAfterSubmitPhoneChange = sleepAfterSubmitPhoneChange;
        return this;
    }

    public SessionController setSleepForLoadLoginPage(int sleepForLoadLoginPage) {
        this.sleepForLoadLoginPage = sleepForLoadLoginPage;
        return this;
    }

    public SessionController setSleepAfterLogin(int sleepAfterLogin) {
        this.sleepAfterLogin = sleepAfterLogin;
        return this;
    }

    public SessionController setSleepForLoadChangeNumber(int sleepForLoadChangeNumber) {
        this.sleepForLoadChangeNumber = sleepForLoadChangeNumber;
        return this;
    }

    public SessionController setSleepForLoadServiceManagement(int sleepForLoadServiceManagement) {
        this.sleepForLoadServiceManagement = sleepForLoadServiceManagement;
        return this;
    }

    public SessionController setSleepForLoadServices(int sleepForLoadServices) {
        this.sleepForLoadServices = sleepForLoadServices;
        return this;
    }

    public SessionController setSleepAfterChooseRow(int sleepAfterChooseRow) {
        this.sleepAfterChooseRow = sleepAfterChooseRow;
        return this;
    }

    public SessionController setSleepAfterSetPagination(int sleepAfterSetPagination) {
        this.sleepAfterSetPagination = sleepAfterSetPagination;
        return this;
    }

    public void startSession(int agentsCount, String login, String password, String forChangeNumbsFilePath, String toChangeNumbsFilePath) {
        if (forChangeNumbsFilePath == null || agentsCount == 0) {
            return;
        }
        List<Agent> agents = createAgents(agentsCount, login, password);
        List<String> forChange = getNumbersFromFile(forChangeNumbsFilePath);
        if (toChangeNumbsFilePath != null) {
            List<String> toChange = getNumbersFromFile(toChangeNumbsFilePath);
            TaskDispatcher.getInstance().addTasks(forChange, toChange, agents);
        } else {
            TaskDispatcher.getInstance().addTasks(forChange, agents);
        }
        joinThreads(startThreads(agents));
    }

    public void emergencyEndSession() {
        WebDriverCreator.getInstance().closeAllDrivers();
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
                    scrollSleep,
                    numbsRadioPanelLoadSleep,
                    sleepAfterCloseServiceManagement,
                    sleepAfterClosePhoneChange,
                    sleepAfterSendKeyToPhoneNumbTextBox,
                    sleepAfterClickOnPhoneChange,
                    sleepAfterSubmitPhoneChange,
                    sleepForLoadLoginPage,
                    sleepAfterLogin,
                    sleepForLoadChangeNumber,
                    sleepForLoadServiceManagement,
                    sleepForLoadServices,
                    sleepAfterChooseRow,
                    sleepAfterSetPagination);
            result.add(agent);
        }
        return result;
    }

    private List<String> getNumbersFromFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new RuntimeException("Файла не существует!");
        }
        List<String> result = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                result.add(StringUtils.getOnlyNumbs(line));
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
