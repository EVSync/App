package tqs.evsync.backend.userStorie1;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
    features = "classpath:features/discover_charging_stations.feature", // Path from resources/
    glue = "tqs.evsync.backend.userStorie1", 
    plugin = {"pretty", "html:target/cucumber-reports.html"},
    publish = true
    )
public class RunCucumberTest {}