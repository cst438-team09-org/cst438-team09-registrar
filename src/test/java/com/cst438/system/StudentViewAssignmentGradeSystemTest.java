package com.cst438.system;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Properties;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class StudentViewAssignmentGradeSystemTest {
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

    WebDriver driver;
    Random random = new Random();
    WebDriverWait wait;
    static final int DELAY = 2000;

    @BeforeEach
    public void setUpDriver() {
        String driverPath = localConfig.getProperty("chrome.driver.path");
        String binaryPath = localConfig.getProperty("chrome.binary.path");

        System.setProperty("webdriver.chrome.driver", driverPath);
        ChromeOptions ops = new ChromeOptions();
        ops.addArguments("--remote-allow-origins=*");
        ops.setBinary(binaryPath);

        // Start the driver
        driver = new ChromeDriver(ops);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(URL);
    }

    @AfterEach
    public void quit() {
        driver.quit();
    }

    @Test
    public void testInstructorAddAssignmentAndStudentView() throws InterruptedException {
        // Instructor login
        driver.findElement(By.id("email")).sendKeys("ted@csumb.edu");
        driver.findElement(By.id("password")).sendKeys("ted2025"); // Replace with actual password
        driver.findElement(By.id("loginButton")).click();
        Thread.sleep(DELAY);

        // Enter year and semester to view sections
        driver.findElement(By.id("year")).sendKeys("2025");
        driver.findElement(By.id("semester")).sendKeys("Fall");
        driver.findElement(By.id("selectTermButton")).click();
        Thread.sleep(DELAY);

        // Select view assignments for section CST599
        String course = "cst599";
        String xpath = String.format("//tr[td[text()='%s']]/td/a[text()='Assignments']", course);
        driver.findElement(By.xpath(xpath)).click();
        Thread.sleep(DELAY);

        // Generate a random assignment title
        String assignmentTitle = "assignment" + random.nextInt(100000);
        driver.findElement(By.id("assignmentTitle")).sendKeys(assignmentTitle);
        driver.findElement(By.id("dueDate")).sendKeys("2025-12-01");
        driver.findElement(By.id("saveAssignmentButton")).click();

        // Handle the confirmation alert
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[@class='react-confirm-alert-button-group']/button[@label='Yes']")));
        WebElement yesButton = driver.findElement(
                By.xpath("//div[@class='react-confirm-alert-button-group']/button[@label='Yes']"));
        yesButton.click();

        // Verify that the new assignment shows on the assignment page
        assertTrue(driver.getPageSource().contains(assignmentTitle), "Assignment title not found on the page.");

        // Logout as instructor
        driver.findElement(By.id("logoutButton")).click();

        // Student login
        driver.findElement(By.id("email")).sendKeys("samb@csumb.edu");
        driver.findElement(By.id("password")).sendKeys("sam2025");
        driver.findElement(By.id("loginButton")).click();

        // Navigate to view assignments
        driver.findElement(By.id("assignmentsLink")).click();

        // Verify that the new assignment in course CST599 appears and the assignment score is blank
        assertTrue(driver.getPageSource().contains(assignmentTitle), "Assignment title not found in student's assignments.");
        assertTrue(driver.getPageSource().contains("—"), "Assignment score is not blank.");
    }
}
