#!/bin/bash
javac -Xlint -source 1.8 -target 1.8 -cp .:jsoup-1.8.2.jar:commons-collections4-4.0.jar:commons-csv-1.1.jar Crawlee_DB.java CrawlECTutor.java Crawlee.java FileManager.java CSVmanager.java
