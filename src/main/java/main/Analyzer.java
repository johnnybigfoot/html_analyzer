package main;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

public class Analyzer {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(Analyzer.class);

    public static void main(String[] args) throws IOException {
        if (args.length != 2)
            throw new IllegalArgumentException("Please provide 2 arguments: <original file> <modified file>");
        String originalPath = args[0];
        String modifiedPath = args[1];
        LOG.info("1st argument is {}", originalPath);
        LOG.info("2nd argument is {}", modifiedPath);
        getOriginalTarget(readDoc(originalPath));
        Optional<Element> modifiedTarget = getModifiedTarget(readDoc(modifiedPath));
        if (modifiedTarget.isPresent()) {
            String path = buildPath(modifiedTarget.get());
            LOG.info("XPath of needed element in diff-case is: " + path);
        } else
            LOG.info("Failed to find target element");

    }

    private static Document readDoc(String filePath) throws IOException {
        File input = new File(filePath);
        return Jsoup.parse(input, "UTF-8", "http://google.com/");
    }

    private static void getOriginalTarget(Document document) {
        Element target = document.getElementById("make-everything-ok-button");
        LOG.info("Target in original is: ");
        LOG.info("{} ", target);
    }

    private static Optional<Element> getModifiedTarget(Document document) {
        Elements byHref = document.getElementsByAttributeValueContaining("href", "ok");
        Optional<Element> neededElement = byHref.stream().
                filter(i -> !i.attr("class").contains("warning")).
                filter(i -> !i.attr("class").contains("danger")).
                filter(i -> i.attr("class").contains("btn")).
                findAny();
        return neededElement;
    }

    private static String buildPath(Element target) {
        Element levelElement;
        Elements parents = target.parents();
        Collections.reverse(parents);
        parents.add(target);
        StringBuilder res = new StringBuilder("//");
        for (Element parent : parents) {
            levelElement = parent;
            int index = 1;
            if (levelElement.parent().children().size() > 1) {
                index = getElementIndex(levelElement.parent().children(), levelElement);
            }
            res.append(levelElement.nodeName()).
                    append("[").
                    append(index).
                    append("]/");
        }
        return res.toString();
    }

    private static int getElementIndex(Elements levelElements, Element curElement) {
        int index = 0;
        for (Element el : levelElements) {
            if (el.nodeName().equals(curElement.nodeName()))
                index++;
        }
        return index;
    }
}
