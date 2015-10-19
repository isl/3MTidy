/*
 * Copyright 2015 Institute of Computer Science,
 * Foundation for Research and Technology - Hellas
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 *
 * Contact:  POBox 1385, Heraklio Crete, GR-700 13 GREECE
 * Tel:+30-2810-391632
 * Fax: +30-2810-391638
 * E-mail: isl@ics.forth.gr
 * http://www.ics.forth.gr/isl
 *
 * Authors :  Giannis Agathangelos, Georgios Samaritakis.
 *
 * This file is part of the 3MTidy project.
 */
package isl;

import isl.dbms.DBCollection;
import isl.dbms.DBMSException;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Tidy class is used to clean up files used by 3MEditor. It removes useless
 * files from eXist and server filesystem.
 *
 * @author samarita
 */
public class Tidy {

    /**
     *
     */
    public final String DBURL,
            /**
             *
             */
            DBCollection,
            /**
             *
             */
            DBUsername,
            /**
             *
             */
            DBPassword,
            /**
             *
             */
            serverPath;
    private final DBCollection rootCol, x3mlCol;

    /**
     * Default Tidy constructor
     *
     * @param databaseURL eXist database URL as a <code>String</code>
     * @param rootCollection eXist root collection as a <code>String</code>
     * @param databaseUser eXist database user as a <code>String</code>
     * @param databasePass eXist database password as a <code>String</code>
     * @param uploadsPath Server upload path as a <code>String</code>
     */
    public Tidy(String databaseURL, String rootCollection, String databaseUser, String databasePass, String uploadsPath) {
        DBURL = databaseURL;
        DBCollection = rootCollection;
        DBUsername = databaseUser;
        DBPassword = databasePass;
        serverPath = uploadsPath;
        rootCol = new DBCollection(DBURL, DBCollection, DBUsername, DBPassword);
        x3mlCol = new DBCollection(DBURL, DBCollection + "/3M/x3ml", DBUsername, DBPassword);

    }

    /**
     * Performs actual clean up
     */
    public void run() {
        System.out.println("*****************************");
        int[] numbers = deleteNotUsedFiles();

        System.out.println("Deleting useless files report");
        System.out.println("Server:" + numbers[0]);
        System.out.println("eXist:" + numbers[1]);
        System.out.println("*****************************");
        numbers = deleteDuplicateFiles();

        System.out.println("Deleting duplicate files report");
        System.out.println("Server:" + numbers[0]);
        System.out.println("eXist:" + numbers[1]);

        System.out.println("*****************************");
        System.out.println("Deleting eXist leftovers report");
        System.out.println("eXist:" + deleteExistLeftovers());

    }

    /**
     * Deletes files from x3ml eXist collection that are not used by any
     * mapping.
     *
     * @return Number of files deleted as a <code>int</code>
     */
    public int deleteExistLeftovers() {
        int delCounter = 0;
        ArrayList<String> allFilesReferencedByMappings = getFilesUsedInMappings();
        System.out.println("Files referenced by mappings:" + allFilesReferencedByMappings.size());

        String[] allFilesInExist = x3mlCol.listFiles();
        System.out.println("Initial number of files inside x3ml collection:" + allFilesInExist.length);

        for (String fileInExist : allFilesInExist) {
            if (!(fileInExist.equals("Template.xml") || fileInExist.equals("schemata_list.xml") || fileInExist.equals("3MTemplate.xml") || fileInExist.equals("Help.xml") || fileInExist.equals("cidoc_crm_v6.0-draft-2015January.rdfs") || fileInExist.equals("cidoc_crm_v5.1-draft-2013May.rdfs")) && !allFilesReferencedByMappings.contains(fileInExist)) {
                try {
//                    System.out.println("REMOVING:"+fileInExist);
                    x3mlCol.removeFile(fileInExist);
                    delCounter = delCounter + 1;

                } catch (DBMSException ex) {
                    ex.printStackTrace();
                }
            }
        }

        return delCounter;

    }

    /**
     * Deletes files from server that are not used by any mapping and then
     * delete those from eXist too.
     *
     * @return Number of files deleted as a <code>int[]</code>. First item is
     * number of server files deleted, second item is number of eXist files
     * deleted.
     */
    public int[] deleteNotUsedFiles() {
        ArrayList<String> allFilesReferencedByMappings = getFilesUsedInMappings();
        System.out.println("Files referenced by mappings:" + allFilesReferencedByMappings.size());
        ArrayList<String> allFilesInServer = new ArrayList<String>();
        allFilesInServer = getFilesInFolder(serverPath, allFilesInServer);
        System.out.println("Initial number of files in server:" + allFilesInServer.size());

        //First of all delete files from Tomcat that are not used.
        ArrayList<String> fileNamesToDelete = deleteDeadFilesFromServer(allFilesReferencedByMappings, allFilesInServer);

        int[] deletedFiles = new int[2];
        deletedFiles[0] = fileNamesToDelete.size();
        //Then delete files from eXist that are not used.

        deletedFiles[1] = deleteDeadFilesFromExist(fileNamesToDelete, x3mlCol);
        return deletedFiles;
    }

    /**
     * Deletes duplicate files from server and then deletes those from eXist
     * too.
     *
     * @return Number of files deleted as a <code>int[]</code>. First item is
     * number of server files deleted, second item is number of eXist files
     * deleted.
     */
    public int[] deleteDuplicateFiles() {

        ArrayList<String> allFoldersInServer = new ArrayList<String>();
        allFoldersInServer = getFoldersInServer(serverPath, allFoldersInServer);
//        System.out.println("Folders in server:" + allFoldersInServer);

        for (String folderPath : allFoldersInServer) {
            ArrayList<String> allFilesInFolder = new ArrayList<String>();
            allFilesInFolder = getFilesInFolder(folderPath, allFilesInFolder);
            System.out.println("Initial number of files in folder (" + folderPath + "):" + allFilesInFolder.size());

            if (allFilesInFolder.size() > 0) {
                Folder folder = new Folder();
                HashMap<String, ArrayList<String>> duplicates = folder.mapDuplicates(folderPath);
                System.out.println("Final number of files in folder (" + folderPath + "):" + duplicates.size());
                for (String fileToKeep : duplicates.keySet()) {
//                    System.out.println("KEEPING: " + fileToKeep);
                    for (String file : duplicates.get(fileToKeep)) {
                        if (!file.equals(fileToKeep)) {
//                            System.out.println("DELETING: " + file);

                            updateFilenamesInAttributes(file, fileToKeep);
                            try {
                                x3mlCol.removeFile(file);
                            } catch (DBMSException ex) {
                                ex.printStackTrace();
                            }
                            new File(folderPath + System.getProperty("file.separator") + file).delete();

//
//                        System.out.println("SEARCHING FOR DB USAGES");
                        }
                    }
                }
            } else {
                System.out.println("Final number of files in folder (" + folderPath + "):0");
            }
        }

        int[] deletedFiles = new int[2];
//        deletedFiles[0] = fileNamesToDelete.size();
//        //Then delete files from eXist that are not used.
//
//        deletedFiles[1] = deleteDeadFilesFromExist(fileNamesToDelete, x3mlCol);
        return deletedFiles;
    }

    /**
     * Gets first duplicate file for a given file path
     * @param filePath Filepath to check as a <code>String</code>
     * @param folderPath Folderpath to check as a <code>String</code>
     * @return Name of first duplicate file found as a <code>String</code>
     */
    public String getDuplicate(String filePath, String folderPath) {
        Folder folder = new Folder();
        ArrayList<String> duplicates = folder.findDuplicates(filePath, folderPath);
//        System.out.println("DUPS="+duplicates);
        if (duplicates != null) {
            if (duplicates.size() > 1) {
                for (String dup : duplicates) {
                    if (!filePath.endsWith(dup)) { //Return first filename that is not the same as input
                        return dup;
                    }
                }
            }
        }
        return null;

    }

    private static ArrayList<String> deleteDeadFilesFromServer(ArrayList<String> allFilesInExist, ArrayList<String> allFilesInServer) {
        int delCounter = 0;
        ArrayList<String> results = new ArrayList();

        for (String filepath : allFilesInServer) {
            String filename = filepath.substring(filepath.lastIndexOf(System.getProperty("file.separator")) + 1);

            if (!(filename.endsWith(".txt") || filename.equals("cidoc_crm_v5.1-draft-2013May.rdfs")) && !allFilesInExist.contains(filename)) {
                delCounter = delCounter + 1;
                results.add(filename);
                //TODO add Remove code
                new File(filepath).delete();
            }

        }
        System.out.println("Deleting " + delCounter + " files from Server!");
        return results;
    }

    private static int deleteDeadFilesFromExist(ArrayList<String> fileNamesToDelete, DBCollection x3mlCol) {
        ArrayList<String> results = new ArrayList();
        String[] x3mlFiles = x3mlCol.listFiles();
        System.out.println("Files inside x3ml collection: " + x3mlFiles.length);
        int delCounter = 0;
        for (String x3mlFile : x3mlFiles) {
            if (!(x3mlFile.equals("Template.xml") || x3mlFile.equals("schemata_list.xml") || x3mlFile.equals("3MTemplate.xml") || x3mlFile.equals("Help.xml")) && fileNamesToDelete.contains(x3mlFile)) {
                delCounter = delCounter + 1;
//                                    System.out.println("REMOVING:"+x3mlFile);

//                System.out.println(x3mlFile);
                //TODO add Remove code
                x3mlCol.removeFile(x3mlFile);
            } else {
//                System.out.println("KEEPING:"+x3mlFile);
            }
        }
        System.out.println("Deleting " + delCounter + " files from eXist!");
        return delCounter;

    }

    /**
     * Checks if a file is used in a mapping or not
     * @param filename Filename to check as a <code>String</code>
     * @return If file is used then <code>true</code>, else <code>false</code>
     */
    public boolean isFileUsedInMapping(String filename) {
        String[] usages = rootCol.query("//info//@*[.='" + filename + "']");
        if (usages != null) {
            if (usages.length > 0) {
                return true;
            }
        }
        return false;
    }

    private void updateFilenamesInAttributes(String oldFilename, String newFilename) {
        rootCol.xUpdate("//info//@*[.='" + oldFilename + "']", newFilename);

//        info//@*='CRMarchaeo_v1.2___07-10-2014171845___983___01-12-2014135018___893___19-03-2015112124___2815.rdfs' 
    }

    private ArrayList<String> getFilesUsedInMappings() {
        return new ArrayList(Arrays.asList(rootCol.query("distinct-values(//@xml_link|//@generator_link|//@html_link|//@rdf_link|//@schema_file)")));
    }

    private ArrayList<String> getFilesInFolder(String path, ArrayList<String> files) {
        File root = new File(path);
        File[] list = root.listFiles();

        if (list == null) {
            return new ArrayList<String>();
        }

        for (File f : list) {
            if (f.isDirectory()) {
                getFilesInFolder(f.getAbsolutePath(), files);
//                System.out.println("Dir:" + f.getAbsoluteFile());
            } else {
//                System.out.println("File:" + f.getAbsoluteFile());
                files.add(f.getAbsolutePath());

            }
        }
        return files;
    }

    private ArrayList<String> getFoldersInServer(String path, ArrayList<String> folders) {
        File root = new File(path);
        File[] list = root.listFiles();

        if (list == null) {
            return new ArrayList<String>();
        }

        for (File f : list) {
            if (f.isDirectory()) {
                folders.add(f.getAbsolutePath());
                getFoldersInServer(f.getAbsolutePath(), folders);
//                System.out.println("Dir:" + f.getAbsoluteFile());
            }
        }
        return folders;
    }

}
