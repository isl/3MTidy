Copyright 2015 Institute of Computer Science,
Foundation for Research and Technology - Hellas

Licensed under the EUPL, Version 1.1 or - as soon they will be approved
by the European Commission - subsequent versions of the EUPL (the "Licence");
You may not use this work except in compliance with the Licence.
You may obtain a copy of the Licence at:

http://ec.europa.eu/idabc/eupl

Unless required by applicable law or agreed to in writing, software distributed
under the Licence is distributed on an "AS IS" basis,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the Licence for the specific language governing permissions and limitations
under the Licence.

Contact:  POBox 1385, Heraklio Crete, GR-700 13 GREECE
Tel:+30-2810-391632
Fax: +30-2810-391638
E-mail: isl@ics.forth.gr
http://www.ics.forth.gr/isl

Authors :  Giannis Agathangelos, Georgios Samaritakis.

This file is part of the 3MTidy project.

3MTidy
======

3MTidy is a Java API used by [3MEditor] (https://github.com/isl/3MEditor) to detect useless or duplicate files and delete them.

## Build - Run
Folders src and lib contain all the files needed to build and create a jar file.

## Usage
The 3MTidy dependecies and licenses used are described in file 3MTidy-Dependencies-LicensesUsed.txt 

Basic usage:
```java
Tidy tidy = new Tidy("eXist xmlrpc URL", "eXist root collection", "eXist username", "eXist password", "server upload path");
//Scans a given folder and returns first duplicate filename (if any)
String duplicate = tidy.getDuplicate("File full path", "Folder full path");
//Clean up useless and duplicate files
tidy.run();

```

Read javadoc for more details.



