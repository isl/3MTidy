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
package gr.forth.ics.isl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Class for finding duplicate files in a folder
  * @author jagathan
 *
 */
public class Folder {

    private static final String hashAlgorithm = "SHA1";	 //algorithm using for the hash function

    private HashMap<String, ArrayList<String>> fileMap;	//HashMap that holds the files with the duplicates

    private String path; //String that holds the folder path

    /**
     * default constructor
     */
    public Folder() {
        this.fileMap = new HashMap<String, ArrayList<String>>();
    }

    /**
     * sets fileMap class member
     *
     * @param fileMap
     */
    public void setFileMap(HashMap<String, ArrayList<String>> fileMap) {
        this.fileMap = fileMap;
    }

    /**
     * returns class member fileMap
     *
     * @return
     */
    public Map<String, ArrayList<String>> getFileMap() {
        return this.fileMap;
    }

    /**
     * setter for class member path
     *
     * @param path
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * getter for class member path
     *
     * @return
     */
    public String getPath() {
        return this.path;
    }

    /**
     * searches inside the folder to find a file that equals with given file
     * return the equal's fileName returns null if there is no equal file
     *
     * @param inputFilePath
     * @param folderPath
     * @return
     */
    public ArrayList<String> findDuplicates(String inputFilePath, String folderPath) {
        ArrayList<String> sameFiles = new ArrayList<String>();

        File folder = new File(folderPath);
        File[] listOfFiles = folder.listFiles();

        String inputFileChecksum = null;
        String fileName = null;

        try {
            inputFileChecksum = generateChecksum(inputFilePath, hashAlgorithm);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (File file : listOfFiles) {
            if (file.isFile()) {
                String filePath = file.getAbsolutePath();
                fileName = file.getName();
                String fileChecksum = null;
                try {
                    fileChecksum = generateChecksum(filePath, hashAlgorithm);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (inputFileChecksum.equals(fileChecksum)) {
                    sameFiles.add(fileName);
                }
            }
        }
        return sameFiles;
    }

    /**
     * helper method if fileName exists in the map's lists returns true else
     * return false
     *
     * @param map
     * @param fileName
     * @return
     */
    private boolean containsFileInList(Map<String, ArrayList<String>> map, String fileName) {

        for (Map.Entry<String, ArrayList<String>> entry : map.entrySet()) {
            if (entry.getValue().contains(fileName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * searches in the folder with the given name puts in HashMap all the files
     * of the folder as keys and puts as values the file that has the same
     * context
     *
     * @param folderPath
     * @return
     */
    public HashMap<String, ArrayList<String>> mapDuplicates(String folderPath) {

        HashMap<String, ArrayList<String>> sameFilesMap = this.fileMap;
        File folder = new File(folderPath);
        File[] listOfFiles = folder.listFiles();

        for (File file : listOfFiles) {
            String fileName = file.getName();
            String filePath = file.getAbsolutePath();
            if (!containsFileInList(sameFilesMap, fileName)) //skip extra information
            {
                sameFilesMap.put(fileName, findDuplicates(filePath, folderPath));
            }
        }
        return sameFilesMap;
    }

    /**
     * Generates checksum with the given algorithm returns the hex string of the
     * checksum
     *
     * @param fileName
     * @return
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    private String generateChecksum(String filePath, String algorithm)
            throws NoSuchAlgorithmException, IOException {

        MessageDigest md = MessageDigest.getInstance(algorithm);
        FileInputStream fis = new FileInputStream(filePath);

        byte[] dataBytes = new byte[1024];
        int nread = 0;

        while ((nread = fis.read(dataBytes)) != -1) {
            md.update(dataBytes, 0, nread);
        }
        fis.close();

        byte[] mdBytes = md.digest();

        return bytesToHex(mdBytes);
    }

    /**
     * converts byte array to hex String returns hex String
     *
     * @param byteArray
     * @return
     */
    private String bytesToHex(byte[] byteArray) {

        StringBuffer sb = new StringBuffer("");
        String hex = null;
        for (int i = 0; i < byteArray.length; i++) {
            sb.append(Integer.toString((byteArray[i] & 0xff) + 0x100, 16).substring(1));
        }

        hex = sb.toString();
        return hex;
    }
}
