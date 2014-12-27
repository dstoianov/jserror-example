package ru.yandex.qatools.jserror;

import net.lightbody.bmp.proxy.ProxyServer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.net.PortProber;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Parameterized.class)
public class JsErrorsJUnitTest {

    public static WebDriver driver;
    private static ProxyServer proxy = null;
    private static DesiredCapabilities caps;
    private static String proxyIp = "localhost";
    private String url;

    public JsErrorsJUnitTest(String url) {
        this.url = url;
    }

    @Parameterized.Parameters
    public static Collection<String[]> data() {
        List<String[]> urls = new ArrayList<String[]>();
        urls.add(new String[]{"http://www.yandex.com"});
        urls.add(new String[]{"http://codex.wordpress.org/Using_Your_Browser_to_Diagnose_JavaScript_Errors"});
        urls.add(new String[]{"http://getfirebug.com/errors"});
        return urls;
    }

    @BeforeClass
    public static void addInterceptor() throws Exception {
        int port = PortProber.findFreePort();
        proxy = new ProxyServer(port);
        proxy.start();

        proxy.setLocalHost(InetAddress.getByName(proxyIp));
        proxy.newHar("Testing");

        ScriptInjection.injectScriptRightAfterHeadTag(proxy, OnErrorHandler.SCRIPT);
        caps = new DesiredCapabilities();
        caps.setJavascriptEnabled(true);
        caps.setCapability(CapabilityType.PROXY, getProxy());

//        ScriptInjection.addProxyToCapabilities(caps, proxy.seleniumProxy());
        //    	driver = new HtmlUnitDriver(caps);


    }

    @AfterClass
    public static void stopProxy() throws Exception {
        if (proxy != null) {
            proxy.stop();
        }
    }

    public static Proxy getProxy() throws UnknownHostException {
        Proxy p = new Proxy();
        String PROXY = proxyIp + ":" + proxy.getPort();
        p.setProxyType(Proxy.ProxyType.MANUAL);
        p.setHttpProxy(PROXY).setSslProxy(PROXY);
        return p;
//        return proxy.seleniumProxy();
    }

    @Test
    public void shoudNotAppear() throws IOException {
        driver = new ChromeDriver(caps);
        driver.manage().window().maximize();

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
        if (driver != null) {
            driver.quit();
        }
    }
}
