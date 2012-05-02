package net.masterthought.gherka;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BddHtmlBuilder {

    private List<File> folders;
    private List<String> suffixes;
    private File htmlViewDirectory;

    public static Map<String, String> contentMap = new HashMap<String, String>() {{
        put("Narrative:", "<span class=\"feature-keyword indent0 narrative\">Narrative:</span>");
        put("Feature:", "<span class=\"feature-keyword indent0 narrative\">Feature:</span>");
        put("Background:", "<span class=\"feature-keyword indent1 background\">Background:</span>");
        put("Scenario:", "<span class=\"feature-keyword indent1 scenario\">Scenario:</span>");
        put("Scenario Outline:", "<span class=\"feature-keyword indent1 scenario-outline\">Scenario Outline:</span>");
        put("Examples:", "<span class=\"feature-keyword indent2 examples\">Examples:</span>");
        put("Given", "<span class=\"step-keyword indent2\">Given</span>");
        put("When", "<span class=\"step-keyword indent2\">When</span>");
        put("Then", "<span class=\"step-keyword indent2\">Then</span>");
        put("And", "<span class=\"step-keyword indent2\">And</span>");
        put("As a", "<span class=\"narrative-keyword indent1\">As a</span>");
        put("I want to", "<span class=\"narrative-keyword indent1\">I want to</span>");
        put("In order to", "<span class=\"narrative-keyword indent1\">In order to</span>");
        put("So that", "<span class=\"narrative-keyword indent1\">So that</span>");
    }};

    public BddHtmlBuilder(List<String> folders, List<String> suffixes, String outDir) {
        this.htmlViewDirectory = new File(outDir, "html-view");
        this.suffixes = suffixes;
        this.folders = findBddfiles(folders);
    }

    public void generateHtml() throws Exception {
        if (!htmlViewDirectory.exists()) {
            htmlViewDirectory.mkdirs();
        }

        for (File bddFile : folders) {
            VelocityEngine ve = new VelocityEngine();
            ve.init(getProperties());
            Template featureTemplate = ve.getTemplate("templates/featureTemplate.vm");
            VelocityContext context = new VelocityContext();

            try {
                String fileContent = applyParameters(readFileAsString(bddFile));
                fileContent = applyKeywords(contentMap.keySet(), fileContent);
                fileContent = applyHtml(fileContent);
                fileContent = applytables(fileContent);
                fileContent = applySpacing(fileContent);
                fileContent = applyQuotes(fileContent);
                fileContent = applyTags(fileContent);
                context.put("fileName", bddFile.getName());
                context.put("content", fileContent);

                generateReport(bddFile.getName() + ".html", featureTemplate, context);
                System.out.println("Processing: " + bddFile);
            } catch (Exception ex) {
                System.out.println("[ERROR] Could not process file: " + bddFile + " \nexception:\n" + ex.getMessage());
            }
        }

        generateOverview();
        generateCss();
        copyOriginals();
    }

    private String applyTags(String s) {
        List<String> list = new ArrayList<String>();
        for (String line : Splitter.onPattern("\r?\n").split(s)) {
            if (line.contains("@")) {
                list.add(line.replaceAll("<div>", "<div class=\"step-tags indent1\">"));
            } else {
                list.add(line);
            }
        }
        return Joiner.on("\n").join(list);
    }

    private void copyOriginals() throws IOException {
        if (folders.size() != 0) {
            System.out.println("Copying original files for reference");
            for (File file : folders) {
                FileUtils.copyFile(file, new File(htmlViewDirectory.getAbsolutePath(), file.getName()));
            }
        }
    }

    private void generateOverview() throws Exception {
        VelocityEngine ve = new VelocityEngine();
        ve.init(getProperties());
        Template overviewTemplate = ve.getTemplate("templates/overviewTemplate.vm");
        VelocityContext context = new VelocityContext();
        context.put("fileList", folders);
        generateReport("index.html", overviewTemplate, context);
        System.out.println("Generating index.html overview page");
    }

    private String applyQuotes(String s) {
        List<String> list = new ArrayList<String>();
        for (String line : Splitter.onPattern("\r?\n").split(s)) {
            if (line.contains("'")) {
                list.add(line.replaceAll("'(.+?)'", "<span class=\"step-quote\">&quot;$1&quot;</span>"));
            } else {
                list.add(line);
            }
        }
        return Joiner.on("\n").join(list);
    }

    private String applySpacing(String fileContent) {
        Pattern p = Pattern.compile("<div>\\s*</div>");
        Matcher m = p.matcher(fileContent);
        while (m.find()) {
            fileContent = m.replaceAll("<br/>");
        }
        return fileContent;
    }

    private String applytables(String fileContent) {
        Pattern p = Pattern.compile("<div>\\s*\\|");
        Matcher m = p.matcher(fileContent);
        while (m.find()) {
            fileContent = m.replaceAll("<tr><td>");
        }

        p = Pattern.compile("\\|\\s*</div>");
        m = p.matcher(fileContent);
        while (m.find()) {
            fileContent = m.replaceAll("</td></tr>");
        }

        p = Pattern.compile("\\|");
        m = p.matcher(fileContent);
        while (m.find()) {
            fileContent = m.replaceAll("</td><td>");
        }

        int rowCount = 0;
        List<String> list = new ArrayList<String>();
        for (String line : Splitter.onPattern("\r?\n").split(fileContent)) {

            if (line.contains("<tr>")) {
                if (rowCount == 0) {
                    list.add(line.replace("<tr>", "<table class=\"example-table indent3\"><tr>").replaceAll("<td>", "<th>").replaceAll("</td>", "</th>"));
                } else {
                    list.add(line);
                }
                rowCount++;
            } else if (line.contains("</tr>")) {
                list.add(line);
            } else {
                if (rowCount > 0) {
                    list.add("</table>");
                } else {
                    list.add(line);
                }
                rowCount = 0;
            }
        }
        fileContent = Joiner.on("\n").join(list);
        return fileContent;
    }

    private String applyParameters(String s) {
        List<String> list = new ArrayList<String>();
        for (String line : Splitter.onPattern("\r?\n").split(s)) {
            if (line.contains("<")) {
                list.add(line.replaceAll("<(.+?)>", "<span class=\"step-parameter\">&lt;$1&gt;</span>"));
            } else {
                list.add(line);
            }
        }
        return Joiner.on("\n").join(list);
    }

    private String applyHtml(String fileContent) {
        List<String> list = new ArrayList<String>();
        for (String s : Splitter.onPattern("\r?\n").split(fileContent)) {
            list.add("<div>" + s + "</div>");
        }
        return Joiner.on("\n").join(list);
    }

    private void generateReport(String fileName, Template featureResult, VelocityContext context) throws Exception {
        Writer writer = new FileWriter(new File(htmlViewDirectory, fileName));
        featureResult.merge(context, writer);
        writer.flush();
        writer.close();
    }

    private void writeFile(String fileName, String content) throws IOException {
        Writer writer = new FileWriter(new File(htmlViewDirectory, fileName));
        writer.append(content);
        writer.flush();
        writer.close();
    }

    private String applyKeywords(Set<String> words, String content) {
        for (String word : words) {
            content = replaceWord(word, content);
        }
        return content;
    }

    private String replaceWord(String word, String content) {
        String res = content;
        String regex = word;
        return Pattern.compile(regex).matcher(res).replaceAll(contentMap.get(word));
    }

    private String readFileAsString(File filePath) throws java.io.IOException {
        byte[] buffer = new byte[(int) filePath.length()];
        BufferedInputStream f = null;
        try {
            f = new BufferedInputStream(new FileInputStream(filePath));
            f.read(buffer);
        } finally {
            if (f != null) try {
                f.close();
            } catch (IOException ignored) {
            }
        }
        return new String(buffer);
    }

    private Properties getProperties() {
        Properties props = new Properties();
        props.setProperty("resource.loader", "class");
        props.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        return props;
    }

    private List<File> findBddfiles(List<String> folders) {
        List<File> fileList = new ArrayList<File>();
        for (String folder : folders) {
            for (String file : findBddFile(new File(folder))) {
                fileList.add(new File(folder, file));
            }
        }
        return fileList;
    }

    private String[] findBddFile(File targetDirectory) {
        String[] suffixList = new String[suffixes.size()];
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setIncludes(suffixes.toArray(suffixList));
        scanner.setBasedir(targetDirectory);
        scanner.scan();
        return scanner.getIncludedFiles();
    }

    private void generateCss() throws IOException {
        List<String> css = new ArrayList<String>();
        css.add("body{\n" +
                "    background-color:#272822;\n" +
                "    color: #dbdb74;\n" +
                "    font-family: arial;\n" +
                "}\n" +
                "\n" +
                ".feature-keyword{color:#f92672;}\n" +
                ".step-keyword{color:#66d9ef;}\n" +
                ".narrative-keyword{color:#66d9ef;}\n" +
                ".step-parameter{color:#ae81ff;}\n" +
                ".step-quote{color:white;}\n" +
                ".step-tags{color:white;}\n" +
                "\n" +
                ".indent1{padding-left:30px;}\n" +
                ".indent2{padding-left:50px;}\n" +
                ".indent3{padding-left:70px;}\n" +
                "\n" +
                "table.example-table {\n" +
                "margin-left:50px;\n" +
                "color: #86e22e;\n" +
                "border-width: 1px;\n" +
                "border-spacing: 2px;\n" +
                "border-style: outset;\n" +
                "border-color: #dbdb74;\n" +
                "border-collapse: collapse;\n" +
                "background-color: #272822;\n" +
                "}\n" +
                "table.example-table th {\n" +
                "color: #ffb24c;\n" +
                "border-width: 1px;\n" +
                "padding: 5px;\n" +
                "border-style: inset;\n" +
                "border-color: #dbdb74;\n" +
                "background-color: #272822;\n" +
                "}\n" +
                "table.example-table td {\n" +
                "color:#86e22e;\n" +
                "text-align: center;\n" +
                "border-width: 1px;\n" +
                "padding: 5px;\n" +
                "border-style: inset;\n" +
                "border-color: #dbdb74;\n" +
                "background-color: #272822;\n" +
                "}\n" +
                "\n" +
                "a{color:#66d9ef;}\n" +
                ".subscript{\n" +
                "    font-size:.9em;\n" +
                "    font-style: italic;\n" +
                "    color:#f92672;\n" +
                "}");

        List<String> lightCss = new ArrayList<String>();
        lightCss.add("body{\n" +
                "    background-color: #f6fdff;\n" +
                "    color: #6c6c6c;\n" +
                "    font-family: arial;\n" +
                "}\n" +
                "\n" +
                ".feature-keyword{color: #f91958;font-weight: bold;}\n" +
                ".step-keyword{color:#3388ef;font-weight: bold;}\n" +
                ".narrative-keyword{color: #3388ef;font-weight: bold;}\n" +
                ".step-parameter{color: #aa58ff;}\n" +
                ".step-quote{color: #020202;}\n" +
                ".step-tags{color: #020202;}\n" +
                "\n" +
                ".indent1{padding-left:30px;}\n" +
                ".indent2{padding-left:50px;}\n" +
                ".indent3{padding-left:70px;}\n" +
                "\n" +
                "table.example-table {\n" +
                "margin-left:50px;\n" +
                "color: #86e22e;\n" +
                "border-width: 1px;\n" +
                "border-spacing: 2px;\n" +
                "border-style: outset;\n" +
                "border-color: #6c6c6c;\n" +
                "border-collapse: collapse;\n" +
                "background-color: #272822;\n" +
                "}\n" +
                "table.example-table th {\n" +
                "color: #3388ef;\n" +
                "border-width: 1px;\n" +
                "padding: 5px;\n" +
                "border-style: inset;\n" +
                "border-color: #6c6c6c;\n" +
                "background-color: #f6fdff;\n" +
                "}\n" +
                "table.example-table td {\n" +
                "color:#6c6c6c;\n" +
                "text-align: center;\n" +
                "border-width: 1px;\n" +
                "padding: 5px;\n" +
                "border-style: inset;\n" +
                "border-color: #6c6c6c;\n" +
                "background-color: #f6fdff;\n" +
                "}\n" +
                "\n" +
                "a{color:#6c6c6c;}\n" +
                ".subscript{\n" +
                "    font-size:.9em;\n" +
                "    font-style: italic;\n" +
                "    color:#f92672;\n" +
                "}");

        File cssFile = new File(htmlViewDirectory, "style.css");
        if (!cssFile.exists()) {
            System.out.println("Generating CSS");
            writeFile("style.css", Joiner.on("\n").join(css));
        } else {
            System.out.println("CSS already exists - not generating");
        }
        writeFile("light-style.css", Joiner.on("\n").join(lightCss));
    }


}
