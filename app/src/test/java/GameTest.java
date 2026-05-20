import org.junit.Assert;
import org.junit.Test;
import thrones.game.GameOfThrones;
import thrones.game.utility.PropertiesLoader;

import java.util.Properties;

public class GameTest {
    @Test
    public void test1Feature1() {
        final Properties properties = PropertiesLoader.loadPropertiesFile("properties/test1.properties");
        GameOfThrones gameOfThrones = new GameOfThrones(properties);
        String logResult = gameOfThrones.runApp();
        Assert.assertTrue(logResult.contains("PileNORTH:9H,9S,6C,11D,7C"));
        Assert.assertTrue(logResult.contains("PileSOUTH:6H,7S,6S,9C,8S,13D,12S"));
        Assert.assertTrue(logResult.contains("Values:PileNORTH:12-27,PileSOUTH:15-37"));
        Assert.assertTrue(logResult.contains("Score:9,6,9,6"));

        Assert.assertTrue(logResult.contains("PileNORTH:4H,3S,11S,5S,13S,10D"));
        Assert.assertTrue(logResult.contains("PileSOUTH:13H,5C,8C,10S,12D,11C"));
        Assert.assertTrue(logResult.contains("Values:PileNORTH:4-12,PileSOUTH:43-0"));
        Assert.assertTrue(logResult.contains("Score:19,10,19,10"));
    }

    @Test
    public void test2Feature2() {
        // club for attack, spade for defend
        final Properties properties = PropertiesLoader.loadPropertiesFile("properties/test2.properties");
        GameOfThrones gameOfThrones = new GameOfThrones(properties);
        String logResult = gameOfThrones.runApp();
        Assert.assertTrue(logResult.contains("PileNORTH:7H,6C,7C,11D"));
        Assert.assertTrue(logResult.contains("PileSOUTH:6H,7S,9C,8S,13D,12S"));
        Assert.assertTrue(logResult.contains("Values:PileNORTH:10-7,PileSOUTH:15-31"));
        Assert.assertTrue(logResult.contains("Score:0,13,0,13"));

        Assert.assertTrue(logResult.contains("PileNORTH:4H,3S,5S,10D"));
        Assert.assertTrue(logResult.contains("PileSOUTH:13H,5C,8C,10S,8D,12D,11C"));
        Assert.assertTrue(logResult.contains("Values:PileNORTH:4-2,PileSOUTH:43-2"));
        Assert.assertTrue(logResult.contains("Score:10,17,10,17"));
    }

    @Test
    public void test3Feature2() {
        final Properties properties = PropertiesLoader.loadPropertiesFile("properties/test3.properties");
        GameOfThrones gameOfThrones = new GameOfThrones(properties);
        String logResult = gameOfThrones.runApp();
        Assert.assertTrue(logResult.contains("PileNORTH:7H,6C,7C,11D,12C"));
        Assert.assertTrue(logResult.contains("PileSOUTH:6H,7S,9C,8S,12S"));
        Assert.assertTrue(logResult.contains("Values:PileNORTH:30-7,PileSOUTH:15-31"));
        Assert.assertTrue(logResult.contains("Score:0,13,0,13"));

        Assert.assertTrue(logResult.contains("PileNORTH:4H,3S,11S,5S,2C,10D"));
        Assert.assertTrue(logResult.contains("PileSOUTH:13H,5C,8C,10S,12D,11C"));
        Assert.assertTrue(logResult.contains("Values:PileNORTH:0-22,PileSOUTH:43-0"));
        Assert.assertTrue(logResult.contains("Score:0,27,0,27"));
    }

    @Test
    public void test4Feature3() {
        final Properties properties = PropertiesLoader.loadPropertiesFile("properties/test4.properties");
        GameOfThrones gameOfThrones = new GameOfThrones(properties);
        String logResult = gameOfThrones.runApp();
        Assert.assertTrue(logResult.contains("PileNORTH:3H,12C,6S,6D,7C,11D,13S"));
        Assert.assertTrue(logResult.contains("PileSOUTH:6H,7S,10S,13D,12S"));
        Assert.assertTrue(logResult.contains("Values:PileNORTH:10-20,PileSOUTH:6-23"));
        Assert.assertTrue(logResult.contains("Score:3,6,3,6"));

        Assert.assertTrue(logResult.contains("PileNORTH:2H,6C,5S,5C,10D"));
        Assert.assertTrue(logResult.contains("PileSOUTH:10H,8C,4S,8D,8S,11C,12D"));
        Assert.assertTrue(logResult.contains("Values:PileNORTH:8-7,PileSOUTH:8-22"));
        Assert.assertTrue(logResult.contains("Score:3,18,3,18"));
    }

    @Test
    public void test5Feature3() {
        final Properties properties = PropertiesLoader.loadPropertiesFile("properties/test5.properties");
        GameOfThrones gameOfThrones = new GameOfThrones(properties);
        String logResult = gameOfThrones.runApp();
        Assert.assertTrue(logResult.contains("PileNORTH:3H,4C,6C,7C,6S"));
        Assert.assertTrue(logResult.contains("PileSOUTH:6H,8C,8D,8S,5C,7S"));
        Assert.assertTrue(logResult.contains("Values:PileNORTH:20-9,PileSOUTH:5-29"));
        Assert.assertTrue(logResult.contains("Score:3,6,3,6"));

        Assert.assertTrue(logResult.contains("PileNORTH:7H,3C,2S,5D,3S"));
        Assert.assertTrue(logResult.contains("PileSOUTH:10H,12C,2C,10S,13D,12S"));
        Assert.assertTrue(logResult.contains("Values:PileNORTH:10-7,PileSOUTH:32-20"));
        Assert.assertTrue(logResult.contains("Score:3,23,3,23"));
    }
}
