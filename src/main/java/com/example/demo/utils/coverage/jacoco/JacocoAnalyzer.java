package com.example.demo.utils.coverage.jacoco;

import com.example.demo.utils.coverage.jacoco.function.JacocoCountable;
import com.example.demo.utils.coverage.jacoco.model.constant.JacocoCounterType;
import com.example.demo.utils.coverage.jacoco.model.xml.JacocoCounter;
import com.example.demo.utils.coverage.jacoco.model.xml.JacocoReport;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * jacoco输出文件的解析器
 */
public class JacocoAnalyzer {

    public static JacocoReport analyzeReport(String jacocoReportFile) {
        final Path path = Paths.get(jacocoReportFile);
        final File jacocoXmlFile = path.toFile();
        if (!jacocoXmlFile.exists()) {
            System.out.println("JaCoCo XML file does not exist.");
            return null;
        }
        try {
            final JAXBContext jaxbContext = JAXBContext.newInstance(JacocoReport.class);
            final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            final XMLReader xmlReader = createXMLReader();
            final InputSource inputSource = new InputSource(new FileInputStream(jacocoXmlFile));
            final SAXSource source = new SAXSource(xmlReader, inputSource);
            return (JacocoReport) jaxbUnmarshaller.unmarshal(source);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    private static XMLReader createXMLReader() throws ParserConfigurationException, SAXException {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        spf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        spf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        spf.setNamespaceAware(true);

        SAXParser saxParser = spf.newSAXParser();
        XMLReader xmlReader = saxParser.getXMLReader();
        xmlReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false);
        return xmlReader;
    }

    public interface JacocoCountableClass extends JacocoCountable {

        default JacocoCounter getClassCounter() {
            return getCounter(JacocoCounterType.CLASS);
        }
    }

}
