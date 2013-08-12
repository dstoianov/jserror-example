package ru.yandex.qatools.jserror;

import org.browsermob.proxy.ProxyServer;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.openqa.selenium.Platform;
import org.openqa.selenium.remote.DesiredCapabilities;

public class ProxyRule extends TestWatcher {
	private ProxyServer proxy;
	private DesiredCapabilities caps;	

	@Override
	protected void starting(Description description) {
		proxy = new ProxyServer(0);
		try {
			proxy.start();
			ScriptInjection.injectScriptRightAfterHeadTag(proxy, OnErrorHandler.SCRIPT);

			caps = new DesiredCapabilities("firefox", "", Platform.ANY);
			caps.setJavascriptEnabled(true);
			ScriptInjection.addProxyToCapabilities(caps, proxy.seleniumProxy());
		} catch (Exception e) {
			throw new RuntimeException("Error while starting proxy: " + e.getMessage());
		}  
	}
	
	@Override
	protected void finished(Description description) {
		try {
			proxy.stop();
		} catch (Exception e) {
			throw new RuntimeException("Error while stopping proxy: " + e.getMessage());
		}
	}
	
	public DesiredCapabilities getCaps() {
		return caps;
	}
}