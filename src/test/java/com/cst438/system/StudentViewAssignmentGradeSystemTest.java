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
        try (InputStream input = StudentViewAssignmentGradeSystemTest.class.getClassLoader()
                .getResourceAsStream("test.properties")) {
            if (input == null) {
                throw new RuntimeException("test.properties not found. Copy test.properties to test.properties and configure for your system.");
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
        wait = new WebDriverWait(driver, Duration.ofSeconds(1000));
        driver.get(URL);
    }

    @AfterEach
    public void quit() {
        driver.quit();
    }

    @Test
    public void testInstructorAddAssignmentAndStudentView() throws InterruptedException {
        // Instructor login
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email"))).sendKeys("ted@csumb.edu");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password"))).sendKeys("ted2025"); // Replace with actual password
        wait.until(ExpectedConditions.elementToBeClickable(By.id("loginButton"))).click();

        // Wait until login completes
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("year")));

        // Enter year and semester to view sections
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("year"))).sendKeys("2025");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("semester"))).sendKeys("Fall");
        wait.until(ExpectedConditions.elementToBeClickable(By.id("selectTermButton"))).click();

        // Wait until term selection is processed
        String course = "cst599";
        String xpath = String.format("//tr[td[text()='%s']]/td/a[text()='Assignments']", course);
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath))).click();

        // Wait for Add Assignment button and click it
        wait.until(ExpectedConditions.elementToBeClickable(By.id("addAssignmentButton"))).click();

        // Wait for dialog input fields to be visible
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("title")));

        // Generate a random assignment title
        String assignmentTitle = "assignment" + random.nextInt(100000);

        // Fill in the assignment title
        WebElement titleInput = driver.findElement(By.name("title"));
        titleInput.clear();
        titleInput.sendKeys(assignmentTitle);

        // Fill in the due date (YYYY-MM-DD format)
        WebElement dueDateInput = driver.findElement(By.name("dueDate"));
        dueDateInput.clear();
        dueDateInput.sendKeys("12/01/2025");

        // Click Save button inside the dialog
        driver.findElement(By.xpath("//dialog//button[text()='Save']")).click();

        // Wait until dialog disappears or page updates with new assignment title
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//dialog")));

        // Verify new assignment appears on page
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), assignmentTitle));
        assertTrue(driver.getPageSource().contains(assignmentTitle), "Assignment title not found on the page.");

        // Logout as instructor
        wait.until(ExpectedConditions.elementToBeClickable(By.id("logoutLink"))).click();

        // Student login
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email"))).sendKeys("samb@csumb.edu");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password"))).sendKeys("sam2025");
        wait.until(ExpectedConditions.elementToBeClickable(By.id("loginButton"))).click();

        // Navigate to view assignments
        wait.until(ExpectedConditions.elementToBeClickable(By.id("viewAssignmentsLink"))).click();

        // Enter year and semester to view sections
        driver.findElement(By.id("year")).sendKeys("2025");
        driver.findElement(By.id("semester")).sendKeys("Fall");
        wait.until(ExpectedConditions.elementToBeClickable(By.id("selectTermButton"))).click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), assignmentTitle));

        // Verify that the new assignment in course CST599 appears and the assignment score is blank
        assertTrue(driver.getPageSource().contains(assignmentTitle), "Assignment title not found in student's assignments.");
        assertTrue(driver.getPageSource().contains("N/A"), "Assignment score is not blank.");

    }

}
