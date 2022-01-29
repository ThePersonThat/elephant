package edu.sumdu.tss.elephant.cucumber;


import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features",
        glue = {"edu.sumdu.tss.elephant.cucumber.steps"}
)
public class CucumberRunnerTest {}
