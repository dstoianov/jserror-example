package ru.yandex.qatools.jserror;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import com.opera.core.systems.OperaProfile;

import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.protocol.HttpContext;
import org.browsermob.proxy.ProxyServer;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

public class ScriptInjection {
    private static final String HEAD = "<head>";
    private static final String SCRIPT = "<script";
    
    public static void addProxyToCapabilities(DesiredCapabilities caps, Proxy proxy) {
        caps.setCapability(CapabilityType.PROXY, proxy);
        if ("chrome".equals(caps.getBrowserName())) {
            @SuppressWarnings("unchecked")
            List<String> switches = (ArrayList<String>) caps.getCapability("chrome.switches");
            if (switches == null) {
                switches = new ArrayList<String>();
            }
            switches.add("--proxy-server=" + proxy.getHttpProxy());
            caps.setCapability("chrome.switches", switches);
        }
        if ("opera".equals(caps.getBrowserName())) {
            OperaProfile profile = new OperaProfile();
            profile.preferences().set("Proxy", "HTTP server", proxy.getHttpProxy());
            profile.preferences().set("Proxy", "Use HTTP", "1");
            caps.setCapability("opera.profile", profile);
        }
    }

    public static void injectScriptRightAfterHeadTag(ProxyServer proxy, final String script) throws Exception {
        proxy.addResponseInterceptor(new HttpResponseInterceptor() {
            public void process(HttpResponse response, HttpContext context) throws HttpException, IOException {
                Header[] headers = response.getHeaders("Content-Type");
                boolean isHtml = false;
                for (Header header : headers) {
                    if (header.getValue().startsWith("text/html")) {
                        isHtml = true;
                        break;
                    }
                }

                if (!isHtml) {
                    return;
                }

                // мы увеличиваем размер кода, поэтому этот заголовок смысла не имеет
                response.removeHeaders("Content-Length");

                BasicHttpEntity en = (BasicHttpEntity) response.getEntity();

                if (!"gzip".equals(en.getContentEncoding().getValue())) {
                    return;
                }

                BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(en.getContent())));
                // вычитываем текст из потока, по ходу делая ungzip
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                br.close();

                int firstScript = sb.indexOf(SCRIPT);
                if (firstScript >= 0) {
                    sb.insert(firstScript, script);
                } else {
                    // вставляем в <head> скрипт-хэндлер на первую позицию
                    int head = sb.indexOf(HEAD);
                    if (head < 0) {
                        return;
                    }
                    sb.insert(head + HEAD.length(), script);
                }

                // выполняем gzip измененной строки
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                GZIPOutputStream gzos = new GZIPOutputStream(baos);
                gzos.write(sb.toString().getBytes());
                gzos.close();

                // используем промежуточный буфер, что не лучший вариант
                InputStream newContent = new ByteArrayInputStream(baos.toByteArray());
                en.setContent(newContent);
            }
        });
    }
}