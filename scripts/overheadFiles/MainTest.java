package de.peass;

import org.junit.Test;
import de.dagere.kopeme.annotations.Assertion;
import de.dagere.kopeme.annotations.MaximalRelativeStandardDeviation;
import org.junit.rules.TestRule;
import org.junit.Rule;
import de.dagere.kopeme.junit.rule.KoPeMeRule;

public class MainTest {

    @Test
    @de.dagere.kopeme.annotations.PerformanceTest(executionTimes = 10, warmupExecutions = 10, logFullData = true, useKieker = false, timeout = 600000, repetitions = 10, redirectToNull = true, dataCollectors = "ONLYTIME_NOGC")
    public void testMe() {
        C0_0 object = new C0_0();
        object.method0();
        object.method1();
    }

    @Rule()
    public TestRule kopemeRule = new KoPeMeRule(this);
}
