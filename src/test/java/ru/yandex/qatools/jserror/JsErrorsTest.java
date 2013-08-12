package ru.yandex.qatools.jserror;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.After;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

@RunWith(Parameterized.class)
public class JsErrorsTest {
    @ClassRule
    public static ProxyRule proxy = new ProxyRule();
    
    private WebDriver driver = new HtmlUnitDriver(proxy.getCaps());
    
    private String url;
    
    @Parameterized.Parameters
    public static Collection<String[]> data() {
    	List<String[]> urls = new ArrayList<String[]>();
    	urls.add(new String[]{"http://www.yandex.com"});
    	return urls;
    }
    
    public JsErrorsTest(String url) {
    	this.url = url;
    }
    
    @Test
    public void shoudNotAppear() throws IOException {	
    	driver.get(url);
        List<String> errors = OnErrorHandler.getCurrentErrors(driver);
        assertThat("Detected " + errors.size() + " js-errors:" + collectErrorMessages(errors),
        		errors.size(), equalTo(0));
    }
    
    private String collectErrorMessages(List<String> errors) {
        StringBuilder message = new StringBuilder();
        for (String error : errors) {
            message.append("[").append(error).append("]");
        }
        return message.toString();
    }
    
    @After
    public void stopDriver() {
    	driver.close();
    	driver.quit();
    }
}
