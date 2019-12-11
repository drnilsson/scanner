/**
 * 
 */
package com.ibm.sre.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.sre.data.CatalogData;
import com.ibm.sre.data.Messages;
import com.ibm.sre.data.ScanConstants;

/**
 * @author Dale Nilsson
 * 
 * This class generates a comma delimited error catalog.
 * The input come from an ArrayList of error messages generated 
 * from the LogScan utility. It uses the CatalogData class to hold data
 *
 */
public class CatalogGenerator {
    private static final Logger logger = LoggerFactory.getLogger(CatalogGenerator.class);
    // holds the path to the Constants file, needed because the findConstantsPath is recursive
    private String path = "";
    // output file for error catalog
    private FileWriter errorcatWriter;
    
    private String getMessageID() {
        return messageID;
    }
    private void setMessageID(String messageID) {
        this.messageID = messageID;
    }
    private String getMessageName() {
        return messageName;
    }
    private void setMessageName(String messageName) {
        this.messageName = messageName;
    }
    
    private String messageID = "";
    private String messageName = "";
 
    /**
     * @return the path with the Constants file
     */
    private String getConstPath() {
        return path;
    }
    /**
     * @param path the path to set
     */
    private void setConstPath(String path) {
        this.path = path;
    }
    // used to store the message IDs used in the code
    private Map<String, String> idMap = new HashMap<String, String>();
    // used to store the error catalog data
    private ArrayList<String> errorCat = new ArrayList<String>();
  
    public CatalogGenerator() {
    }
    // searches the path for the Constants file 
    private void findConstantsPath(String constfilename, String startPath) {
	
	try {
	    File folder = new File(startPath);
	    File[] files = folder.listFiles();
	    if (getConstPath().length() == 0) {
		// loop thru all the files looking for the Constants file
		for (File file : files) {
		    if (file.isFile() && file.getName().equalsIgnoreCase(constfilename)) {
			setConstPath(file.getAbsolutePath());
		    } else if (file.isDirectory()) {
			findConstantsPath(constfilename, file.getAbsolutePath());
		    }
		}
	    }
	} catch (Exception e) {
	    logger.error(ScanConstants.Message009, ScanConstants.CGEN003, e);
	    throw e;
	}

    }

    private void replaceIDs(ArrayList<String> errorList, CatalogData catData) throws Exception {
	try {
	    // iterate through the error messages and lookup values in Constants file
	    for (int numOfMessages = 0; numOfMessages < errorList.size(); numOfMessages++) {
		String line = errorList.get(numOfMessages);
		// split the comma separated values in the message list
		String[] errValues = line.split(",");
		// pull the 3 error values and clean up for parsing
		catData.setConstfile(errValues[0].trim().substring(0, line.indexOf(".")));
		setMessageID(errValues[0].trim().substring(line.indexOf(".") + 1));
		setMessageName(errValues[1].trim().substring(line.indexOf(".") + 1));
		// some messages use String.format, so pull it out
		String fullmessageDesc = errValues[2].trim().replace("String.format(", "");
		// var for the message description key
		String messageDesc = "";
		String descValue = "";
		// check if the message description uses the Constants file
		if (!fullmessageDesc.contains(catData.getConstfile())) {
		    descValue = fullmessageDesc;
		    logger.warn("Message description '" + fullmessageDesc + "' not in " + catData.getConstfile());
		} else {
		    // handle messages that are in the Constants file
		    fullmessageDesc = fullmessageDesc.substring(line.indexOf(".") + 1);

		    // handle error descriptions that have additional data
		    if (fullmessageDesc.contains(" ")) {
			messageDesc = fullmessageDesc.substring(0, fullmessageDesc.indexOf(" "));
		    } else {
			// remove trailing parens on the log message
			messageDesc = fullmessageDesc.replace(")", "");
			messageDesc = messageDesc.replace(".", "");
		    }

		    // display the error catalog values,
		    // there is a separate method that writes a CSV
		    descValue = idMap.get(messageDesc);
		}
		System.out.println(Messages.getString("CatGen.message-from-code") + line);
		if (descValue != null) {
		    
		    System.out.println(Messages.getString("CatGen.item") + (numOfMessages + 1) + ", "
			    + idMap.get(getMessageID()) + ", " + idMap.get(getMessageName()) + ", " + descValue);
		    if (descValue.split(" ").length < 4) {
			logger.info(">>> Detected short error description, consider adding more description");
			catData.incShortDesc();
		    }
		    if (descValue.contains("\\")) {
			logger.info(">>> Check use of formatting characters in the message.");
		    }
		} else {
		    throw new Exception("Error processing constants values, no value found");
		}
		// add the mapped log message values to the error catalog list
		errorCat.add(idMap.get(getMessageID()) + ", " + idMap.get(getMessageName()) + ", " + descValue);
		catData.incTotalDesc();
	    }
	} catch (Exception e) {
	    logger.error("Exception replacing message IDs with Constants values", e);
	    throw e;
	}
    }
    // writes the error catalog to a file
    private void write2file() throws Exception {
	try {
	    String errorcatname = Messages.getString("LogScan.outputPath") + Messages.getString("CatGen.filename");
	    errorcatWriter = new FileWriter(errorcatname);
	    PrintWriter errorcat_output = new PrintWriter(errorcatWriter);
	    for (int i = 0; i < errorCat.size(); i++) {
		errorcat_output.println(errorCat.get(i));
	    }
	} catch (Exception e) {
	    logger.error(Messages.getString("CatGen.write-error"), e);
	    throw e;   
	}
	errorcatWriter.close();
    }
    
    public void generate(ArrayList<String> errorMessages, String startpath, boolean output2file) throws Exception {
	try {
	    CatalogData catData = new CatalogData();
	    
	    // pull the Constants filename from the error message data
	    catData.setFilename(errorMessages.get(0).substring(0, errorMessages.get(0).indexOf(".")) + ".java");
	    // search for the Constants file and save the full path
	    findConstantsPath(catData.getFilename(), startpath);
	    
	    if (getConstPath().length() > 0) {
		// read constant file items into an arraylist
		File constfile = new File(getConstPath());
		BufferedReader reader = new BufferedReader(new FileReader(constfile));
		String line = reader.readLine();
		// Load Constants data into Hash Map
		while (line != null) {
		    // trim spaces from name=value
		    line = line.replaceAll(" =", "=");
		    line = line.replaceAll(" =", "=");
		    line = line.replaceAll("= ", "=");
		    line = line.replaceAll("= ", "=");
		    // process the each constant value
		    if (line.contains("public static")) {
			// locate the constant variable
			int startID = line.indexOf("final String") + 13;
			int endID = line.indexOf("=");
			// pull the message ID from the 
			String id = line.substring(startID, endID);
			logger.debug("ID start=" + startID + " end=" + endID);
			int startValue = line.indexOf("\"") + 1;
			int endValue = line.indexOf("\"", startValue);
			// store the id/value in the hash map
			idMap.put(id, line.substring(startValue, endValue));
		    }
		    // read the next line
		    line = reader.readLine();
		}
		reader.close();
		// look up values in Constants file
		replaceIDs(errorMessages, catData);
		// write error catalog to file
		if (output2file) {
		    write2file();
		}
		logger.info(Messages.getString("CatGen.done"), Messages.getString("CatGen.filename"));
		System.out.println(Messages.getString("CatGen.num-error-desc") + catData.getTotalDesc());
		if (catData.getShortDesc() > 0){
		    System.out.println(Messages.getString("CatGen.short-descriptions") + catData.getShortDesc());
		    float shortPercent = ((new Float(catData.getShortDesc()).floatValue())) / (new Float(catData.getTotalDesc()).floatValue());
		    System.out.println(Messages.getString("CatGen.percent-improved") + String.format("%.0f", shortPercent * 100F) + "%");
		}
	    } else {
		throw new Exception(ScanConstants.Message008);
	    }

	} catch (Exception e) {
	    logger.error(ScanConstants.Message008, ScanConstants.CGEN002, e);
	    throw e;
	}
    }
}
