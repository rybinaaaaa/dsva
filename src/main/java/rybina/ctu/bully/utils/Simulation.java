package rybina.ctu.bully.utils;

import java.util.*;

public class Simulation {

    public static HashMap<String, Simulation.FileInfo> enrichFakeData() {
        HashMap<String, Simulation.FileInfo> fileSystem = new HashMap<>();

        List<Simulation.FileInfo> fakeFiles = Arrays.asList(
                new Simulation.FileInfo("readme.txt", "This is a README file.", Arrays.asList(PermissionRole.GUEST, PermissionRole.USER)),
                new Simulation.FileInfo("config.json", "{\"setting\": \"value\"}", Arrays.asList(PermissionRole.ADMIN)),
                new Simulation.FileInfo("data.csv", "name,age\nAlice,30\nBob,25", Arrays.asList(PermissionRole.USER, PermissionRole.ADMIN)),
                new Simulation.FileInfo("script.sh", "#!/bin/bash\necho Hello World", Arrays.asList(PermissionRole.ADMIN)),
                new Simulation.FileInfo("notes.md", "# Notes\nSome important notes.", Arrays.asList(PermissionRole.USER)),
                new Simulation.FileInfo("project.docx", "Project documentation content.", Arrays.asList(PermissionRole.ADMIN)),
                new Simulation.FileInfo("image.png", "<binary data>", Arrays.asList(PermissionRole.GUEST)),
                new Simulation.FileInfo("video.mp4", "<binary video data>", Arrays.asList(PermissionRole.USER, PermissionRole.ADMIN)),
                new Simulation.FileInfo("presentation.pptx", "Presentation slides content.", Arrays.asList(PermissionRole.ADMIN)),
                new Simulation.FileInfo("archive.zip", "<binary zip data>", Arrays.asList(PermissionRole.USER, PermissionRole.ADMIN)),
                new Simulation.FileInfo("music.mp3", "<binary music data>", Arrays.asList(PermissionRole.GUEST, PermissionRole.USER)),
                new Simulation.FileInfo("database.db", "<binary database data>", Arrays.asList(PermissionRole.ADMIN)),
                new Simulation.FileInfo("log.txt", "Log file content.", Arrays.asList(PermissionRole.USER)),
                new Simulation.FileInfo("error.log", "Error log content.", Arrays.asList(PermissionRole.ADMIN)),
                new Simulation.FileInfo("backup.bak", "Backup file data.", Arrays.asList(PermissionRole.ADMIN)),
                new Simulation.FileInfo("report.pdf", "Report content.", Arrays.asList(PermissionRole.USER, PermissionRole.ADMIN)),
                new Simulation.FileInfo("invoice.xls", "Invoice data.", Arrays.asList(PermissionRole.ADMIN)),
                new Simulation.FileInfo("thumbnail.jpg", "<binary image data>", Arrays.asList(PermissionRole.GUEST)),
                new Simulation.FileInfo("diagram.svg", "<SVG content>", Arrays.asList(PermissionRole.USER)),
                new Simulation.FileInfo("dockerfile", "# Dockerfile content", Arrays.asList(PermissionRole.ADMIN)),
                new Simulation.FileInfo("makefile", "# Makefile content", Arrays.asList(PermissionRole.USER, PermissionRole.ADMIN)),
                new Simulation.FileInfo("readme.md", "# Readme file", Arrays.asList(PermissionRole.GUEST, PermissionRole.USER)),
                new Simulation.FileInfo("setup.py", "# Setup script for Python", Arrays.asList(PermissionRole.ADMIN)),
                new Simulation.FileInfo("service.yaml", "# Service configuration", Arrays.asList(PermissionRole.ADMIN)),
                new Simulation.FileInfo("api.json", "{\"api\": \"data\"}", Arrays.asList(PermissionRole.USER, PermissionRole.ADMIN)),
                new Simulation.FileInfo("key.pem", "<private key>", Arrays.asList(PermissionRole.ADMIN)),
                new Simulation.FileInfo("certificate.crt", "<certificate data>", Arrays.asList(PermissionRole.ADMIN)),
                new Simulation.FileInfo("deployment.txt", "Deployment instructions.", Arrays.asList(PermissionRole.USER)),
                new Simulation.FileInfo("changelog.txt", "Changelog content.", Arrays.asList(PermissionRole.USER, PermissionRole.ADMIN))
        );

        for (Simulation.FileInfo file : fakeFiles) {
            fileSystem.put(file.getName(), file);
        }

        return fileSystem;
    }

    public static class FileInfo {
        private String name;
        private String content;
        private List<PermissionRole> roles;

        public FileInfo(String name, String content, List<PermissionRole> roles) {
            this.name = name;
            this.content = content;
            this.roles = roles;
        }

        public String getName() {
            return name;
        }

        public String getContent() {
            return content;
        }

        public List<PermissionRole> getRoles() {
            return roles;
        }

        @Override
        public String toString() {
            return "FileInfo{" +
                    "name='" + name + '\'' +
                    ", content='" + content + '\'' +
                    ", roles=" + roles +
                    '}';
        }
    }

    public static enum PermissionRole {
        GUEST, USER, ADMIN
    }
}
