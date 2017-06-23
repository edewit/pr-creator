package io.openshift.launchpad.github;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.HashMap;

import javax.inject.Singleton;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import org.asciidoctor.Asciidoctor;

/**
 * Service that can merge a adoc file into a template.
 */
@Singleton
public class TemplateMergerService {

    public String convertToAsciidoc(File adocFile) {
        Asciidoctor asciidoctor = Asciidoctor.Factory.create();
        return asciidoctor.convertFile(adocFile, new HashMap<>());
    }

    public File mergeTemplate(File workingFolder, String html) throws IOException {
        File tempFile = File.createTempFile("output", "html");
        Configuration configuration = new Configuration();
        configuration.setDirectoryForTemplateLoading(new File(workingFolder, ".openshiftio"));
        try {
            OutputStreamWriter out = new FileWriter(tempFile);
            configuration.getTemplate("index.html.flt").process(Collections.singletonMap("html", html), out);
        } catch (TemplateException e) {
            throw new RuntimeException("could not transform template");
        }

        return tempFile;
    }
}
