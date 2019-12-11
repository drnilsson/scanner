/**
 * 
 */
package com.ibm.sre.data;


/**
 * @author Dale Nilsson
 *
 */
public class AppInfo {

    /**
     * Class to hold application data for scanning application logging
     */
    public AppInfo() {
    }

    private String log_fatal = Messages.getString("LogScan.fatal");
    private String log_error = Messages.getString("LogScan.error");
    private String log_warn = Messages.getString("LogScan.warn");
    private String log_info = Messages.getString("LogScan.info");
    private String log_debug = Messages.getString("LogScan.debug");
    private String log_catch = Messages.getString("LogScan.catch");
    private int sum_fatal = 0;
    private int sum_error = 0;
    private int sum_debug = 0;
    private int sum_info = 0;
    private int sum_warn = 0;
    private int sum_catches = 0;
    private int sum_no_logging = 0;
    private int sum_debug_only = 0;
    private int sum_files_skipped = 0;
    private int sum_files_catch_nologs = 0;
    public int getSum_files_catch_nologs() {
        return sum_files_catch_nologs;
    }

    public void incSum_files_catch_nologs() {
        this.sum_files_catch_nologs++;
    }

    private boolean pom = false;
    private boolean node = false;
    private String compname = "";
    private String startpath = "";

    public String getStartpath() {
        return startpath;
    }

    public void setStartpath(String startpath) {
        this.startpath = startpath;
    }

    public String getCompname() {
        return compname;
    }

    public void setCompname(String compname) {
        this.compname = compname;
    }

    public boolean isNode() {
        return node;
    }

    public void setNode(boolean node) {
        this.node = node;
    }

    public boolean isPom() {
        return pom;
    }

    public void setPom(boolean pom) {
        this.pom = pom;
    }

    public String getLogFatal() {
	return log_fatal;
    }

    public String getLogError() {
	return log_error;
    }

    public String getLogWarn() {
	return log_warn;
    }

    public String getLogInfo() {
	return log_info;
    }

    public String getLogDebug() {
	return log_debug;
    }

    public String getLogCatch() {
	return log_catch;
    }

    public int getSumFatal() {
	return sum_fatal;
    }

    public void incSumFatal() {
	this.sum_fatal++;
    }

    public int getSumError() {
	return sum_error;
    }

    public void incSumError() {
	this.sum_error++;
    }

    public int getSumDebug() {
	return sum_debug;
    }

    public void incSumDebug() {
	this.sum_debug++;
    }

    public int getSumInfo() {
	return sum_info;
    }

    public void incSumInfo() {
	this.sum_info++;
    }

    public int getSumWarn() {
	return sum_warn;
    }

    public void incSumWarn() {
	this.sum_warn++;
    }

    public int getSumCatches() {
	return sum_catches;
    }

    public void incSumCatches() {
	this.sum_catches++;
    }

    public int getSum_no_logging() {
	return sum_no_logging;
    }

    public void incSum_no_logging() {
	this.sum_no_logging++;
    }

    public int getSum_debug_only() {
	return sum_debug_only;
    }

    public void incSum_debug_only() {
	this.sum_debug_only++;
    }

    public int getSum_files_skipped() {
	return sum_files_skipped;
    }

    public void incSum_files_skipped() {
	this.sum_files_skipped++;
    }
}
