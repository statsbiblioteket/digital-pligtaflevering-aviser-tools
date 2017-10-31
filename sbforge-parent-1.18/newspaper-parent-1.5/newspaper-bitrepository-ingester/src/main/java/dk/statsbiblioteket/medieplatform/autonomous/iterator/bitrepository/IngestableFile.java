package dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository;

import java.net.URL;

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;

/**
 * Contains all relevant attributes need for ingest of a file into a bitrepository.
 */
public class IngestableFile {
    private final String FileID;
    private final URL localUrl;
    private final ChecksumDataForFileTYPE checksum;
    private final Long fileSize;
    private final String path;

    public IngestableFile(String fileID, URL localUrl, ChecksumDataForFileTYPE checksum, Long fileSize, String path) {
        FileID = fileID;
        this.localUrl = localUrl;
        this.checksum = checksum;
        this.fileSize = fileSize;
        this.path = path;
    }

    public String getFileID() {
        return FileID;
    }

    public URL getLocalUrl() {
        return localUrl;
    }

    public ChecksumDataForFileTYPE getChecksum() {
        return checksum;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public String getPath() {
        return path;
    }
    
    @Override
    public String toString() {
        return "IngestableFile{" +
                "FileID='" + FileID + '\'' +
                ", url=" + localUrl +
                ", checksum=" + checksum +
                ", fileSize=" + fileSize +
                ", path=" + path +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        IngestableFile other = (IngestableFile) obj;
        if (FileID == null) {
            if (other.FileID != null)
                return false;
        } else if (!FileID.equals(other.FileID))
            return false;
        if (checksum == null) {
            if (other.checksum != null)
                return false;
        } else if (!checksum.equals(other.checksum))
            return false;
        if (fileSize == null) {
            if (other.fileSize != null)
                return false;
        } else if (!fileSize.equals(other.fileSize))
            return false;
        if (path == null) {
            if (other.path != null)
                return false;
        } else if (!path.equals(other.path))
            return false;
        if (localUrl == null) {
            if (other.localUrl != null)
                return false;
        } else if (!localUrl.equals(other.localUrl))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((FileID == null) ? 0 : FileID.hashCode());
        result = prime * result
                + ((checksum == null) ? 0 : checksum.hashCode());
        result = prime * result
                + ((fileSize == null) ? 0 : fileSize.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        result = prime * result + ((localUrl == null) ? 0 : localUrl.hashCode());
        return result;
    }
}
