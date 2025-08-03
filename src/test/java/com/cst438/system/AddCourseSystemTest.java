package com.cst438.system;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class AddCourseSystemTest {
    static final String CHROME_DRIVER_FILE_LOCATION = "/Users/Tekprincezz415/Downloads/chromedriver-mac-arm64/chromedriver";
    static final String URL = "http://localhost:5173";   // react dev server

    static final int DELAY = 2000;
    WebDriver driver;

    Wait<WebDriver> wait;

    Random random = new Random();

    @BeforeEach
    public void setUpDriver() throws Exception {

        // set properties required by Chrome Driver
        System.setProperty(
                "webdriver.chrome.driver", CHROME_DRIVER_FILE_LOCATION);
        ChromeOptions ops = new ChromeOptions();
        ops.addArguments("--remote-allow-origins=*");

        // start the driver
        driver = new ChromeDriver(ops);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
        wait = new WebDriverWait(driver, Duration.ofSeconds(2));
        driver.get(URL);
    }

    @AfterEach
    public void quit() {
        driver.quit();
    }

    @Test
    public void testStudentAddCourse() throws InterruptedException {
        // navigate to "home" on the nav bar and login
        driver.findElement(By.id("email")).sendKeys("sama@csumb.edu");
        driver.findElement(By.id("password")).sendKeys("sam2025");
        driver.findElement(By.id("loginButton")).click();

        // dismiss the alert message

        Thread.sleep(DELAY);
        driver.findElement(By.id("scheduleLink")).click();
        Thread.sleep(DELAY);
        driver.findElement(By.id("year")).sendKeys("2025");
        driver.findElement(By.id("semester")).sendKeys("Fall");
        driver.findElement(By.id("selectTermButton")).click();
        Thread.sleep(DELAY);

        //find out what column number contains the courseId
        WebElement courseIdHeader = driver.findElement(By.xpath("//thead/tr/th[text()='Course ID']"));
        assertNotNull(courseIdHeader);
        List<WebElement> prevSiblings = courseIdHeader.findElements(By.xpath("./preceding-sibling::*"));
        int courseIdCol = prevSiblings.size() + 1;
        WebElement courseId = driver.findElement(By.xpath("//tbody/tr/td[position()="+courseIdCol+" and text()='cst599']"));
        assertNotNull(courseId);

        WebElement courseRow = courseId.findElement(By.xpath(".."));
        assertNotNull(courseRow);

        WebElement dropButton = courseRow.findElement(By.xpath(".//button[text()='Drop']"));
        assertNotNull(dropButton);

        dropButton.click();


        driver.findElement(By.xpath("//button[@label='Yes']")).click();
        Thread.sleep(DELAY);
        assertThrows(NoSuchElementException.class, ()->{
           driver.findElement(By.xpath("//tbody/tr/td[position()="+courseIdCol+" and text()='cst599']"));
        });

        driver.findElement(By.id("addCourseLink")).click();
        Thread.sleep(DELAY);

        //find out what column number contains the courseId
        courseIdHeader = driver.findElement(By.xpath("//thead/tr/th[text()='course Id']"));
        assertNotNull(courseIdHeader);
        prevSiblings = courseIdHeader.findElements(By.xpath("./preceding-sibling::*"));
        int courseIdColumn = prevSiblings.size() + 1;
        courseId = driver.findElement(By.xpath("//tbody/tr/td[position()="+courseIdColumn+" and text()='cst599']"));
        assertNotNull(courseId);

        courseRow = courseId.findElement(By.xpath(".."));
        assertNotNull(courseRow);

        WebElement addButton = courseRow.findElement(By.xpath(".//button[text()='Add']"));
        assertNotNull(addButton);

        addButton.click();

        driver.findElement(By.xpath("//button[@label='Yes']")).click();
        driver.findElement(By.id("transcriptLink")).click();
        Thread.sleep(DELAY);

        //find out what column number contains the grade
        WebElement gradeHeader = driver.findElement(By.xpath("//thead/tr/th[text()='Grade']"));
        assertNotNull(gradeHeader);
        prevSiblings = gradeHeader.findElements(By.xpath("./preceding-sibling::*"));
        int gradeCol = prevSiblings.size() + 1;

        //find out what column number contains the courseId
        courseIdHeader = driver.findElement(By.xpath("//thead/tr/th[text()='CourseId']"));
        assertNotNull(courseIdHeader);
        prevSiblings = courseIdHeader.findElements(By.xpath("./preceding-sibling::*"));
        courseIdColumn = prevSiblings.size() + 1;
        courseId = driver.findElement(By.xpath("//tbody/tr/td[position()="+courseIdColumn+" and text()='cst599']"));
        assertNotNull(courseId);

        courseRow = courseId.findElement(By.xpath(".."));
        assertNotNull(courseRow);

        WebElement grade = courseRow.findElement(By.xpath("./td["+gradeCol+"]"));
        assertNotNull(addButton);

        assertEquals("—", grade.getText());



    }

}