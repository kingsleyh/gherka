package net.masterthought.gherka;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

import java.util.ArrayList;
import java.util.List;

public class Gherka {

    public static void main(String[] args) throws Exception {
        GherkaParameters params = new GherkaParameters();
        JCommander cmd = new JCommander(params);

        try {
            cmd.parse(args);
//            List<String> list = new ArrayList<String>();
//            list.add("/Users/kings/Desktop/autotestFraudAdapter/test-resources");
//            list.add("/Users/kings/Desktop/Automation_Pack");
//            list.add("/Users/kings/visa/cucumber-jvm-example/src/test/resources");
//            List<String> suffixes = new ArrayList<String>();
//            suffixes.add("**/*.txt");
//            suffixes.add("**/*.story");
//            suffixes.add("**/*.feature");
//            BddHtmlBuilder builder = new BddHtmlBuilder(list, suffixes, ".");
            BddHtmlBuilder builder = new BddHtmlBuilder(params.getFolders(), params.getSuffixes(), params.getOutDir());
            builder.generateHtml();
        } catch (ParameterException ex) {
            System.out.println(ex.getMessage());
            cmd.usage();
        }

        System.out.println("Done!");
    }
}
