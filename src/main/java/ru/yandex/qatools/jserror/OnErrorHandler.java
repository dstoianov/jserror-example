package ru.yandex.qatools.jserror;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import java.util.ArrayList;
import java.util.List;

public class OnErrorHandler {
    private static final String VAR = "jserrors";

    /**
     * Скрипт, который делает две вещи:
     * 1) в объекте window создает глобальный динамический массив
     * 2) создает обработчик событий onerror, который складывает описание ошибок в вышеуказанный массив
     * <p/>
     * При перезагрузке страницы массив очищается.
     */
    public static final String SCRIPT = "<script type=\"text/javascript\">window." + VAR + "=[];" +
            "window.onerror=function(msg,url,ln){window." + VAR + ".push(msg+' at '+url+' on line '+ln)};</script>";

    private static final String RETURN_ERRORS = "return window." + VAR;

    /**
     * Возвращает список ошибок, обнаруженных с момента последней загрузки страницы.
     *
     * @param driver браузер
     * @return список ошибок
     */
    @SuppressWarnings("unchecked")
    public static List<String> getCurrentErrors(WebDriver driver) {
        if (driver == null) {
            throw new IllegalStateException("driver не может быть null!");
        }
        Object errors = ((JavascriptExecutor) driver).executeScript(RETURN_ERRORS);
        return errors == null ? new ArrayList<String>() : (List<String>) errors;
    }

    /**
     * Определяет, производится ли логирование ошибок по наличию переменной в window.
     *
     * @param driver браузер
     * @return true, если логирование производилось
     */
    @SuppressWarnings("unchecked")
    public static boolean isErrorLogged(WebDriver driver) {
        if (driver == null) {
            throw new IllegalStateException("driver не может быть null!");
        }
        Object errors = ((JavascriptExecutor) driver).executeScript(RETURN_ERRORS);
        return errors != null;
    }
}

