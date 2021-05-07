package agent.impl;

import agent.Agent;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
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

@Getter
@Slf4j
public class AgentImpl implements Agent {

    private static final Lock lockForChooseNumber = new ReentrantLock();
    private static String lockedNumber = "";
    private Long id = 1L;
    private String login = "596007198779";
    private String password = "Jean273273";
    private SeleniumClient seleniumClient;
    private final TaskDispatcher taskDispatcher = TaskDispatcher.getInstance();

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

    private Integer errorLimit = 10;

    private boolean close = false;

    public AgentImpl() { }

    public AgentImpl(Long id, String login, String password) {
        this.id = id;
        this.login = login;
        this.password = password;
    }

    public AgentImpl(Long id,
                     String login,
                     String password,
                     Integer scrollSleep,
                     Integer numbsRadioPanelLoadSleep,
                     Integer sleepAfterCloseServiceManagement,
                     Integer sleepAfterClosePhoneChange,
                     Integer sleepAfterSendKeyToPhoneNumbTextBox,
                     Integer sleepAfterClickOnPhoneChange,
                     Integer sleepAfterSubmitPhoneChange,
                     Integer sleepForLoadLoginPage,
                     Integer sleepAfterLogin,
                     Integer sleepForLoadChangeNumber,
                     Integer sleepForLoadServiceManagement,
                     Integer sleepForLoadServices,
                     Integer sleepAfterChooseRow,
                     Integer sleepAfterSetPagination,
                     Integer errorLimit) {
        this.id = id;
        this.login = login;
        this.password = password;
        this.scrollSleep = scrollSleep;
        this.numbsRadioPanelLoadSleep = numbsRadioPanelLoadSleep;
        this.sleepAfterCloseServiceManagement = sleepAfterCloseServiceManagement;
        this.sleepAfterClosePhoneChange = sleepAfterClosePhoneChange;
        this.sleepAfterSendKeyToPhoneNumbTextBox = sleepAfterSendKeyToPhoneNumbTextBox;
        this.sleepAfterClickOnPhoneChange = sleepAfterClickOnPhoneChange;
        this.sleepAfterSubmitPhoneChange = sleepAfterSubmitPhoneChange;
        this.sleepForLoadLoginPage = sleepForLoadLoginPage;
        this.sleepAfterLogin = sleepAfterLogin;
        this.sleepForLoadChangeNumber = sleepForLoadChangeNumber;
        this.sleepForLoadServiceManagement = sleepForLoadServiceManagement;
        this.sleepForLoadServices = sleepForLoadServices;
        this.sleepAfterChooseRow = sleepAfterChooseRow;
        this.sleepAfterSetPagination = sleepAfterSetPagination;
        this.errorLimit = errorLimit;
    }

    @Override
    public void run() {
        logic(0);
        endSession(seleniumClient);
    }

    private void logic(int count) {
        if (count >= errorLimit || close) {
            log.error("Вышли в лимит");
            return;
        }
        try {
            seleniumClient = new SeleniumClientImpl();
            loginAndGoToStartPage(seleniumClient);
            goToServices(seleniumClient);
            setPagination(seleniumClient);
            changeNumbersCycle(seleniumClient);
        } catch (Exception e) {
            log.error("Ошибка ", e);
            endSession(seleniumClient);
            logic(count + 1);
        }
    }

    private void changeNumbersCycle(SeleniumClient seleniumClient) {
        seleniumClient
                .focus(By.name("serv_tab"))
                .focus(By.name("connected_services"))
                .focus(By.name("rtk-datagrid"))
                .focus(By.id("vgt-table"));
        List<WebElement> rows;
        do {
            rows = seleniumClient.getElementsFromFocus(By.className("list-of-services__ListOfServices__active"));
            if (!taskDispatcher.hasForChangeTask(id)) {
                break;
            }
            WebElement row = rows.stream().filter(
                    r -> taskDispatcher.isForChangeTask(id, StringUtils.getPhoneNumb(SeleniumUtils.getWebElementText(r))))
                    .findAny()
                    .orElse(null);
            if (row != null) {
                String number = StringUtils.getPhoneNumb(SeleniumUtils.getWebElementText(row));
                choosePhoneRow(row, seleniumClient);
                goToServiceManagementWindow(seleniumClient);
                goToChangeNumberMenu(seleniumClient);
                if (taskDispatcher.isChangeToMode()) {
                    boolean isChangeError = false;
                    do {
                        String numberToChange = taskDispatcher.getTaskToChange(id);
                        if (numberToChange == null) {
                            break;
                        }
                        setLastFourSymbolsFromTaskToChange(seleniumClient, numberToChange);
                        String changed = chooseNumberAndSubmit(seleniumClient, numberToChange);
                        if (changed == null || !checkChangeIsSuccessfulAndCloseWindows(seleniumClient)) {
                            isChangeError = true;
                            taskDispatcher.addUnSuccessToChange(numberToChange);
                        } else {
                            taskDispatcher.addToChangelog(number, changed);
                        }
                        taskDispatcher.closeTaskToChange(id, numberToChange);
                    } while (isChangeError);
                } else {
                    String changed = chooseNumberAndSubmit(seleniumClient, null);
                    if (changed != null) {
                        if (checkChangeIsSuccessfulAndCloseWindows(seleniumClient)) {
                            taskDispatcher.addToChangelog(number, changed);
                        }
                    }
                }
                closeChangeNumberMenu(seleniumClient);
                closeServiceManagementWindow(seleniumClient);
                backToServicesPage(seleniumClient);
                taskDispatcher.closeTaskForChange(id, number);
            } else {
                break;
            }
        } while (taskDispatcher.hasForChangeTask(id));
    }

    private void backToServicesPage(SeleniumClient seleniumClient) {
        seleniumClient
                .focus(By.id("3179985402"))
                .focus(By.name("HTML 3"))
                .scrollToFocus()
                .sleepSecs(scrollSleep)
                .clickOnFocus()
                .sleepSecs(numbsRadioPanelLoadSleep)
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
                .sleepSecs(sleepAfterCloseServiceManagement)
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
                .sleepSecs(sleepAfterClosePhoneChange)
                .unFocus();
    }

    private boolean checkChangeIsSuccessfulAndCloseWindows(SeleniumClient seleniumClient) {
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
                    .sleepSecs(scrollSleep)
                    .focus(notifyDialogBox)
                    .focus(By.className("dialogMiddleCenterInner"))
                    .focus(By.className("btn-popup-close"))
                    .clickOnFocus();
            return msg != null && msg.contains("Номер изменен");
        } catch (Exception e) {
            log.error("Ошибка при проверке статуса смены ", e);
            return false;
        } finally {
            seleniumClient.unFocus();
        }
    }

    private void setLastFourSymbolsFromTaskToChange(SeleniumClient seleniumClient, String taskToChange) {
        String last4Symbols = StringUtils.getLastFourNumbs(taskToChange);
        List<WebElement> inputDivList = seleniumClient
                .getElementsFromFocus(By.className("phone_end"));
        for (WebElement inputDiv : inputDivList) {
            WebElement input = seleniumClient
                    .focus(inputDiv)
                    .getElementFromFocus(By.tagName("input"));
            int index = Integer.parseInt(input.getAttribute("tabindex"));
            seleniumClient
                    .focus(input)
                    .clearFocus()
                    .sendKeysToFocus(String.valueOf(last4Symbols.charAt(index - 1)))
                    .sleepSecs(sleepAfterSendKeyToPhoneNumbTextBox);
        }
        seleniumClient.unFocus();
        seleniumClient
                .focus(By.name("find_btn"))
                .clickOnFocus();
        seleniumClient.sleepSecs(numbsRadioPanelLoadSleep);
        seleniumClient.unFocus();
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
        } catch (Exception e) {
            log.error("Ошибка при попытке смены номера ", e);
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
                .sleepSecs(scrollSleep)
                .clickOnFocus()
                .unFocus()
                .sleepSecs(scrollSleep);
        submitChange(seleniumClient);
    }

    private void submitChange(SeleniumClient seleniumClient) {
        seleniumClient
                .focus(By.name("change_phone_btn"))
                .clickOnFocus()
                .sleepSecs(sleepAfterClickOnPhoneChange)
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
                .sleepSecs(sleepAfterSubmitPhoneChange)
                .unFocus();
    }

    private String getPhoneNumbFromRadioElement(SeleniumClient seleniumClient, WebElement radio) {
        WebElement phoneLabel = seleniumClient
                .focus(radio)
                .getElementFromFocus(By.className("c-field-label"));
        return StringUtils.getPhoneNumb(SeleniumUtils.getWebElementText(phoneLabel));
    }


    private void goToChangeNumberMenu(SeleniumClient seleniumClient) {
        seleniumClient
                .focus(By.className("dialogMiddleCenter"))
                .focus(By.name("html_arrow_gt"))
                .clickOnFocus()
                .sleepSecs(sleepForLoadChangeNumber)
                .unFocus();
    }

    private void goToServiceManagementWindow(SeleniumClient seleniumClient) {
        seleniumClient
                .focus(By.name("Текст 23"))
                .scrollToFocus()
                .sleepSecs(scrollSleep)
                .clickOnFocus()
                .sleepSecs(sleepForLoadServiceManagement)
                .unFocus();
    }

    private void choosePhoneRow(WebElement row, SeleniumClient seleniumClient) {
        seleniumClient
                .focus(row)
                .scrollToFocus()
                .sleepSecs(scrollSleep)
                .clickOnFocus()
                .sleepSecs(sleepAfterChooseRow)
                .unFocus();
    }

    private void setPagination(SeleniumClient seleniumClient) {
        seleniumClient
                .focus(By.name("serv_tab"))
                .focus(By.name("rtk-paginator"))
                .focus(By.name("rtk-dropdown"))
                .scrollToFocus()
                .sleepSecs(scrollSleep)
                .clickOnFocus()
                .sleepSecs(scrollSleep);
        List<WebElement> paginationElements = seleniumClient.getElementsFromFocus(By.className("multiselect__option"));
        for (WebElement webElement : paginationElements) {
            String innerText = SeleniumUtils.getWebElementText(webElement.findElement(By.tagName("span")));
            if (innerText != null && innerText.equals("100")) {
                seleniumClient
                        .focus(webElement)
                        .clickOnFocus()
                        .sleepSecs(sleepAfterSetPagination);
                break;
            }
        }
        seleniumClient.unFocus();
    }

    private void goToServices(SeleniumClient seleniumClient) {
        seleniumClient
                .focus(By.name("Услуги-tab-header"))
                .focus(By.className("tab1"))
                .clickOnFocus()
                .sleepSecs(sleepForLoadServices)
                .unFocus();
    }

    private void loginAndGoToStartPage(SeleniumClient seleniumClient) {
        seleniumClient
                .get("https://client.rt.ru/")
                .sleepSecs(sleepForLoadLoginPage)
                .focus(By.id("username"))
                .sendKeysToFocus(login)
                .sleepSecs(sleepAfterSendKeyToPhoneNumbTextBox)
                .unFocus()
                .focus(By.id("password"))
                .sendKeysToFocus(password)
                .sleepSecs(sleepAfterSendKeyToPhoneNumbTextBox)
                .unFocus()
                .focus(By.id("kc-login"))
                .submitFocus()
                .sleepSecs(sleepAfterLogin)
                .unFocus();
    }

    private void endSession(SeleniumClient seleniumClient) {
        try {
            seleniumClient.close();
        } catch (Exception e) {
            log.error("Ошибка закрытия браузера ", e);
        }
    }
}
