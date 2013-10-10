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

import static java.util.Arrays.asList;

import java.util.HashSet;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class FilesToStorePage extends AbstractPage {

    @FindBy(id = "confirmStoreFileList")
    private WebElement files;

    @FindBy(name = "storeButton")
    private WebElement storeButton;

    public FilesToStorePage(final WebDriver driver) {
        super(driver);
    }

    public HashSet<String> getListedFiles() {
        return new HashSet<String>(asList(files.getText().trim().split("\n")));
    }

    public CloudJenkinsConfigurationPage clickStore() {
        storeButton.click();
        return PageFactory.initElements(getWebDriver(), CloudJenkinsConfigurationPage.class);
    }

}
