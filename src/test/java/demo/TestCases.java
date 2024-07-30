package demo;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
// import io.github.bonigarcia.wdm.WebDriverManager;
import demo.wrappers.Wrappers;

public class TestCases {
    ChromeDriver driver;

    /*
     * TODO: Write your tests here with testng @Test annotation. 
     * Follow `testCase01` `testCase02`... format or what is provided in instructions
     */

     
    /*
     * Do not change the provided methods unless necessary, they will help in automation and assessment
     */
    @BeforeTest
    public void startBrowser()
    {
        System.setProperty("java.util.logging.config.file", "logging.properties");

        // NOT NEEDED FOR SELENIUM MANAGER
        // WebDriverManager.chromedriver().timeout(30).setup();

        ChromeOptions options = new ChromeOptions();
        LoggingPreferences logs = new LoggingPreferences();

        logs.enable(LogType.BROWSER, Level.ALL);
        logs.enable(LogType.DRIVER, Level.ALL);
        options.setCapability("goog:loggingPrefs", logs);
        options.addArguments("--remote-allow-origins=*");

        System.setProperty(ChromeDriverService.CHROME_DRIVER_LOG_PROPERTY, "build/chromedriver.log"); 

        driver = new ChromeDriver(options);

        driver.manage().window().maximize();
    }
    @Test
    public void testCase01(){
        System.out.println("Start of testCase01");
        try {
            driver.get("https://www.scrapethissite.com/pages/");
            WebDriverWait wait= new WebDriverWait(driver, Duration.ofSeconds(20));
            wait.until(ExpectedConditions.urlContains("pages"));
            Thread.sleep(3000);

            WebElement link1= driver.findElement(By.linkText("Hockey Teams: Forms, Searching and Pagination"));
            Wrappers.clickOnElement(link1, driver);
            System.out.println("Link clicked");

            wait.until(ExpectedConditions.urlContains("pages/forms"));

            ArrayList<Map<String, Object>> hockeyTeams = new ArrayList<>();

             // Iterate through 4 pages
            for (int i = 0; i < 4; i++) {
                List<WebElement> rows = driver
                        .findElements(By.xpath("//*[@class='pct text-success' or @class='pct text-danger']"));
                for (WebElement row : rows) {
                    WebElement nameElement = row.findElement(By.xpath("./preceding-sibling::td[@class='name']"));
                    String teamName = nameElement.getText();
                    WebElement yearElement = row.findElement(By.xpath("./preceding-sibling::td[@class='year']"));
                    String year = yearElement.getText();
                    double winPercentage = Double.parseDouble(row.getText().replace("%", ""));

                    if (winPercentage < 0.40) {
                        Map<String, Object> teamData = new HashMap<>();
                        teamData.put("Epoch Time of Scrape", Instant.now().getEpochSecond());
                        teamData.put("Team Name", teamName);
                        teamData.put("Year", year);
                        teamData.put("Win %", winPercentage);
                        hockeyTeams.add(teamData);
                    }
                }

                // Click next page if not the last iteration
                if (i < 3) {
                    WebElement nextPageButton = driver.findElement(By.xpath("//*[@aria-label='Next']"));
                    if (nextPageButton.isDisplayed()) {
                        Wrappers.clickOnElement(nextPageButton, driver);
                    }
                }
            }

            // Convert ArrayList to JSON
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.writeValue(new File("hockey-team-data.json"), hockeyTeams);
            
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        System.out.println("testcase01:Pass");
        System.out.println("End of testCase01");
    }

   @Test
    public void testCase02(){
        System.out.println("Start of testCase02");
        try {
            driver.get("https://www.scrapethissite.com/pages/");
            WebDriverWait wait= new WebDriverWait(driver, Duration.ofSeconds(20));
            wait.until(ExpectedConditions.urlContains("pages"));
            Thread.sleep(3000);

            WebElement link2=driver.findElement(By.linkText("Oscar Winning Films: AJAX and Javascript"));
            Wrappers.clickOnElement(link2, driver);
            System.out.println("Link clicked");

            wait.until(ExpectedConditions.urlContains("pages/ajax-javascript"));
            ArrayList<Map<String, Object>> oscarWinners = new ArrayList<>();
          String outputFilePath = "output/oscar-winner-data.json";

             // Get all years
             List<WebElement> years = driver.findElements(By.xpath("//*[@class='year-link']"));
             for (WebElement yearLink : years) {
                 String year = yearLink.getText();
                 Wrappers.clickOnElement(yearLink, driver);
 
                 // Get top 5 movies for the year
                 List<WebElement> rows = driver.findElements(By.xpath("//*[@class='film-title']"));
                 int count = 0;
                 boolean isWinner = false;
                 for (WebElement row : rows) {
                     if (count >= 5)
                         break; 
                     WebElement nominationElement = row
                             .findElement(By.xpath("./following-sibling::td[@class='film-nominations']"));
                     WebElement awardsElement = row
                             .findElement(By.xpath("./following-sibling::td[@class='film-awards']"));
                     try {
                         WebElement bestMovie = row
                                 .findElement(By.xpath("./following-sibling::td[@class='film-best-picture']/i"));
                         if (!bestMovie.isDisplayed()) {
                             isWinner = true;
                         }
                     } catch (Exception e) {
                         isWinner = false;
                     }
 
                     String title = row.getText();
                     String nomination = nominationElement.getText();
                     String awards = awardsElement.getText();
 
                     Map<String, Object> movieData = new HashMap<>();
                     movieData.put("Epoch Time of Scrape", Instant.now().getEpochSecond());
                     movieData.put("Year", year);
                     movieData.put("Title", title);
                     movieData.put("Nomination", nomination);
                     movieData.put("Awards", awards);
                     movieData.put("isWinner", isWinner);
 
                     oscarWinners.add(movieData);
                     count++;
                 }
 
                 // Go back to the list of years
                 driver.navigate().back();
             }
 
             // Convert ArrayList to JSON
             ObjectMapper mapper = new ObjectMapper();
             mapper.enable(SerializationFeature.INDENT_OUTPUT);
             File outputFile = new File(outputFilePath);
 
             // Ensure the output directory exists
             if (!outputFile.getParentFile().exists()) {
                 outputFile.getParentFile().mkdirs();
             }
 
             mapper.writeValue(outputFile, oscarWinners);
 
             // Assert that the file is present and not empty
             Assert.assertTrue(outputFile.exists(), "The JSON file does not exist.");
             Assert.assertTrue(outputFile.length() > 0, "The JSON file is empty.");
 
             System.out.println("JSON file created successfully at: " + outputFile.getAbsolutePath());
            
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        System.out.println("testcase02:Pass");
        System.out.println("End of testCase02");

    }

    @AfterTest
    public void endTest()
    {
        driver.close();
        driver.quit();

    }
}