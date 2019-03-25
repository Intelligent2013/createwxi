package dyuzhev.createwxi;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.util.UUID;

/**
 *
 * @author Alexander Dyuzhev
 */
public class createwxi {
    
    static int componentsCounter = -1;
    static int fileCounter = -1;
    static String name = "";
    static String inputFolder = "";
    static Document doc;
    static String companyName = "My Company";
    
    private static void processDirectory(String dir, Element parentElement) throws IOException {
        Element directoryElement = doc.createElement("Directory");
        if (componentsCounter==-1) {
            directoryElement.setAttribute("Id", "APPLICATIONFOLDER");
        } else {
            directoryElement.setAttribute("Id", "dirid"+fileCounter);
        }
        File currentPath = new File(dir);
        String folderName = currentPath.getName();
        directoryElement.setAttribute("Name", folderName);
        parentElement.appendChild(directoryElement);
        
        componentsCounter++;
        {
            Element componentElement = doc.createElement("Component");
            componentElement.setAttribute("Id", "comp"+componentsCounter);
            componentElement.setAttribute("DiskId", "1");
            componentElement.setAttribute("Guid", UUID.randomUUID().toString());
            directoryElement.appendChild(componentElement);
            
            {
                Element createFolderElement = doc.createElement("CreateFolder");
                componentElement.appendChild(createFolderElement);
                Element removeFolderElement =  doc.createElement("RemoveFolder");
                fileCounter++;
                removeFolderElement.setAttribute("Id", "RemoveDir"+fileCounter);
                removeFolderElement.setAttribute("On", "uninstall");
                componentElement.appendChild(removeFolderElement);
                
                if (componentsCounter==0) {
                    Element registryKeyElement = doc.createElement("RegistryKey");
                    registryKeyElement.setAttribute("Root", "HKCU");
                    registryKeyElement.setAttribute("Key", "Software\\" + companyName + "\\" + name);
                    registryKeyElement.setAttribute("Action", "createAndRemoveOnUninstall");
                    componentElement.appendChild(registryKeyElement);
                    {
                        Element registryValueElement = doc.createElement("RegistryValue");
                        registryValueElement.setAttribute("Name", "Version");
                        registryValueElement.setAttribute("Value", "1.0");
                        registryValueElement.setAttribute("Type", "string");
                        registryValueElement.setAttribute("KeyPath", "yes");
                        registryKeyElement.appendChild(registryValueElement);
                    }
                }
                
                for (final File fileEntry : currentPath.listFiles()) {
                    if (!fileEntry.isDirectory() ) { //&& !fileEntry.getName().startsWith(".")
                        //listFilesForFolder(fileEntry);
                    //} else {
                        //System.out.println(fileEntry.getName());
                        Element fileElement = doc.createElement("File");
                        fileCounter++;
                        if (componentsCounter==0 && fileEntry.getName().equalsIgnoreCase(name+".exe")) {
                            fileElement.setAttribute("Id", "LauncherId");
                            fileCounter--;
                            Element shortcutElement = doc.createElement("Shortcut");
                            shortcutElement.setAttribute("Id", "ExeShortcut");
                            shortcutElement.setAttribute("Directory", "ProgramMenuDir");
                            shortcutElement.setAttribute("Name", name);
                            shortcutElement.setAttribute("Advertise", "no");
                            shortcutElement.setAttribute("Icon", "StartMenuIcon.exe");
                            shortcutElement.setAttribute("IconIndex", "0");
                            fileElement.appendChild(shortcutElement);
                        } else {
                            fileElement.setAttribute("Id", "FileId"+fileCounter);
                        }
                        fileElement.setAttribute("Name", fileEntry.getName());
                        //file path relative to inputFolder
                        File inputPath = new File(inputFolder);
                        
                        fileElement.setAttribute("Source", fileEntry.getPath().substring(inputPath.getPath().length()+1));
                        componentElement.appendChild(fileElement);
                    }
                }
                
                for (final File fileEntry : currentPath.listFiles()) {
                    if (fileEntry.isDirectory()) {
                        fileCounter++;
                        processDirectory(fileEntry.getAbsolutePath(), directoryElement);
                    }
                }
                /*//for each file in current directory
                try (Stream<Path> paths = Files.walk(Paths.get(dir))) {
                    paths
                        .filter(Files::isRegularFile)
                        .forEach(System.out::println);
                }*/
            }
        }
        
    
        
        //return directoryElement3;
    }
    
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage: java -jar createwxi.jar <software name> <input folder>");
            return;
        }
        name = args[0];
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        doc = docBuilder.newDocument();
        // Create Include root element 
        Element includeRootElement = doc.createElement("Include");
        doc.appendChild(includeRootElement);
        // Create Directory Element
        Element directoryElement = doc.createElement("Directory");
        directoryElement.setAttribute("Id", "TARGETDIR");
        directoryElement.setAttribute("Name", "SourceDir");
        includeRootElement.appendChild(directoryElement);
        
        {
                Element directoryElement2 = doc.createElement("Directory");
                directoryElement2.setAttribute("Id", "ProgramFilesFolder");
                directoryElement2.setAttribute("Name", "PFiles");
                directoryElement.appendChild(directoryElement2);
                
                {
                    
                    inputFolder = args[1];
                    processDirectory(inputFolder, directoryElement2);
                    {
                        
                        
                    }
                }
                
                directoryElement2 = doc.createElement("Directory");
                directoryElement2.setAttribute("Id", "ProgramMenuFolder");
                directoryElement.appendChild(directoryElement2);
                {
                    Element directoryElement3 = doc.createElement("Directory");
                    directoryElement3.setAttribute("Id", "ProgramMenuDir");
                    directoryElement3.setAttribute("Name", companyName);
                    directoryElement2.appendChild(directoryElement3);
                    
                    componentsCounter++;
                    Element componentElement = doc.createElement("Component");
                    componentElement.setAttribute("Id", "comp"+componentsCounter);
                    componentElement.setAttribute("Guid", "76632624-6d0c-415c-a000-a1781d9d2433");
                    directoryElement3.appendChild(componentElement);
                    {
                        Element removeFolderElement = doc.createElement("RemoveFolder");
                        removeFolderElement.setAttribute("Id", "ProgramMenuDir");
                        removeFolderElement.setAttribute("On", "uninstall");
                        componentElement.appendChild(removeFolderElement);
                        
                        Element registryValueElement = doc.createElement("RegistryValue");
                        registryValueElement.setAttribute("Root", "HKCU");
                        registryValueElement.setAttribute("Key", "Software\\" + companyName + "\\" + name);
                        registryValueElement.setAttribute("Type", "string");
                        registryValueElement.setAttribute("Value", "");
                        componentElement.appendChild(registryValueElement);
                        
                    }
                }
        }
        
        Element directoryRefElement = doc.createElement("DirectoryRef");
        directoryRefElement.setAttribute("Id", "TARGETDIR");
        includeRootElement.appendChild(directoryRefElement);
      
        Element componentElement = doc.createElement("Component");
        componentElement.setAttribute("Id", "RegistryEntries");
        componentElement.setAttribute("Guid", "197A1214-8064-41EF-A38F-24954E7263D8");
        directoryRefElement.appendChild(componentElement);
        
        Element registryKeyElement = doc.createElement("RegistryKey");
        registryKeyElement.setAttribute("Root", "HKCR");
        registryKeyElement.setAttribute("Key", "SystemFileAssociations\\.docx\\shell\\Check with " + name + "\\command");
        registryKeyElement.setAttribute("Action", "createAndRemoveOnUninstall");
        componentElement.appendChild(registryKeyElement);
        
        Element registryValueElement = doc.createElement("RegistryValue");
        registryValueElement.setAttribute("Type", "string");
        registryValueElement.setAttribute("Value", "&quot;[APPLICATIONFOLDER]\\" + name + ".exe&quot; &quot;%1&quot;");
        registryKeyElement.appendChild(registryValueElement);
        
        registryKeyElement = doc.createElement("RegistryKey");
        registryKeyElement.setAttribute("Root", "HKCR");
        registryKeyElement.setAttribute("Key", "SystemFileAssociations\\.doc\\shell\\Check with " + name + "\\command");
        registryKeyElement.setAttribute("Action", "createAndRemoveOnUninstall");
        componentElement.appendChild(registryKeyElement);
        
        registryValueElement = doc.createElement("RegistryValue");
        registryValueElement.setAttribute("Type", "string");
        registryValueElement.setAttribute("Value", "&quot;[APPLICATIONFOLDER]\\" + name + ".exe&quot; &quot;%1&quot;");
        registryKeyElement.appendChild(registryValueElement);
        
        Element featureElement = doc.createElement("Feature");
        featureElement.setAttribute("Id", "DefaultFeature");
        featureElement.setAttribute("Title", "Main Feature");
        featureElement.setAttribute("Level", "1");
        includeRootElement.appendChild(featureElement);
        
        for (int i=0; i <= componentsCounter; i++) {
            Element componentRefElement = doc.createElement("ComponentRef");
            componentRefElement.setAttribute("Id", "comp" + i);
            featureElement.appendChild(componentRefElement);
        }
        
        Element componentRefElement = doc.createElement("ComponentRef");
        componentRefElement.setAttribute("Id", "RegistryEntries");
        featureElement.appendChild(componentRefElement);
        componentRefElement = doc.createElement("ComponentRef");
        componentRefElement.setAttribute("Id", "CleanupMainApplicationFolder");
        featureElement.appendChild(componentRefElement);
        
         // Save Document to bundle.wxi file
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        StringWriter writer = new StringWriter();
        String outfile = "bundle.wxi";
        StreamResult result = new StreamResult(new File(outfile));
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(new DOMSource(doc), result);
        System.out.println("File " + outfile + " created.");
    }
}
