/**
 * 
 */
package com.ibm.sre.data;


/**
 * @author Dale Nilsson
 *
 */
public class CatalogData {

    /**
     * 
     */
    public CatalogData() {
    }
    private String filename = "";
    public String getFilename() {
        return filename;
    }
    public void setFilename(String filename) {
        this.filename = filename;
    }
    // holds the path to the Constants file
    private String path = "";
    private int shortDesc = 0;
    private int totalDesc = 0;
    public String getConstfile() {
        return constfile;
    }
    public void setConstfile(String constfile) {
        this.constfile = constfile;
    }
    public String getMessageID() {
        return messageID;
    }
    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }
    public String getMessageName() {
        return messageName;
    }
    public void setMessageName(String messageName) {
        this.messageName = messageName;
    }
    private String constfile = "";
    private String messageID = "";
    private String messageName = "";
    public int getTotalDesc() {
        return totalDesc;
    }
    public void incTotalDesc() {
        this.totalDesc++;
    }
    public int getShortDesc() {
        return shortDesc;
    }
    public void incShortDesc() {
        this.shortDesc++;
    }
    /**
     * @return the path with the Constants file
     */
    public String getPath() {
        return path;
    }
    /**
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }
}
