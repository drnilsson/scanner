/**
 * 
 */
package com.ibm.sre.data;

/**
 * @author Dale Nilsson
 *
 *  class for holding file scanning data
 */
public class FileInfo {
    private int sumFatal = 0;
    private int sumError = 0;
    private int sumDebug = 0;
    private int sumInfo = 0;
    private int sumWarn = 0;
    private int sumCatches = 0;
    private String skipMessage = "";
    private int linenum = 1;
    private String fullMessage = "";
    private boolean hasCatch = false;

    public boolean hasCatch() {
        return hasCatch;
    }

    public void setHasCatch(boolean hasCatch) {
        this.hasCatch = hasCatch;
    }

    public String getFullMessage() {
        return fullMessage;
    }

    public void setFullMessage(String fullMessage) {
        this.fullMessage = fullMessage;
    }

    public boolean isNoLogs() {
        return noLogs;
    }

    public boolean isLogsNotNeeded() {
        return logsNotNeeded;
    }

    public boolean hasDebug() {
        return hasDebug;
    }

    private boolean noLogs = true;
    private boolean logsNotNeeded = false;
    private boolean hasDebug = false;

    public void setNoLogs(boolean noLogs) {
        this.noLogs = noLogs;
    }

    public void setLogsNotNeeded(boolean logsNotNeeded) {
        this.logsNotNeeded = logsNotNeeded;
    }

    public void setHasDebug(boolean hasDebug) {
        this.hasDebug = hasDebug;
    }

    public int getLinenum() {
        return linenum;
    }

    public void incLinenum() {
        this.linenum++;
    }

    public String getSkipMessage() {
	return skipMessage;
    }

    public void setSkipMessage(String skipMessage) {
	this.skipMessage = skipMessage;
    }

    public int getSumFatal() {
	return sumFatal;
    }

    public void incSumFatal() {
	this.sumFatal++;
    }

    public int getSumError() {
	return sumError;
    }

    public void incSumError() {
	this.sumError++;
    }

    public int getSumDebug() {
	return sumDebug;
    }

    public void incSumDebug() {
	this.sumDebug++;
    }

    public int getSumInfo() {
	return sumInfo;
    }

    public void incSumInfo() {
	this.sumInfo++;
    }

    public int getSumWarn() {
	return sumWarn;
    }

    public void incSumWarn() {
	this.sumWarn++;
    }

    public int getSumCatches() {
	return sumCatches;
    }

    public void incSumCatches() {
	this.sumCatches++;
    }

    public FileInfo() {

    }

}
