package agent.impl;

import agent.Agent;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import selenium_utils.SeleniumClient;
import selenium_utils.impl.SeleniumClientImpl;
import task_dispatcher.TaskDispatcher;
import utils.SeleniumUtils;
import utils.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AgentImpl implements Agent {

    private static final Lock lockForChooseNumber = new ReentrantLock();
    private static String lockedNumber = "";
    private Long id = 1L;
    private String login = "596007198779";
    private String password = "Jean273273";
    private SeleniumClient seleniumClient;
    private TaskDispatcher taskDispatcher = TaskDispatcher.getInstance();

    public AgentImpl() { }

    public AgentImpl(Long id, String login, String password) {
        this.id = id;
        this.login = login;
        this.password = password;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void run() {
        logic();
    }

    private void logic() {
        try {
            seleniumClient = new SeleniumClientImpl();
            loginAndGoToStartPage(seleniumClient);
            goToServices(seleniumClient);
            setPagination(seleniumClient);
            changeNumbersCycle(seleniumClient);
        } finally {
            endSession(seleniumClient);
        }
    }

    private void changeNumbersCycle(SeleniumClient seleniumClient) {
        focusOnTableOnServicesPage(seleniumClient);
        List<WebElement> rows;
        int rowInd = 0;
        do {
            rows = seleniumClient.getElementsFromFocus(By.className("list-of-services__ListOfServices__active"));
            if (rowInd >= rows.size()) {
                break;
            }
            WebElement row = rows.get(rowInd);
            String number = StringUtils.getOnlyNumbs(SeleniumUtils.getWebElementText(row));
            if (taskDispatcher.isForChangeTask(id, number)) {
                choosePhoneRow(row, seleniumClient);
                goToServiceManagementWindow(seleniumClient);
                goToChangeNumberMenu(seleniumClient);
                System.out.println(chooseNumberAndSubmit(seleniumClient, null));
                if (changeIsSuccessful(seleniumClient)) {
                    taskDispatcher.completeTaskForChange(id, number);
                }
                closeChangeNumberMenu(seleniumClient);
                closeServiceManagementWindow(seleniumClient);
                backToServicesPage(seleniumClient);
            }
            rowInd++;
        } while (rowInd < rows.size());
    }

    private void backToServicesPage(SeleniumClient seleniumClient) {
        seleniumClient
                .focus(By.id("3179985402"))
                .focus(By.name("HTML 3"))
                .scrollToFocus()
                .sleepSecs(1)
                .clickOnFocus()
                .sleepSecs(10)
                .unFocus();
    }

    private void closeServiceManagementWindow(SeleniumClient seleniumClient) {
        WebElement changePhoneWindow = seleniumClient
                .getElementsFromFocus(By.className("uni-DialogBox"))
                .stream()
                .filter(e -> Objects.equals(e.getAttribute("source"), "3186816388"))
                .findAny()
                .orElse(null);
        seleniumClient
                .focus(changePhoneWindow)
                .focus(By.className("i-close"))
                .clickOnFocus()
                .sleepSecs(2)
                .unFocus();
    }

    private void closeChangeNumberMenu(SeleniumClient seleniumClient) {
        WebElement changePhoneMenu = seleniumClient
                .getElementsFromFocus(By.className("uni-DialogBox"))
                .stream()
                .filter(e -> Objects.equals(e.getAttribute("source"), "3192157722"))
                .findAny()
                .orElse(null);
        seleniumClient
                .focus(changePhoneMenu)
                .focus(By.className("i-close"))
                .clickOnFocus()
                .sleepSecs(2)
                .unFocus();
    }

    private boolean changeIsSuccessful(SeleniumClient seleniumClient) {
        try {
            WebElement notifyDialogBox = seleniumClient
                    .getElementsFromFocus(By.className("uni-DialogBox"))
                    .stream()
                    .filter(e -> Objects.equals(e.getAttribute("source"), "unknown"))
                    .findAny()
                    .orElse(null);
            seleniumClient
                    .focus(notifyDialogBox)
                    .focus(By.className("dialogMiddleCenterInner"));
            WebElement notifyWindowLabel = seleniumClient
                    .focus(By.name("unidialogMessage"))
                    .getElementFromFocus(By.tagName("p"));
            String msg = SeleniumUtils.getWebElementText(notifyWindowLabel);
            seleniumClient
                    .sleepSecs(2)
                    .focus(notifyDialogBox)
                    .focus(By.className("dialogMiddleCenterInner"))
                    .focus(By.className("btn-popup-close"))
                    .clickOnFocus();
            return msg != null && msg.contains("Номер изменен");
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return false;
        } finally {
            seleniumClient.unFocus();
        }
    }

    private String chooseNumberAndSubmit(SeleniumClient seleniumClient, String required) {
        lockForChooseNumber.lock();
        try {
            List<WebElement> phoneRadioElements = seleniumClient
                    .focus(By.name("NumbersRadioPanel"))
                    .getElementsFromFocus(By.className("c-field-radio"));
            for (WebElement listElem : phoneRadioElements) {
                String phoneFromLabel = getPhoneNumbFromRadioElement(seleniumClient, listElem);
                if (!(Objects.equals(phoneFromLabel, lockedNumber))
                        && (Objects.equals(required, null) || Objects.equals(phoneFromLabel, required))) {
                    lockedNumber = phoneFromLabel;
                    chooseAndChange(seleniumClient, listElem);
                    return phoneFromLabel;
                }
            }
            return null;
        } finally {
            seleniumClient.unFocus();
            lockForChooseNumber.unlock();
        }
    }

    private void chooseAndChange(SeleniumClient seleniumClient, WebElement radio) {
        WebElement input = seleniumClient
                .focus(radio)
                .getElementFromFocus(By.className("c-field-label"));
        seleniumClient
                .focus(input)
                .scrollToFocus()
                .sleepSecs(2)
                .clickOnFocus()
                .unFocus()
                .sleepSecs(2);
        submitChange(seleniumClient);
    }

    private void submitChange(SeleniumClient seleniumClient) {
        seleniumClient
                .focus(By.name("change_phone_btn"))
                .clickOnFocus()
                .sleepSecs(3)
                .unFocus();
        WebElement changePhoneWindow = seleniumClient
                .getElementsFromFocus(By.className("uni-DialogBox"))
                .stream()
                .filter(e -> Objects.equals(e.getAttribute("source"), "3192239738"))
                .findAny()
                .orElse(null);
        seleniumClient
                .focus(changePhoneWindow)
                .focus(By.name("btn_yes_change_phone"))
                .clickOnFocus()
                .sleepSecs(5)
                .unFocus();
    }

    private String getPhoneNumbFromRadioElement(SeleniumClient seleniumClient, WebElement radio) {
        WebElement phoneLabel = seleniumClient
                .focus(radio)
                .getElementFromFocus(By.className("c-field-label"));
        return StringUtils.getOnlyNumbs(SeleniumUtils.getWebElementText(phoneLabel));
    }


    private void goToChangeNumberMenu(SeleniumClient seleniumClient) {
        seleniumClient
                .focus(By.className("dialogMiddleCenter"))
                .focus(By.name("html_arrow_gt"))
                .clickOnFocus()
                .sleepSecs(4)
                .unFocus();
    }

    private void goToServiceManagementWindow(SeleniumClient seleniumClient) {
        seleniumClient
                .focus(By.name("Текст 23"))
                .scrollToFocus()
                .sleepSecs(2)
                .clickOnFocus()
                .sleepSecs(2)
                .unFocus();
    }

    private void choosePhoneRow(WebElement row, SeleniumClient seleniumClient) {
        seleniumClient
                .focus(row)
                .scrollToFocus()
                .sleepSecs(1)
                .clickOnFocus()
                .sleepSecs(5)
                .unFocus();
    }

    private void setPagination(SeleniumClient seleniumClient) {
        seleniumClient
                .focus(By.name("serv_tab"))
                .focus(By.name("rtk-paginator"))
                .focus(By.name("rtk-dropdown"))
                .scrollToFocus()
                .sleepSecs(3)
                .clickOnFocus()
                .sleepSecs(1);
        List<WebElement> paginationElements = seleniumClient.getElementsFromFocus(By.className("multiselect__option"));
        for (WebElement webElement : paginationElements) {
            String innerText = SeleniumUtils.getWebElementText(webElement.findElement(By.tagName("span")));
            if (innerText != null && innerText.equals("100")) {
                seleniumClient
                        .focus(webElement)
                        .clickOnFocus()
                        .sleepSecs(5);
                break;
            }
        }
        seleniumClient.unFocus();
    }

    private void focusOnTableOnServicesPage(SeleniumClient seleniumClient) {
        seleniumClient
                .focus(By.name("serv_tab"))
                .focus(By.name("connected_services"))
                .focus(By.name("rtk-datagrid"))
                .focus(By.id("vgt-table"));
    }

    private void goToServices(SeleniumClient seleniumClient) {
        seleniumClient
                .focus(By.name("Услуги-tab-header"))
                .focus(By.className("tab1"))
                .clickOnFocus()
                .sleepSecs(10)
                .unFocus();
    }

    private void loginAndGoToStartPage(SeleniumClient seleniumClient) {
        seleniumClient
                .get("https://client.rt.ru/")
                .sleepSecs(2)
                .focus(By.id("username"))
                .sendKeysToFocus(login)
                .sleepSecs(1)
                .unFocus()
                .focus(By.id("password"))
                .sendKeysToFocus(password)
                .sleepSecs(1)
                .unFocus()
                .focus(By.id("kc-login"))
                .submitFocus()
                .sleepSecs(10)
                .unFocus();
    }

    private void endSession(SeleniumClient seleniumClient) {
        seleniumClient.close();
    }
}
