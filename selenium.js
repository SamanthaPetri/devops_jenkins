async function runTest() {
  // Create a new WebDriver instance
  const driver = await new Builder().forBrowser('chrome').build();

  try {
    // Navigate to your HTML page
    await driver.get("http://localhost:8000/");

    // Find an element by its ID
    const button = await driver.findElement(By.id("menu-toggle"));

    // Click the button
    await button.click();
  } finally {
    // Quit the WebDriver
    await driver.quit();
  }
}

runTest();
