package dk.statsbiblioteket.newspaper.promptdomsingester.util;

import java.util.List;

/**
 * Class encapsulating a request to add relations to a fedora repository.
 */
public class AddRelationsRequest {

    private String pid;
    private String subject;
    private String predicate;
    private List<String> objects;
    private String comment;

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getPredicate() {
        return predicate;
    }

    public void setPredicate(String predicate) {
        this.predicate = predicate;
    }

    public List<String> getObjects() {
        return objects;
    }

    public void setObjects(List<String> objects) {
        this.objects = objects;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
