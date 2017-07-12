package io.openshift.launchpad.github;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collections;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.SafeMode;
import org.slf4j.Logger;

import static org.asciidoctor.OptionsBuilder.options;

/**
 * Service that can merge a adoc file into a template.
 */
@ApplicationScoped
public class TemplateMergerService {

    @Inject
    private Logger logger;

    public String convertToAsciidoc(File adocFile) {
        Asciidoctor asciidoctor = Asciidoctor.Factory.create();
        return asciidoctor.convertFile(adocFile, options().toFile(false).safe(SafeMode.UNSAFE).asMap());
    }

    public File mergeTemplate(File workingFolder, String html) throws IOException {
        File tempFile = File.createTempFile("output", "html");
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_23);
        configuration.setDirectoryForTemplateLoading(new File(workingFolder, ".openshiftio"));
        try {
            OutputStreamWriter out = new FileWriter(tempFile);
            configuration.getTemplate("index.html.flt").process(Collections.singletonMap("html", html), out);
        } catch (TemplateException | TemplateNotFoundException e) {
            logger.warn("could not apply template '{}'", workingFolder);
            return null;
        }

        return tempFile;
    }
}
