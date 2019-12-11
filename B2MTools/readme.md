Overview:
	This utility is intended to make it easier to scan a project source code for 
	logging usage to make it easier to identify gaps, identify inconsistencies, and
	improve coverage. It also automatically generates the input for the error catalog.
	It and scans a set of Java and JS files for logging usage and 	the output goes to 
	the console and optionally to a file. Using the CatalogGenerator class, an error catalog with all the error messages
	is generated in a csv file.
	
	) The path to start the scan is passed as a parameter to the application.
		- The scanner loads a list of all the .java and .js files for scanning
		- There is a list of folders to ignore, like 'test', that can be extended in 
		  the messages properties file.
		- There is a list of file to ignore, like test, that can be extended in 
		  the messages properties file.
		  
	) For Java, it scans the pom.xml for the logger dependency (from the properties file)
	For Node, it scans the package.json for the logger dependency (from the properties file)
	The logger dependency is listed in the output
	
	) The scanner searches each file in the list:
		- Java interfaces are ignored because they should not have logging
		- Files name *Constants are ignored
		- All .fatal, .error, .warn, .info, and .debug log messages are counted.
		- All 'catch' lines are displayed, catches should have a log message
		
	) A summary of the scan results prints in the console and optionally to a file.
		- The total for each logging type is listed
		- The total number of catches in the code
		- The total number of files skipped (Interfaces and Constants)
		- The total number of files that may need logging
		- The total and percentage of files with no logging
		- The total files with only log.debug
		- The percentage of files that have info logging

	) The Error messages are stored in an ArrayList and processed by the CatGenerator.
	The error constants files is scanned and the values for the ID, name, and description
	are matched with those in the ArrayList. The Error Catalog is output as a CSV file.
	
Note: Mac and Linux users need to edit the properties:
	LogScan.delimiter and change to /
	LogScan.outputPath and change to a local path for the output file

Todo:
	) node project has not been tested for Catalog Generation
	) summary list of files with no logging details
	) tune .info percent so it counts 1 info per file
	) handle old kplogger format in pom.xml with <version>
	) test for embedded strings in logging
	) look at Summary only option for reports
	) look at *Filename as a file filter
	) look at *name folder as a filter
	) make junit tests
	) make sample app for demo / testing
	
Change Log:
