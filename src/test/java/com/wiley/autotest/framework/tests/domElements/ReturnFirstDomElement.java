package com.wiley.autotest.framework.tests.domElements;

import com.wiley.autotest.framework.config.BaseTest;
import com.wiley.autotest.framework.pages.TestDomElementPage;
import org.testng.annotations.Test;

public class ReturnFirstDomElement extends BaseTest {

    @Test
    public void test() {
        openPage("mainTestElement.html", TestDomElementPage.class)
                .checkFirstDomElement();
    }
}
