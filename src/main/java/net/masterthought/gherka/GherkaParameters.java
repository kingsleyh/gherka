package net.masterthought.gherka;

import com.beust.jcommander.Parameter;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

public class GherkaParameters {

    @Parameter(names = "-folders", required = true, description = "Comma-separated list of folders for bdd scripts to be located")
    private String folders;

    @Parameter(names = "-suffixes", required = true, description = "Comma-separated list of bdd file suffixes e.g. story,feature,txt")
    private String suffixes;

    @Parameter(names = "-o", required = true, description = "Output directory for the html")
    private String outDir;

    public List<String> getFolders() {
        return Lists.newArrayList(Splitter.on(",").split(folders));
    }

    public List<String> getSuffixes() {
        List<String> suffixList = new ArrayList<String>();
        List<String> list = Lists.newArrayList(Splitter.on(",").split(suffixes));
        for (String suffix : list) {
            suffixList.add("**/*." + suffix);
        }
        return suffixList;
    }

    public String getOutDir(){
        return outDir;
    }

}
