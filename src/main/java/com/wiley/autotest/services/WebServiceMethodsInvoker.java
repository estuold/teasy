package com.wiley.autotest.services;

import com.wiley.autotest.selenium.AbstractTest;
import com.wiley.autotest.selenium.AbstractWebServiceTest;
import com.wiley.autotest.selenium.Report;
import org.springframework.stereotype.Service;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestRunner;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * User: dfedorov
 * Date: 7/26/12
 * Time: 9:32 AM
 */
@Service
public class WebServiceMethodsInvoker extends MethodsInvoker {

    public <T extends Annotation> void invokeSuiteMethodsByAnnotation(final Class<T> annotationClass, final ITestContext testContext) {
        invokeGroupMethodsByAnnotation(annotationClass, testContext);
    }

    public <T extends Annotation> void invokeGroupMethodsByAnnotation(final Class<T> annotationClass, final ITestContext testContext) {
        final TestClassContext testClassContext = new TestClassContext(((TestRunner) testContext).getTest()
                .getXmlClasses()
                .get(0)
                .getSupportClass(), null, annotationClass, testContext);
        invokeMethodsByAnnotation(testClassContext, true);
    }

    public <T extends Annotation> void invokeMethodsByAnnotation(final AbstractWebServiceTest testObject, final Class<T> annotationClass) {
        invokeMethodsByAnnotation(new TestClassContext(testObject.getClass(), testObject, annotationClass), false);
    }

    @Override
    void invokeMethod(AbstractTest instance, Method method, TestClassContext context, boolean isBeforeAfterGroup) {
        AbstractWebServiceTest abstractWebServiceTest = (AbstractWebServiceTest) instance;
        try {
            method.setAccessible(true);
            method.invoke(instance);
        } catch (Throwable e) {
            Throwable exceptionForLog;
            if (e instanceof InvocationTargetException) {
                exceptionForLog = ((InvocationTargetException) e).getTargetException();
            } else {
                exceptionForLog = e;
            }

            String errorMessage = "Precondition method failed. " + exceptionForLog.getMessage() + ". Stacktrace: " + getStackTrace(exceptionForLog);
            if (isBeforeAfterGroup) {
                abstractWebServiceTest.setPostponedBeforeAfterGroupFail(errorMessage, context.getTestContext());
            } else {
                abstractWebServiceTest.setPostponedTestFail(errorMessage);
            }

            new Report(errorMessage, exceptionForLog).allure();
            throw new StopTestExecutionException(errorMessage, exceptionForLog);
        }
    }

    private static String getStackTrace(Throwable throwable) {
        if (throwable == null) {
            return null;
        }

        Writer stacktrace = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stacktrace);
        throwable.printStackTrace(printWriter);
        return stacktrace.toString();
    }
}
