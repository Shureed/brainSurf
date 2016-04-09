import org.apache.commons.collections.map.HashedMap;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.DoubleClickAction;

import java.sql.Time;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.lang.System;

/**
 * Created by reid on 4/9/16.
 */
public class Surfer {
    String currentSource;
    float score;
    WebDriver driver;
    Map<String,String> sources;
    Map<String, Map<String, Double>> content;
    public Surfer() throws Exception
    {
        driver = new FirefoxDriver();
        sources = new HashMap<String, String>();
        sources.put("reddit", "https://www.reddit.com/random");
        sources.put("imgur", "https://www.imgur.com/random");

        content = new HashMap<String, Map<String, Double>>();

        content.put("imgur", new HashedMap());
        content.put("reddit", new HashedMap());

        currentSource = "imgur";
    }

    void nextContent()
    {
        driver.get(sources.get(currentSource));
    }

    void changeSource(String source)
    {
        currentSource = source;
    }

    void start()
    {
        score = System.currentTimeMillis();
        nextContent();
    }

    void next() throws Exception
    {
        score = System.currentTimeMillis() - score;
        content.get(currentSource).put(driver.getCurrentUrl(), (double) score);
        nextContent();
        score = System.currentTimeMillis();
        System.out.println(driver.getCurrentUrl());
    }
}