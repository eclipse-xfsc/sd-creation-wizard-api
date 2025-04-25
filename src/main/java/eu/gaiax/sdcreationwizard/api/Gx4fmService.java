package eu.gaiax.sdcreationwizard.api;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;


/**
 * 'getRepoAndCopyShaclFiles' is the primary method called from the controller class.
 * If the Gx4FmFramework is enabled it clones or pulls the "/ontology-management-base" Repository
 * and copy the needed shapes into the "shapes" dir
 */
@Service
public class Gx4fmService {

    private static final String REPO_URL = "https://github.com/GAIA-X4PLC-AAD/ontology-management-base.git";
    private static final Logger logger = LoggerFactory.getLogger(Gx4fmService.class);
    private static final List<String> skipFiles = Arrays.asList("gx", "src");

    //@Value("${sdcreationwizard.gx4fm.enabled}") //TODO use with a startup runner instead of the hardcoded value
    public static final boolean isGx4FmFrameworkEnabled = true;


    public static void getRepoAndCopyShaclFiles() {
        logger.info("Starting to process ontology-management-base Repo... ");
        File localPath = new File("./ontology-management-base_temp"); // Specify a temp local path

        if (localPath.exists()) {
            //if local repository exist discard locale changes and pull origin
            try (Git git = Git.open(localPath)) {
                if (!git.status().call().isClean()) {
                    git.reset().setMode(org.eclipse.jgit.api.ResetCommand.ResetType.HARD).call();
                }
                git.pull().call();
            } catch (GitAPIException | IOException e) {
                logger.error("Failed to pull repo: {}  ", e.getMessage());
                return;
            }
        } else {
            // clone repo if not exists
            try {
                Git.cloneRepository()
                .setURI(REPO_URL)
                .setBranch("main")
                .setDirectory(localPath)
                .call();
            } catch (GitAPIException e) {
                logger.error("Failed to clone repo: {}  ", e.getMessage());
                return;
            }
        }
        try {
            // Prepare the destination path
            File destFolder = new File("./shapes/gx4fm-plc-aad/Other");
            if (destFolder.exists()) {
                deleteContent(destFolder);
            } else {
                destFolder.mkdirs();
            }

            copyShaclFiles(localPath, destFolder);
        } catch (IOException ex) {
            logger.error("Failed to copy files: {}  ", ex.getMessage());
        }
    }

    /**
     * Search for "_shacl.ttl" files and copy to the "shapes" folder
     * @param localPath File
     * @throws IOException if copy fails
     */
    private static void copyShaclFiles(File localPath, File destFolder) throws IOException {
        // Iterate through each sub folder in the parent folder to find the "_shacl.ttl" files and copy them
        for (File folder : Objects.requireNonNull(localPath.listFiles())) {
            if (folder.isDirectory() && !skipFiles.contains(folder.getName())) {
                for (File file : Objects.requireNonNull(folder.listFiles())) {
                    if (file.isFile() && file.getName().contains("_shacl.ttl")) {
                        logger.info("Copy shacl file {}", file.getName());
                        Path targetPath = destFolder.toPath().resolve(file.getName());
                        Files.copy(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        }
    }

    /**
     * Delete all files from dir
     * @param directory File
     */
    private static void deleteContent(File directory){
        Collection<File> shaclFilesInDir = List.of(Objects.requireNonNull(directory.listFiles()));
        logger.info("delete files {}", shaclFilesInDir.size());
        shaclFilesInDir.forEach(FileUtils::deleteQuietly);
    }
}