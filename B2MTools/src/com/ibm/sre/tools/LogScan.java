package com.ibm.sre.tools;

/**
 * @author Dale Nilsson
 * Utility to scan Java and Node source code for logging messages
 * Pass the source code path for scanning as an arg 
 * output goes to the console and to a file named 'component'.log
 * 
 * There are many options and text in the file messages.properties
 * 
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.sre.data.AppInfo;
import com.ibm.sre.data.FileInfo;
import com.ibm.sre.data.Messages;
import com.ibm.sre.data.ScanConstants;

public class LogScan implements ScanConstants {
    private static final Logger logger = LoggerFactory.getLogger(LogScan.class);
    // holds a list of files with complete path
    private static ArrayList<String> scanlist = new ArrayList<String>();
    // file ignore list
    private static ArrayList<String> ignorefiles = new ArrayList<String>();
    // folder ignore list
    private static ArrayList<String> ignorefolders = new ArrayList<String>();
    // store the error logger message data used in the app
    private static ArrayList<String> messageList = new ArrayList<String>();
    // output file for scan results
    private static PrintWriter scan_output;
    // variable from properties file 
    private static boolean hasFileOutput = false;
    // Handle mac/linux '/' vs pc '\\' path delimiter
    // The value is loaded from the properties file
    private static String delim = "\\";
    // default constructor
    public LogScan() {
    }

    // get all files and store in an ArrayList
    private static void loadFiles(String path, AppInfo appinfo) throws Exception {
	File folder = new File(path);
	// check if the path is valid
	if (folder.isDirectory()) {
	    File[] files = folder.listFiles();
	    // load the all file names from all sub folders into an ArrayList
	    for (File file : files) {
		if (file.isFile()) {
		    // test for package.json for a Node project
		    if (file.getName().equalsIgnoreCase("package.json")) { 
			appinfo.setNode(true);
		    }
		    if (file.getName().equalsIgnoreCase("pom.xml")) { 
			appinfo.setPom(true);
		    }
		    // load js and java file names
		    if (file.getName().endsWith(".js") || file.getName().endsWith(".java")) {  
			// filter support files and libraries loaded from the properties file
			if (!ignorefiles.contains(file.getName())) {
			    scanlist.add(file.getAbsolutePath());
			}
		    }
		    // load subdirectories and exclude subdirectories from the igonore list
		} else if (file.isDirectory() && !ignorefolders.contains(file.getName())) {		    
		    logger.debug("Folder load: " + file.getName());
		    loadFiles(file.getAbsolutePath(), appinfo);
		}
	    }
	} else {
	    // handle bad path passed for scanning
	    throw new Exception(Messages.getString("LogScan.badpath") + folder); 
	}
    }
    
    // load ignore list from properties file
    private static void loadIgnores() {
	try {
	    int count = 1;
	    // load file filters from properties file
	    // if there is no value in the properties file, it starts with a "!"
	    while (!Messages.getString("LogScan.ignore" + count).startsWith("!")) {
	        ignorefiles.add(Messages.getString("LogScan.ignore" + count));
	        count++;
	    }
	    // load directory filters from properties file
	    // if there is no value in the properties file, it starts with a "!"
	    count = 1;
	    while (!Messages.getString("LogScan.filter"+count).startsWith("!")) { 
	        ignorefolders.add(Messages.getString("LogScan.filter"+count));
	        count++;
	    }
	    logger.debug(ScanConstants.Message004, ScanConstants.SCAN004);
	} catch (Exception e) {
	    logger.error(ScanConstants.Message007, ScanConstants.CGEN001);
	    throw e;
	}
    }
    
    // writes to both an output file and the console
    private static void printOutput(String out) {
	// only print to file if set in properties
	if (hasFileOutput) { 
	    scan_output.println(out);
	}
	// display the message in the console
	System.out.println(out);
    }

    // scan the pom.xml for the logger reference
    private static void scanPom(AppInfo appinfo) throws Exception {
	try {
	    // set the path for the pom.xml file
	    File pomfile = new File(appinfo.getStartpath() + delim + "pom.xml");
	    // open the pom.xml to read
	    BufferedReader reader = new BufferedReader(new FileReader(pomfile));
	    boolean noLogger = true;
	    // read the first line
	    String line = reader.readLine();
	    printOutput(Messages.getString("LogScan.processPom"));
	    while (line != null) {
		// test for the logger reference
		if (line.contains(Messages.getString("LogScan.javaLogger"))) { 
		    printOutput(Messages.getString("LogScan.loggerFound") + line.trim()); 
		    noLogger = false;
		    line = null;
		} else {
		// read the next line
		    line = reader.readLine();
		}
	    }
	    // print if no Java logger reference found
	    if (noLogger) {
		printOutput(Messages.getString("LogScan.nologger")); 
	    }
	    // close the file reader
	    reader.close();
	} catch (FileNotFoundException e){
	    // handle missing pom
	    logger.error(ScanConstants.Message005, ScanConstants.SCAN005, e); 
	    throw e;
	} catch (Exception e) {
	    // handle bad pom file
	    logger.error(ScanConstants.Message002, ScanConstants.SCAN002, e);
	    throw e;
	}
    }

    // scan node package.json file for the kplogger file
    private static void scanJS(AppInfo appinfo) throws Exception {
	try {
	    printOutput(Messages.getString("LogScan.scanJS")); 
	    boolean noJSlogger = true;
	    BufferedReader reader = new BufferedReader(new FileReader(appinfo.getStartpath() + delim + "package.json")); 
	    // read the first line, then 
	    String line = reader.readLine();
	    while (line != null) {
		// search for the logger reference
		if (line.contains(Messages.getString("LogScan.JSlogger").trim())) {
		    printOutput(Messages.getString("LogScan.loggerFound") + line.trim());
		    noJSlogger = false;
		    line = null;
		} else {
		    // read the next line
		    line = reader.readLine();
		}
	    }
	    if (noJSlogger) {
	        printOutput(Messages.getString("LogScan.nologger")); 
	    }
	    reader.close();
	} catch (Exception e) {
	    logger.error(ScanConstants.Message006, ScanConstants.SCAN006, e);
	    throw e;
	}
    }
    
    // store the message in an ArrayList
    private static void storeLogMesage(String line) {
	// mark the start of the message ID
	int startID = line.indexOf(".fmt(") + 5;
	// test if the log message has an ID, the marker for v3+ 
	if (line.indexOf("ID,") > 1) {
	    logger.debug("*** Log message=" + line.substring(startID).replace(";", ""));
	    messageList.add(line.substring(startID).replace(";", "")); 
	}
    }
    // Used to display a log message in the code
    // returns the log message, used for logs that span multiple lines
    private static String printMessage(AppInfo appinfo, FileInfo fileinfo, String line, BufferedReader reader) throws IOException {
	// create a string to hold the log message, needed for muli-line messages
	String storeLog = line;
	printOutput(Messages.getString("LogScan.message20") + fileinfo.getLinenum() + ": " + line.trim());  
	// handle log messages that have multiple lines, search for line ender ;
	while (!line.contains(Messages.getString("LogScan.semicolon")) && appinfo.isPom()) {  
	    // read next line
	    line = reader.readLine().trim();
	    fileinfo.incLinenum();
	    storeLog = storeLog + line;
	    printOutput("      line " + fileinfo.getLinenum() + ": " + line);
	}
	return storeLog;
    }
    // print the scan totals for a file
    private static void printFileTotals(FileInfo fileinfo) {
	System.out.print(Messages.getString("LogScan.message5"));
	System.out.print(Messages.getString("LogScan.message6") + fileinfo.getSumFatal());
	System.out.print(Messages.getString("LogScan.message7") + fileinfo.getSumError());
	System.out.print(Messages.getString("LogScan.message8") + fileinfo.getSumWarn());
	System.out.print(Messages.getString("LogScan.message9") + fileinfo.getSumInfo());
	System.out.print(Messages.getString("LogScan.message10") + fileinfo.getSumDebug());
	System.out.println(Messages.getString("LogScan.message10a") + fileinfo.getSumCatches());
	// only print to file if set in properties
	if (hasFileOutput) { 
	    scan_output.print(Messages.getString("LogScan.message5"));
	    scan_output.print(Messages.getString("LogScan.message6") + fileinfo.getSumFatal());
	    scan_output.print(Messages.getString("LogScan.message7") + fileinfo.getSumError());
	    scan_output.print(Messages.getString("LogScan.message8") + fileinfo.getSumWarn());
	    scan_output.print(Messages.getString("LogScan.message9") + fileinfo.getSumInfo());
	    scan_output.print(Messages.getString("LogScan.message10") + fileinfo.getSumDebug());
	    scan_output.println(Messages.getString("LogScan.message10a") + fileinfo.getSumCatches());
	}
    }
    // print the scan totals for the application
    private static void printTotals(AppInfo appinfo) {
	printOutput(Messages.getString("LogScan.message11") + appinfo.getCompname());
	printOutput(Messages.getString("LogScan.message12") + scanlist.size());
	printOutput(Messages.getString("LogScan.filesSkipped") + appinfo.getSum_files_skipped());
	int files_needing_logs = scanlist.size() - appinfo.getSum_files_skipped();
	printOutput(Messages.getString("LogScan.needLogging") + files_needing_logs);
	printOutput(Messages.getString("LogScan.message13") + appinfo.getSumFatal());
	printOutput(Messages.getString("LogScan.message14") + appinfo.getSumError());
	printOutput(Messages.getString("LogScan.message15") + appinfo.getSumWarn());
	printOutput(Messages.getString("LogScan.message16") + appinfo.getSumInfo());
	printOutput(Messages.getString("LogScan.message17") + appinfo.getSumDebug());
	printOutput(Messages.getString("LogScan.message18") + appinfo.getSumCatches());
	printOutput(Messages.getString("LogScan.message-nologging") + appinfo.getSum_no_logging());
	float noLogsRating = ((new Float(appinfo.getSum_no_logging()).floatValue())) / (new Float(files_needing_logs));
	printOutput(Messages.getString("LogScan.message-percentnologging") + String.format("%.0f", noLogsRating * 100F)
		+ "%");
	// show files with no logging
	if (appinfo.getSum_no_logging() > .10F) {
	    printOutput(" - consider adding logging to these files");
	}
	// calc files with higher than debug logging
	int logs_higher_than_debug = files_needing_logs - appinfo.getSum_no_logging() - appinfo.getSum_debug_only();
	printOutput("Total files with higher than debug logging: " + logs_higher_than_debug);
	//
	float infoRating = ((new Float(logs_higher_than_debug).floatValue())) / (new Float(files_needing_logs));
	printOutput("Percentage of files with higher than debug logging: " + String.format("%.0f", infoRating * 100F)
		+ "%");
	//
	if (infoRating < .90F) {
	    printOutput(" - consider adding logging higher than debug to the other files");
	}
	//
	printOutput(Messages.getString("LogScan.message-debugonly") + appinfo.getSum_debug_only());
	if (appinfo.getSum_debug_only() > 0) {
	    printOutput(" - consider changing log.debug to log.info or higher in these files");
	}
	if (appinfo.getSum_files_catch_nologs() > 0) {
	    printOutput("Total files with a catch() and no logging: " + appinfo.getSum_files_catch_nologs());
	    printOutput(" - consider adding log messages to these files");
	}
    }
    
    /**
     * @param args 
     *            [0] pass the start path for the scan
     */
    public static void main(String[] args) {
	FileWriter fileWriter = null;
	// bean that holds Application data
	AppInfo appinfo = new AppInfo();
	try {
	    // check for missing arg[0] with scan path
	    if (args.length == 0) {
		throw new Exception(Messages.getString("LogScan.MissingScanArg"));  
	    }
	    appinfo.setStartpath(args[0]);
	    // read the path delimiter, different in PC and Mac
	    delim = Messages.getString("LogScan.delimiter");
	    // get application name from passed arg
	    // the last folder is used as the component name
	    appinfo.setCompname(appinfo.getStartpath().substring(appinfo.getStartpath().lastIndexOf(delim) + 1, appinfo.getStartpath().length())); 
	    hasFileOutput = Boolean.parseBoolean(Messages.getString("LogScan.output2file"));
	    if (hasFileOutput) {
		// setup the output file
		String outputFile = Messages.getString("LogScan.outputPath") + appinfo.getCompname() + ".log";
		fileWriter = new FileWriter(outputFile); 
		scan_output = new PrintWriter(fileWriter);
		scan_output.println(Messages.getString("LogScan.start") + appinfo.getCompname());
		System.out.println(Messages.getString("LogScan.message2") + outputFile);
	    } 
	    logger.info(Messages.getString("LogScan.startScan"), appinfo.getCompname()); 
	    // load ignore files and folders
	    loadIgnores();
	    // get a list of files to scan
	    loadFiles(appinfo.getStartpath(), appinfo);
	    // check the logger version used in the project package.json
	    if (appinfo.isNode()) { 
		scanJS(appinfo);
	    } 
	    // process the POM.xml file usually a Java project
	    if (appinfo.isPom()) { 
		scanPom(appinfo);
	    }
	    // scan all files for logging messages and catches
	    printOutput(Messages.getString("LogScan.startScanning") + appinfo.getStartpath()); 
	    for (int filenum = 0; filenum < scanlist.size(); filenum++) {
		String scanfile = scanlist.get(filenum);
		BufferedReader reader = new BufferedReader(new FileReader(scanfile));
		printOutput(Messages.getString("LogScan.scanning") + scanfile.substring(scanfile.lastIndexOf(delim) + 1, scanfile.length()));
		// read a line from the file
		String line = reader.readLine();	
		// bean to hold file data
		FileInfo fileinfo = new FileInfo();
		// filter out files with Constants files
		if (scanfile.contains("Constants")) {
		    fileinfo.setSkipMessage(Messages.getString("LogScan.contantsFile"));
		    fileinfo.setLogsNotNeeded(true);
		    fileinfo.setNoLogs(false);
		    appinfo.incSum_files_skipped();
		} else {
		    // process a file, check for end of file
		    while (line != null) {
			// count log.fatal
			if (line.contains(appinfo.getLogFatal())) {
			    fileinfo.incSumFatal();
			    appinfo.incSumFatal();
			    fileinfo.setNoLogs(false);
			    // print the message text
			    fileinfo.setFullMessage(printMessage(appinfo, fileinfo, line, reader));
			}
    		    	// count log.error
    		    	if (line.contains(appinfo.getLogError())) {
    		    	    fileinfo.incSumError();
    		    	    appinfo.incSumError();
    		    	    // mark that log.debug is used
    		    	    fileinfo.setHasDebug(true);
    		    	    // print the message text
    		    	    fileinfo.setFullMessage(printMessage(appinfo, fileinfo, line, reader));
    		    	    // save the message for the error catalog
    		    	    storeLogMesage(fileinfo.getFullMessage());
    		    	}
    		    	// count log.warn
    		    	if (line.contains(appinfo.getLogWarn())) {
    		    	    fileinfo.incSumWarn();
    		    	    appinfo.incSumWarn();
    		    	    fileinfo.setNoLogs(false);
    		    	    // print the message text
    		    	    fileinfo.setFullMessage(printMessage(appinfo, fileinfo, line, reader));
    		    	    
    		    	}
    		    	// count log.info
   		    	if (line.contains(appinfo.getLogInfo())) {
    		    	    fileinfo.incSumInfo();
    		    	    appinfo.incSumInfo();
    		    	    fileinfo.setNoLogs(false);
    		    	    // print the message text
    		    	    fileinfo.setFullMessage(printMessage(appinfo, fileinfo, line, reader));
    		    	}
    		    	// count log.debug calls 
    		    	if (line.contains(appinfo.getLogDebug())) {
    		    	    fileinfo.incSumDebug();
    		    	    appinfo.incSumDebug();
    		    	    fileinfo.setNoLogs(false);
    		    	    fileinfo.setHasDebug(true);
    		    	}
    		    	// count catch() found
    		    	if (line.contains(appinfo.getLogCatch())) {
    		    	    fileinfo.incSumCatches();
    		    	    appinfo.incSumCatches();
    		    	    fileinfo.setHasCatch(true);
    		    	    // print the message text
    		    	    printOutput(Messages.getString("LogScan.message21") + fileinfo.getLinenum() + ": " + line.trim());  
    		    	}
    		    	// search for Java interface and exclude from scanning
    		    	if (line.contains(Messages.getString("LogScan.interface"))) { 
    		    	    fileinfo.setNoLogs(false);
    		    	    fileinfo.setLogsNotNeeded(true);
    		    	    appinfo.incSum_files_skipped();
    		    	    fileinfo.setSkipMessage(Messages.getString("LogScan.interfaceFound"));
    		    	    // set line to null to stop reading the file
    		    	    line = null;
    		    	} else {
    		    	    // read the next line
    		    	    line = reader.readLine();
    		    	    fileinfo.incLinenum();
    		    	}
		    } 
		}
		// add files that have only log.debug
		if (fileinfo.isNoLogs() && fileinfo.hasDebug()){
		    appinfo.incSum_debug_only();
		}
		// add files that have catch() with no logging
		if (fileinfo.isNoLogs() && fileinfo.hasCatch()){
		    appinfo.incSum_files_catch_nologs();
		}
		// Display file scanning results for the current file
		if (fileinfo.isNoLogs() && !fileinfo.hasDebug()) {
		    printOutput(Messages.getString("LogScan.noLoggingFound"));
		    appinfo.incSum_no_logging();
		} else if (fileinfo.isLogsNotNeeded()){
		    printOutput(fileinfo.getSkipMessage());
	    	} else {
		    // list of logging totals found in the file
	    	    printFileTotals(fileinfo);
	    	}
		reader.close();

	    }
	    logger.info(ScanConstants.Message003, ScanConstants.SCAN003);
	    // print the totals from the scan
	    printTotals(appinfo);  
	    
	    // Now generate the error catalog
	    if (messageList.size() > 0) {
		printOutput(Messages.getString("LogScan.messageIDs"));
		printOutput(Messages.getString("LogScan.catalog-gen") + appinfo.getCompname() + "\n");
		CatalogGenerator catgen = new CatalogGenerator();
		catgen.generate(messageList, appinfo.getStartpath(), hasFileOutput);
	    } else {
		printOutput(Messages.getString("LogScan.no-logging-ids"));
	    }
	    
	    // if writing to a file, close it
	    if (hasFileOutput) {
		fileWriter.close();
	    }
	} catch (Exception e) {
	    logger.error(ScanConstants.Message001, ScanConstants.SCAN001, e);
	}
	
    } 
}
