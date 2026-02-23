package de.cocondo.app.system.info;

import org.springframework.stereotype.Component;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;

import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class ApplicationInfoContext {

    private static final String POM_FILE_PATH = "pom.xml";

    private String groupId;
    private String artifactId;
    private String version;
    private String projectName;
    private String projectDescription;
    private String javaVersion;
    private String springBootVersion;
    private String buildTimestampFromJar;
    private String buildId;

    public ApplicationInfoContext() {
        readPomInfo();
    }

    public ApplicationInfoContextDTO getApplicationInfoDTO() {
        ApplicationInfoContextDTO dto = new ApplicationInfoContextDTO();
        dto.setGroupId(groupId);
        dto.setArtifactId(artifactId);
        dto.setVersion(version);
        dto.setProjectName(projectName);
        dto.setProjectDescription(projectDescription);
        dto.setJavaVersion(javaVersion);
        dto.setSpringBootVersion(springBootVersion);
        dto.setBuildTimestamp(buildTimestampFromJar);
        dto.setBuildId(buildId);
        return dto;
    }

    private void readPomInfo() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = loadPomFile(builder);

            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();

            groupId = (String) evaluateXPath(xpath, doc, "/project/groupId");
            artifactId = (String) evaluateXPath(xpath, doc, "/project/artifactId");
            version = (String) evaluateXPath(xpath, doc, "/project/version");
            projectName = (String) evaluateXPath(xpath, doc, "/project/name");
            projectDescription = (String) evaluateXPath(xpath, doc, "/project/description");
            javaVersion = (String) evaluateXPath(xpath, doc, "/project/properties/java.version");
            springBootVersion = (String) evaluateXPath(xpath, doc, "/project/parent/version");
            buildTimestampFromJar = readBuildTimestampFromJar();
            buildId = String.valueOf( generateLongBuildIdFromTimestamp(buildTimestampFromJar) );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Document loadPomFile(DocumentBuilder builder) {
        try {
            // Versuche, die pom.xml im Classpath zu finden
            ClassLoader classLoader = getClass().getClassLoader();
            URL resourceUrl = classLoader.getResource("META-INF/maven/de.cocondo.app/pom.xml");
            if (resourceUrl != null) {
                return builder.parse(resourceUrl.openStream());
            }

            // Wenn die pom.xml im Classpath nicht gefunden wurde, versuche, sie im ursprünglichen Pfad zu laden
            File pomFile = new File(POM_FILE_PATH);
            if (pomFile.exists()) {
                return builder.parse(pomFile);
            }

            // Wenn die pom.xml nirgends gefunden wurde, handle den Fehler entsprechend
            throw new RuntimeException("pom.xml konnte nicht gefunden werden");
        } catch (Exception e) {
            throw new RuntimeException("Fehler beim Laden der pom.xml: " + e.getMessage(), e);
        }
    }

    private Object evaluateXPath(XPath xpath, Document doc, String expression) throws Exception {
        XPathExpression xPathExpression = xpath.compile(expression);
        return xPathExpression.evaluate(doc, XPathConstants.STRING);
    }

    private String readBuildTimestampFromJar() {
        try {
            URL jarUrl = getClass().getProtectionDomain().getCodeSource().getLocation();
            File jarFile = new File(jarUrl.toURI());
            long lastModified = jarFile.lastModified();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return dateFormat.format(new Date(lastModified));
        } catch (Exception e) {
            return "N/A";
        }
    }

    private String generateBuildIdFromTimestamp(String timestamp) {
        String[] parts = timestamp.split(" ");
        String datePart = parts[0].replace("-", "");
        String timePart = parts[1].replace(":", "");
        return datePart + timePart;
    }


    private long generateLongBuildIdFromTimestamp(String timestamp) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = dateFormat.parse(timestamp);
            return date.getTime() / 1000L; // Verwende 64-Bit-Zahl, um das Y2.038K Problem zu vermeiden
        } catch (Exception e) {
            return 0L; // Fallback-Wert im Fehlerfall
        }
    }


    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getProjectDescription() {
        return projectDescription;
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public String getSpringBootVersion() {
        return springBootVersion;
    }

    public String getBuildTimestampFromJar() {
        return buildTimestampFromJar;
    }

}
