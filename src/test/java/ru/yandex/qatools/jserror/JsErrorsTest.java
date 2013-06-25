package ru.yandex.qatools.jserror;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.browsermob.proxy.ProxyServer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

@RunWith(Parameterized.class)
public class JsErrorsTest {
    private String url;
    private static ProxyServer proxy;
    private WebDriver driver;
    private static DesiredCapabilities caps;
    
    @Parameterized.Parameters
    public static Collection<String[]> data() {
    	List<String[]> urls = new ArrayList<String[]>();
    	urls.add(new String[]{"http://www.yandex.com"});
    	return urls;
    }
    
    public JsErrorsTest(String url) {
    	this.url = url;
    }
    
    @BeforeClass
    public static void addInterceptor() throws Exception {
        proxy = new ProxyServer(0);
        proxy.start();
        ScriptInjection.injectScriptRightAfterHeadTag(proxy, OnErrorHandler.SCRIPT);
        caps = new DesiredCapabilities("firefox", "", Platform.ANY);
        caps.setJavascriptEnabled(true);
        ScriptInjection.addProxyToCapabilities(caps, proxy.seleniumProxy());
    }
    
    @Test
    public void shoudNotAppear() throws IOException {
    	driver = new HtmlUnitDriver(caps);
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
    
    @AfterClass
    public static void stopProxy() throws Exception {
    	proxy.stop();
    }
}
