package com.example.jiratimelog;

import java.io.*;
import java.nio.file.*;
import java.util.zip.*;

public class ExtractZip {
    public static void main(String[] args) throws Exception {
        String zipPath = args[0];
        String destDir = args[1];
        
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipPath))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path outPath = Paths.get(destDir, entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(outPath);
                } else {
                    Files.createDirectories(outPath.getParent());
                    Files.copy(zis, outPath, StandardCopyOption.REPLACE_EXISTING);
                }
                zis.closeEntry();
            }
        }
        System.out.println("Extracted to " + destDir);
    }
}