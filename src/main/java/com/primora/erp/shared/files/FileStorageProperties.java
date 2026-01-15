package com.primora.erp.shared.files;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "primora.files")
public class FileStorageProperties {

    private String documentsDir;

    public String getDocumentsDir() {
        return documentsDir;
    }

    public void setDocumentsDir(String documentsDir) {
        this.documentsDir = documentsDir;
    }
}
