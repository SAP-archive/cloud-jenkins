package com.sap.hana.cloud.samples.jenkins.plugin.uitests;

/*
 * #%L
 * SAP HANA Cloud Platform Samples - Cloud Jenkins
 * %%
 * Copyright (C) 2013 SAP AG
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class CloudJenkinsConfigurationPage extends AbstractPage {

    @FindBy(name = "storeButton")
    private WebElement storeButton;

    @FindBy(name = "deleteButton")
    private WebElement deleteButton;

    @FindBy(name = "_.includes")
    private WebElement includesTextField;

    @FindBy(name = "_.excludes")
    private WebElement excludesTextField;

    @FindBy(name = "_.path")
    private WebElement deleteTextField;

    public CloudJenkinsConfigurationPage(final WebDriver driver) {
        super(driver);
    }

    public FilesToStorePage storeConfiguration() {
        storeButton.click();
        return PageFactory.initElements(getWebDriver(), FilesToStorePage.class);
    }

    public FilesToStorePage storeConfiguration(final String includes, final String excludes) {
        includesTextField.clear();
        includesTextField.sendKeys(includes);
        excludesTextField.clear();
        excludesTextField.sendKeys(excludes);
        return storeConfiguration();
    }

    public FilesToDeletePage deleteFiles(final String path) {
        deleteTextField.clear();
        deleteTextField.sendKeys(path);
        return deleteFile();
    }

    private FilesToDeletePage deleteFile() {
        deleteButton.click();
        return PageFactory.initElements(getWebDriver(), FilesToDeletePage.class);
    }
}
