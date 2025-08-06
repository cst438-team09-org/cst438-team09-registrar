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

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class AddCourseSystemTest {
    private static final Properties localConfig = new Properties();

    static {
        try (InputStream input = AddCourseSystemTest.class.getClassLoader()
                .getResourceAsStream("test.properties")) {
            if (input == null) {
                throw new RuntimeException("test.properties not found. Copy test.properties.example to test.properties and configure for your system.");

            }
            localConfig.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Could not load test.properties", e);
        }
    }

    static final String URL = localConfig.getProperty("test.url");

    static final int DELAY = 2000;
    WebDriver driver;

    Wait<WebDriver> wait;

    Random random = new Random();

    @BeforeEach
    public void setUpDriver() throws Exception {

        String driverPath = localConfig.getProperty("chrome.driver.path");
        String binaryPath = localConfig.getProperty("chrome.binary.path");

        System.setProperty("webdriver.chrome.driver", driverPath);
        ChromeOptions ops = new ChromeOptions();
        ops.addArguments("--remote-allow-origins=*");
        ops.setBinary(binaryPath);

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


    //
    @Test

    public void testStudentAddCourse() throws InterruptedException {
        // Login as user sama
        driver.findElement(By.id("email")).sendKeys("sama@csumb.edu");
        driver.findElement(By.id("password")).sendKeys("sam2025");
        driver.findElement(By.id("loginButton")).click();
        Thread.sleep(DELAY);

        //sama views his schedule for Fall 2025
        driver.findElement(By.id("scheduleLink")).click();
        Thread.sleep(DELAY);
        driver.findElement(By.id("year")).sendKeys("2025");
        driver.findElement(By.id("semester")).sendKeys("Fall");
        driver.findElement(By.id("selectTermButton")).click();
        Thread.sleep(DELAY);

        //find out what column number contains the courseId
        WebElement courseIdHeader = driver.findElement(By.xpath("//thead/tr/th[text()='Course ID']"));
        assertNotNull(courseIdHeader);
        //The position of the courseId column is determined by how many columns come before it.
        List<WebElement> prevSiblings = courseIdHeader.findElements(By.xpath("./preceding-sibling::*"));
        int courseIdCol = prevSiblings.size() + 1;
        //Find the table cell in the course Id column for course CST599
        WebElement courseId = driver.findElement(By.xpath("//tbody/tr/td[position()="+courseIdCol+" and text()='cst599']"));
        assertNotNull(courseId);
        //The parent of that table cell is the entire row...
        WebElement courseRow = courseId.findElement(By.xpath(".."));
        assertNotNull(courseRow);
        //... which contains the button to drop that course.
        WebElement dropButton = courseRow.findElement(By.xpath(".//button[text()='Drop']"));
        assertNotNull(dropButton);
        //sama drops the course
        dropButton.click();
        driver.findElement(By.xpath("//button[@label='Yes']")).click();
        Thread.sleep(DELAY);
        //After dropping the class, make sure that CST599 no longer shows up in sama's schedule
        assertThrows(NoSuchElementException.class, ()->{
           driver.findElement(By.xpath("//tbody/tr/td[position()="+courseIdCol+" and text()='cst599']"));
        });
        //sama returns to course enrollment page.
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

        //sama navigates to his transcript
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

        //Within the transcript row for CST599, find the cell under the Grade column
        WebElement grade = courseRow.findElement(By.xpath("./td["+gradeCol+"]"));
        assertNotNull(grade);
        //Make sure grade is currently empty
        assertEquals("—", grade.getText());
    }

}
