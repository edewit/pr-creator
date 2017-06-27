package io.openshift.launchpad.github;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;

import com.google.common.io.Files;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test for the TemplateMergerService.
 */
public class TemplateMergerServiceTest {
    private TemplateMergerService templateMergerService = new TemplateMergerService();

    @Test
    public void shouldConvertToAsciidoc() throws IOException {
        //given
        File tempFile = File.createTempFile("test", ".adoc");
        try (PrintWriter printWriter = new PrintWriter(tempFile)) {
            printWriter.write("Vert.x HTTP Booster\n-------------------");
        }

        //when
        String html = templateMergerService.convertToAsciidoc(tempFile);

        //then
        assertEquals("<div class=\"sect1\">\n" +
                             "<h2 id=\"_vert_x_http_booster\">Vert.x HTTP Booster</h2>\n" +
                             "<div class=\"sectionbody\">\n\n" +
                             "</div>\n" +
                             "</div>", html);
    }

    @Test
    public void shouldMergeTemplate() throws IOException {
        //given
        String html = "<h1>Some nice html asciidoc</h1>";
        File workingFolder = Files.createTempDir();
        File templateFolder = new File(workingFolder, ".openshiftio");
        templateFolder.mkdir();
        try (PrintWriter printWriter = new PrintWriter(new File(templateFolder, "index.html.flt"))) {
            printWriter.write("merge ${html}");
        }

        //when
        File result = templateMergerService.mergeTemplate(workingFolder, html);

        //then
        assertTrue(result.exists());
        try(FileInputStream inputStream = new FileInputStream(result)) {
            String content = IOUtils.toString(inputStream);
            assertEquals("merge " + html, content);
        }
    }
}