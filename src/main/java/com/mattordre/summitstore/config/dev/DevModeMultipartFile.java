package com.mattordre.summitstore.config.dev;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

@Getter
class DevModeMultipartFile implements MultipartFile {

    private final byte[] content;

    private final String name;

    private final String originalFilename;

    private final String contentType;


    public DevModeMultipartFile(File file) throws IOException {
        this.content = Files.readAllBytes(file.toPath());
        this.name = file.getName();
        this.originalFilename = file.getName();
        this.contentType = Files.probeContentType(file.toPath());
    }

    @Override
    public boolean isEmpty() { return content.length == 0; }

    @Override
    public long getSize() { return content.length; }

    @Override
    public byte @NotNull [] getBytes() { return content; }

    @Override
    public @NotNull InputStream getInputStream() { return new ByteArrayInputStream(content); }

    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException { Files.write(dest.toPath(), content); }


}
