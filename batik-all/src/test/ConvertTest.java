import org.apache.batik.apps.rasterizer.DestinationType;
import org.apache.batik.apps.rasterizer.SVGConverter;
import org.apache.commons.io.IOUtils;
import org.dom4j.Attribute;
import org.dom4j.DocumentException;
import org.dom4j.VisitorSupport;
import org.python.google.common.io.Files;
import org.w3c.dom.Document;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

/**
 * Created by Egret on 2018/7/3.
 */
public class ConvertTest {
    public static void main(String[] args) throws Exception {
//        System.out.println("hello");
        String svg;
        try(FileReader reader = new FileReader("/Users/lucifer/Downloads/t5_1.svg")) {
//        try(FileReader reader = new FileReader("/Users/lucifer/Downloads/t7.svg")) {
            svg=IOUtils.toString(reader);
            System.out.println(svg);
        }
//        List<String> files=Files.readLines(new File("/Users/lucifer/Downloads/t6.svg"),Charset.defaultCharset());
//        System.out.println(files.size());
//        files.forEach(System.out::println);
//        String[] svgFiles = getPDFString(files);
//        for (String svgFile : svgFiles) {
//            System.out.println(svgFile);
//        }
        String[] svgFiles = getPDFString(Arrays.asList(svg));
        for (String svgFile : svgFiles) {
            System.out.println(svgFile);
        }
        File outputDir = new File("/Users/lucifer/Downloads/");
        SVGConverter converter = new SVGConverter();
        converter.setDefaultFontFamily("SimeHei");
        converter.setDestinationType(DestinationType.PDF);
        converter.setSources(svgFiles);
        converter.setDst(outputDir);
        converter.execute();





    }


    public static String[] getPDFString(List<String> contentsOriginal) throws
            org.xml.sax.SAXException, java.io.IOException, TransformerException, DocumentException {
        List<String> contents = new ArrayList<>();
        for (String content : contentsOriginal) {
//            log.trace("the content before convert: {}", content);
            content = parseSVG(content);

//            log.trace("the content after convert: {}", content);
            contents.add(content);
        }

        String[] svgFiles = new String[contents.size()];
        for (int i = 0; i < contents.size(); i++) {
            Document svgDocument = loadXMLFrom(contents.get(i));
            // Save this SVG into a file (required by SVG -> PDF transformation process)
            File svgFile = File.createTempFile(ff("graphic%s-", i), ".svg");
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            DOMSource source2 = new DOMSource(svgDocument);
            FileOutputStream fOut = new FileOutputStream(svgFile);
            try {
                transformer.transform(source2, new StreamResult(fOut));
            } finally {
                fOut.close();
            }
            svgFiles[i] = svgFile.toString();
        }
        return svgFiles;
    }


    static org.w3c.dom.Document loadXMLFrom(String xml) throws org.xml.sax.SAXException, java.io.IOException {
        return loadXMLFrom(new java.io.ByteArrayInputStream(xml.getBytes("utf-8")));
    }

    static org.w3c.dom.Document loadXMLFrom(java.io.InputStream is) throws org.xml.sax.SAXException,
            java.io.IOException {
        javax.xml.parsers.DocumentBuilderFactory factory =
                javax.xml.parsers.DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        javax.xml.parsers.DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
        } catch (javax.xml.parsers.ParserConfigurationException ex) {
        }
        org.w3c.dom.Document doc = builder.parse(is);
        is.close();
        return doc;
    }

    public static String ff(String template, Object... args) {
        return String.format(template, args);
    }


    static String  parseSVG(String svgContent) throws DocumentException {
        org.dom4j.Document document = SVGer.parseText(svgContent);
        document.accept(new SvgVisiter());
        return document.asXML();
    }

    private static final class SvgVisiter extends VisitorSupport {
        //        public void visit(Document document) {
        //        }
        //        public void visit(Namespace namespace) {
        //            namespace.detach();
        //        }
        public void visit(Attribute node) {
            if (node.getName().equalsIgnoreCase("fill") && node.getValue().matches("#[0-9,a-z]{6}")
                    && deviceColorMap.containsKey(node.getValue())) {
                String newValue = deviceColorMap.get(node.getValue());
                node.setValue(newValue);
            } else if (node.getName().equalsIgnoreCase("href")
                    && node.getNamespacePrefix().equalsIgnoreCase("xlink")
                    && node.getValue().matches("http.*_w\\d+_h\\d+\\.png")) {
                String newImageUrl = node.getValue().replaceAll("_w\\d+_h\\d+", "");
                node.setValue(newImageUrl);
            }
        }

    }


    static final Map<String, String> deviceColorMap = new HashMap<>();
    {
        deviceColorMap.put("#000000", "#000000 device-cmyk(0, 0, 0, 1)");
    }
}
