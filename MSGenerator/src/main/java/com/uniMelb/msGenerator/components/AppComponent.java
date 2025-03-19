package com.uniMelb.msGenerator.components;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.uniMelb.msGenerator.entities.Event;
import com.uniMelb.msGenerator.entities.PatternMeta;
import com.uniMelb.msGenerator.patternMinningAlgorithms.ExecuteJarWithParams;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@Component
public class AppComponent {

    public static Map<String, List<Event>> dataMap = new HashMap<>(0); // event data - trace ID and event entity List
	public static Map<String, List<Integer>> methodAverageTimeMap = new HashMap<>(0); // store the average method execution time
    public static int dataListSize = 1;
    public static Map<String, Integer> eventToIDMapping = new HashMap<>(0); // to convert string sequence to int sequenc to optimise performance
    public static List<List<Integer>> numberedEventList = new ArrayList<>(0); // this is to calculate the gap between two method number
    public static int methodID = 1;

    @Value("${app.rootFolders}")
    private String rootFolders;

    @Value("${app.patternMinningMinimumSupport}")
    private String minimumSupport;

    @Value("${app.patternMinningAlgorithm}")
    private String patternMiningAlgorithm;
    
    // @Value("${app.patternMinningMinimumConfidence}")
    // private double minimumConfidence;

    // @Value("${app.patternMinningMinimumGap}")
    // private int minimumGap;

    @Value("${app.patternMinningLibrary}")
    private String patternMinninglibrary;

    @Value("${app.patternMinningInputFile}")
    private String patternMinningInputFile;

    @Value("${app.patternMinningOutputFile}")
    private String patternMinningOutputFile;

    @Value("${app.kiekerLogFileLocation}")
    private String kiekerLogFileLocation;

    @Value("${app.isXESFileRequired}")
    private boolean isXESFileRequired;

    @Value("${app.isMethodToIdMappingRequired}")
    private boolean isMethodToIdMappingRequired;

    @Value("${app.methodToIdMappingFile}")
    private String methodToIdMappingFileLocation;

    @Value("${app.patternCostInfoFile}")
    private String patternCostInfoFileLocation;

    @Value("${app.excelFileLocation}")
    private String excelFileLocation;
    
    // comma seperated string of names to avoid in pattern minning
    @Value("${app.stringsToAvoidFromPatternMatching}")
    private String stringsToAvoidFromPatternMatching;

    // add parameters to xes file output and xes file output required.

    public void initiateIdentification() {
        System.out.println("=========== System Parameters Starts =============");
        System.out.println("app.rootFolders : "+ rootFolders );
        System.out.println("app.kiekerLogFileLocation : "+kiekerLogFileLocation);
        System.out.println("app.patternMinningMinimumSupport : "+ minimumSupport );
        System.out.println("app.patternMiningAlgorithm : "+ patternMiningAlgorithm );
        // System.out.println("app.patternMinningMinimumConfidence : "+ minimumConfidence );
        // System.out.println("app.patternMinningMinimumGap : "+ minimumGap );
        System.out.println("Pattern Minning Library : " + patternMinninglibrary);
        System.out.println("Pattern Minning Input File : "+patternMinningInputFile);
        System.out.println("Pattern Minning Output File : "+patternMinningOutputFile);
        System.out.println("Is XES File Required : "+isXESFileRequired);
        System.out.println("Is Method To Id Mapping Required : "+isMethodToIdMappingRequired);
        System.out.println("Method To Id Mapping File Location : "+methodToIdMappingFileLocation);
        System.out.println("Pattern Cost Info File Location : "+ patternCostInfoFileLocation);
        System.out.println("Pattern Cost Info File Location : "+ excelFileLocation);
        System.out.println("Discarding Class List : "+ stringsToAvoidFromPatternMatching);
        System.out.println("=========== System Parameters Ends =============");

        readLogFile(kiekerLogFileLocation);
    }


    // read the kiker output files and filter the logs
    public void readLogFile(String kiekerLogFileLocation) {
		try {
			List<String> filteredLineList = new ArrayList<String>();
			final File folder = new File(kiekerLogFileLocation);
			for (final File fileEntry : folder.listFiles()) {
			String extension = FilenameUtils.getExtension(fileEntry.getAbsolutePath());
			if(extension.equalsIgnoreCase("DAT")){
				BufferedReader br = new BufferedReader(new FileReader(fileEntry)); 
					for(String line; (line = br.readLine()) != null; ) {
						// process the line. remove kiker specific logs
                        // removed mapper logs due to db access
						if(line.startsWith("$1;") && !line.contains("kieker.monitoring.probe") && !line.contains("Mapper") ){
							filteredLineList.add(line);
						}
					}
				}
			}
			convertLogsToXesFormat(filteredLineList);
            convertToNumberSequence();
            executePatternMinning();
            generateCostInfo();
		} catch (Exception e) {
			System.out.println("Exception in read log file : "+e.getMessage() );
		}

     }

     // convert filtered kieker logs to xes format
    public void convertLogsToXesFormat(List<String> dataList) {

        File file = new File("C:\\Development\\MSGeneratorSupportWork\\Output\\"+"output.xes");
            // file.getParentFile().mkdir();

        try {
            file.createNewFile();
        } catch (IOException e1) {
            e1.printStackTrace();
        }


        try{
            PrintWriter writer = new PrintWriter(file.getAbsolutePath(), "UTF-8");
            // Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
            String xesText ="<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"+"\n"+
            "<log xes.version=\"2.0\" xes.features=\"nested-attributes\" openxes.version=\"1.0RC7\" xmlns=\"http://www.xes-standard.org/\">"+"\n"+
                    "<extension name=\"Lifecycle\" prefix=\"lifecycle\" uri=\"http://www.xes-standard.org/lifecycle.xesext\"/>"+"\n"+
                    "<extension name=\"Organizational\" prefix=\"org\" uri=\"http://www.xes-standard.org/org.xesext\"/>"+"\n"+
                    "<extension name=\"Time\" prefix=\"time\" uri=\"http://www.xes-standard.org/time.xesext\"/>"+"\n"+
                    "<extension name=\"Concept\" prefix=\"concept\" uri=\"http://www.xes-standard.org/concept.xesext\"/>"+"\n"+
                    "<extension name=\"Semantic\" prefix=\"semantic\" uri=\"http://www.xes-standard.org/semantic.xesext\"/>"+"\n"+
                    "<global scope=\"trace\">"+"\n"+
                        "<string key=\"concept:name\" value=\"UNKNOWN\"/>"+"\n"+
                            "<string key=\"traceID\" value=\"UNKNOWN\"/>"+"\n"+
                    "</global>"+"\n"+
                    "<global scope=\"event\">"+"\n"+
                        "<string key=\"concept:name\" value=\"UNKNOWN\"/>"+"\n"+
                        "<string key=\"lifecycle:transition\" value=\"complete\"/>"+"\n"+
                        "<date key=\"time:timestamp\" value=\"2008-12-09T08:20:01.527+01:00\"/>"+"\n"+
                        // "<date key=\"time:timestamp\" value=\""+currentTimestamp+"\"/>"+"\n"+
                            "<string key=\"caseId\" value=\"UNKNOWN\"/>"+"\n"+
                            "<int key=\"duration\" value=\"1\"/>"+"\n"+
                            "<int key=\"callingOrder\" value=\"1\"/>"+"\n"+
                            "<int key=\"depth\" value=\"1\"/>"+"\n"+
                    "</global>"+"\n";
            System.out.println("@@@@@@@@@ Data list size "+ dataList.size());
            // for(int i=0;i<dataList.size();i++){
                for(int i=dataList.size()-1; i>=0; i--){
                // System.out.println("event loop : "+i);
                String eventString = dataList.get(i);
                String[] tokenizedArray = eventString.split(";");
                String timeStamp = tokenizedArray[1];  // in nano seconds
                String sessionId = tokenizedArray[3];
                String traceId = tokenizedArray[4];

                long inTime = Long.parseLong(tokenizedArray[5]);
                long outTime = Long.parseLong(tokenizedArray[6]);
                String kikerNodeId = tokenizedArray[7];
                int callingOrder = Integer.parseInt(tokenizedArray[8]);
                int depthOfCallingStack = Integer.parseInt(tokenizedArray[9]);
                String methodSignature = tokenizedArray[2];
                
                Event event = new Event();
                event.setSequenceId(Long.parseLong(timeStamp));
                event.setSessionId(sessionId);
                event.setTraceId(traceId);
                event.setMethodSignature(methodSignature);
                event.setInTime(inTime);
                // event.setFormattedDate(""+inTime);
                event.setFormattedDate(convertDate(inTime));
                event.setOutTime(outTime);
                event.setCallingOrder(callingOrder);
                event.setDeptOfCallingStack(depthOfCallingStack);
                List<Event> tempList = new ArrayList<>(0);
                // List<Event> miningTempList = new ArrayList<>(0);
                if(dataMap.get(event.getTraceId()) != null) {
                    tempList = dataMap.get(event.getTraceId());
                    // miningTempList = miningDatMap.get(event.getTraceId());
                    // System.out.println("%%%%%%%%%% Element available in temp list");
                }
                
                tempList.add(event);
                Collections.sort(tempList, (o1, o2) -> o1.getCallingOrder() - o2.getCallingOrder());
                dataMap.put(event.getTraceId(), tempList);

                // List<Integer> durationList;  //error in jforum log java.lang.NumberFormatException: For input string: "4807580800"
                // if(methodAverageTimeMap.get(event.shortMethodName()) != null) {
                //     durationList  = methodAverageTimeMap.get(event.shortMethodName());
                // } else {
                //     durationList = new ArrayList<>();
                // }
                // durationList.add(event.getMethodDuration());
                // methodAverageTimeMap.put(event.shortMethodName(), durationList);
                if (eventToIDMapping.get(event.shortMethodName()) == null) {
                    if ( event.shortMethodName().contains("init") 
                    || event.shortMethodName().contains("private")
                    || event.shortMethodName().contains(".lambda") 
                    || event.shortMethodName().contains("getBotAgents") 
                    || event.shortMethodName().contains("WebRequestContext") 
                    || event.shortMethodName().contains("CsrfFilter")
                    || event.shortMethodName().contains("CsrfHttpServletRequestWrapper") 
                    || event.shortMethodName().contains("net.jforum.JForum.")
                    || event.shortMethodName().contains("net.jforum.DBConnection")
                    || event.shortMethodName().contains("net.jforum.entities.") 
                    || event.shortMethodName().contains("net.jforum.util.I18n.") 
                    || event.shortMethodName().contains("net.jforum.context.JForumContext") 
                    || event.shortMethodName().contains("net.jforum.util.legacy.clickstream.")
                    || event.shortMethodName().contains("net.jforum.util.DbUtils") 
                    || event.shortMethodName().contains("net.jforum.JForumExecutionContext")
                    || event.shortMethodName().contains("net.jforum.cache.") 
                    || event.shortMethodName().contains("net.jforum.util.stats.")
                    || event.shortMethodName().contains("net.jforum.repository.SecurityRepository")
                    || event.shortMethodName().contains("net.jforum.repository.ModulesRepository")
                    || event.shortMethodName().contains("net.jforum.SessionFacade.")
                    || event.shortMethodName().contains("net.jforum.security.")
                    || event.shortMethodName().contains("net.jforum.context.web.")
                    || event.shortMethodName().contains("net.jforum.Command.") 
                    || event.shortMethodName().contains("ControllerUtils")
                    || event.shortMethodName().contains("net.jforum.repository.Tpl")
                    || event.shortMethodName().contains("net.jforum.util.bbcode.")
                    || event.shortMethodName().contains("net.jforum.util.Hash")
                    || event.shortMethodName().contains("net.jforum.view.admin.LuceneStatsAction")
                    || event.shortMethodName().contains("net.jforum.dao.generic.GenericDataAccessDriver")
                    || event.shortMethodName().contains("RankingAction")
                    || event.shortMethodName().contains("CacheAction")
                    || event.shortMethodName().contains("Captcha")
                    || event.shortMethodName().contains("DBVersionWorkarounder")
                    || event.shortMethodName().contains("PostAction")
                    || event.shortMethodName().contains("HikariPooledConnection")
                    || event.shortMethodName().contains("BoundedLinkedHashMap")
                    || event.shortMethodName().contains("DataAccessDriver")
                    || event.shortMethodName().contains(".forum.ForumAction.list")
                    || event.shortMethodName().contains(".UrlPattern.")
                    // || event.shortMethodName().contains(".SystemGlobals.")
                    // || event.shortMethodName().contains(".VariableExpander.")
                    // || event.shortMethodName().contains(".isCategoryAccessible")
                    // || event.shortMethodName().contains(".BannerCommon.")
                    // || event.shortMethodName().contains("GenericBannerDAO")
                    ) { //|| event.shortMethodName().contains(".set") 
						// System.out.println("Removed init, setters, and lambda "+ event.shortMethodName());
					} else {
                        eventToIDMapping.put(event.shortMethodName(), methodID); // to convert string sequence to int sequence
                        methodID++;
                    }
                }

            }
            System.out.println("### XES Starting");
            // uncomment below to get the xes files, takes time to produce the output, hence commenting
            if (isXESFileRequired) {
                int i = 1;
                for (Entry entry : dataMap.entrySet()) {
                    // System.out.println(entry.getKey() + "/" + entry.getValue());
                        xesText = xesText + "<trace>"+"\n"+
                                "<string key=\"concept:name\" value=\"Trace"+i+"\"/>"+
                                "<string key=\"traceID\" value=\""+entry.getKey()+"\"/>"+"\n";
                            i++;
                            System.out.println("### : "+i);
                            ArrayList<Event> entryList = (ArrayList<Event>) entry.getValue();
                            for (Event e : entryList){
                                xesText = xesText+"<event>"+"\n"+
                                "<string key=\"concept:name\" value=\""+e.methodName()+"\"/>"+"\n"+
                                "<string key=\"lifecycle:transition\" value=\"complete\"/>"+"\n"+
                                "<date key=\"time:timestamp\" value=\""+e.getFormattedDate()+"\"/>"+"\n"+
                                        "<string key=\"caseId\" value=\""+e.getSequenceId()+"\"/>"+"\n"+
                                        "<int key=\"duration\" value=\""+e.getMethodDuration()+"\"/>"+"\n"+
                                        "<int key=\"callingOrder\" value=\""+e.getCallingOrder()+"\"/>"+"\n"+
                                        "<int key=\"depth\" value=\""+e.getDeptOfCallingStack()+"\"/>"+"\n"+"</event>"; 
                                
                            }
                            xesText = xesText+"</trace>"+"\n";
                }
                xesText = xesText+"</log>"+"\n";
                writer.println(xesText);
                writer.close();
                System.out.println("Writing data to XES file completed...");
            }   
            System.out.println("### method to ID mapping");
            if(isMethodToIdMappingRequired && eventToIDMapping.size()>0) {
                File methodToIdMappingFile = new File(methodToIdMappingFileLocation);
                try {
                    methodToIdMappingFile.createNewFile();
                    String fileContenet = "";
                    for (Entry entry : eventToIDMapping.entrySet()) {
                        // System.out.println(entry.getKey() + "/" + entry.getValue());
                        fileContenet = fileContenet + " Method : "+entry.getKey().toString() +
                                    "ID : "+ entry.getValue().toString()+"\n";
                    }
                PrintWriter methodInfoWriter = new PrintWriter(methodToIdMappingFile.getAbsolutePath(), "UTF-8");
                methodInfoWriter.println(fileContenet);
                methodInfoWriter.close();
                System.out.println("Writing data to Method to ID Mapping file completed...");

                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.out.println("Exception in convert logs to XES format : "+ e.getLocalizedMessage());
            e.printStackTrace();
        }
	}

    public void convertToNumberSequence() {
        System.out.println("### Converting number sequence");
        String inputStringToPatternMinning = "";
        for (Map.Entry<String,List<Event>> entry : dataMap.entrySet())  {
			List<Integer> uniqueMethodIntegerList = new ArrayList<Integer>(0); // integer mapped event list in correct order
				for (Event event : entry.getValue()) { // removing init, setters, and lambda functions
                    if ( event.shortMethodName().contains("init") 
                    || event.shortMethodName().contains("private")
                    || event.shortMethodName().contains(".lambda") 
                    || event.shortMethodName().contains("getBotAgents") 
                    || event.shortMethodName().contains("WebRequestContext") 
                    || event.shortMethodName().contains("CsrfFilter")
                    || event.shortMethodName().contains("CsrfHttpServletRequestWrapper") 
                    || event.shortMethodName().contains("net.jforum.JForum.")
                    || event.shortMethodName().contains("net.jforum.DBConnection")
                    || event.shortMethodName().contains("net.jforum.entities.") 
                    || event.shortMethodName().contains("net.jforum.util.I18n.") 
                    || event.shortMethodName().contains("net.jforum.context.JForumContext") 
                    || event.shortMethodName().contains("net.jforum.util.legacy.clickstream.")
                    || event.shortMethodName().contains("net.jforum.util.DbUtils") 
                    || event.shortMethodName().contains("net.jforum.JForumExecutionContext")
                    || event.shortMethodName().contains("net.jforum.cache.") 
                    || event.shortMethodName().contains("net.jforum.util.stats.")
                    || event.shortMethodName().contains("net.jforum.repository.SecurityRepository")
                    || event.shortMethodName().contains("net.jforum.repository.ModulesRepository")
                    || event.shortMethodName().contains("net.jforum.SessionFacade.")
                    || event.shortMethodName().contains("net.jforum.security.")
                    || event.shortMethodName().contains("net.jforum.context.web.")
                    || event.shortMethodName().contains("net.jforum.Command.") 
                    || event.shortMethodName().contains("ControllerUtils")
                    || event.shortMethodName().contains("net.jforum.repository.Tpl")
                    || event.shortMethodName().contains("net.jforum.util.bbcode.")
                    || event.shortMethodName().contains("net.jforum.util.Hash")
                    || event.shortMethodName().contains("net.jforum.view.admin.LuceneStatsAction")
                    || event.shortMethodName().contains("net.jforum.dao.generic.GenericDataAccessDriver")
                    || event.shortMethodName().contains("RankingAction")
                    || event.shortMethodName().contains("CacheAction")
                    || event.shortMethodName().contains("Captcha")
                    || event.shortMethodName().contains("DBVersionWorkarounder")
                    || event.shortMethodName().contains("PostAction")
                    || event.shortMethodName().contains("HikariPooledConnection")
                    || event.shortMethodName().contains("BoundedLinkedHashMap")
                    || event.shortMethodName().contains("DataAccessDriver")
                    || event.shortMethodName().contains(".forum.ForumAction.list")
                    || event.shortMethodName().contains(".UrlPattern.") // temp
                    // || event.shortMethodName().contains(".SystemGlobals.")
                    // || event.shortMethodName().contains(".VariableExpander.")
                    // || event.shortMethodName().contains(".isCategoryAccessible")
                    // || event.shortMethodName().contains(".BannerCommon.")
                    // || event.shortMethodName().contains("GenericBannerDAO")
                    ) { //|| event.shortMethodName().contains(".set") 
						// System.out.println("Removed init, setters, and lambda "+ event.shortMethodName());
					} else {
                        int eventId = eventToIDMapping.get(event.shortMethodName());
                        event.setIntegerId(eventId);
                        
                        if (eventId >=1 && !uniqueMethodIntegerList.contains(eventId)) {
                            uniqueMethodIntegerList.add(eventId);
                        } else {
                            // same event occurred twice in the trace, hence avoiding the second
                            // System.out.println("Method integer mapping - "+ event.shortMethodName() +" has mapping id "+ eventId + " already contains "+ uniqueMethodIntegerList.contains(eventId));
                        }
						
					}
				}
			if (uniqueMethodIntegerList.size() > 1) { // removing 1 methods from pattern minning library input
                String spaceSeparatedString = "";
                List<Integer> sortedUniqueMethodIntegerList = uniqueMethodIntegerList;
                Collections.sort(sortedUniqueMethodIntegerList); // sorting order required for pattern minning library
                    for (int methodInt: sortedUniqueMethodIntegerList){
                        spaceSeparatedString = spaceSeparatedString+ String.valueOf(methodInt) + " ";
                    }
                // spaceSeparatedString.trim();
                // sample format for GSP input 3 2 4 1 -1 -2 (-1 for end of event, -2 for end of trace)
                inputStringToPatternMinning = inputStringToPatternMinning + spaceSeparatedString +"-1 -2"+ " \n"; // frequent PM algor
                // inputStringToPatternMinning = inputStringToPatternMinning + spaceSeparatedString +" \n"; // apriori & pattern growth (FP)item set
                numberedEventList.add(uniqueMethodIntegerList); // numbered event list contains the lists of interger events
            }
		} 
        
        // File file = new File("C:\\Development\\MSGeneratorInputOutput\\Output\\"+"eventPattern.txt");
        File file = new File(patternMinningInputFile);

        // file.getParentFile().mkdir();
        try {
            file.createNewFile();
            PrintWriter writer = new PrintWriter(file.getAbsolutePath(), "UTF-8");
            writer.println(inputStringToPatternMinning);
            writer.close();

        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public void executePatternMinning(){
        System.out.println("### Execute pattern mining");
        ExecuteJarWithParams executeJarWithParams = new ExecuteJarWithParams();
                // String[] parameters = {"run", "GSP", "C:/Development/MSGeneratorSupportWork/contextPrefixSpan.txt", "C:/Development/MSGeneratorSupportWork/output.txt", "5%"};
        
        String[] parameters = {"run", patternMiningAlgorithm, this.patternMinningInputFile, this.patternMinningOutputFile, this.minimumSupport, "3", "0"};
        // String[] parameters = {"run", "Apriori", this.patternMinningInputFile, this.patternMinningOutputFile, this.minimumSupport};
        // String[] parameters = {"run", "FPGrowth_itemsets", this.patternMinningInputFile, this.patternMinningOutputFile, this.minimumSupport, "3", "1"};
        executeJarWithParams.executePatternMinning(this.patternMinninglibrary, parameters);
        System.out.println("Pattern Minning Completed : "+ this.patternMinningOutputFile);
    }

    public void generateCostInfo() {
            String filePath = this.patternMinningOutputFile;
            Map<String, Double> supportDataMap = new HashMap<>(0);
            List<PatternMeta> patternMetaList = new ArrayList<>(0);
            try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
                String line;
                while ((line=br.readLine())!= null) { //format - 41953 43281 -1 #SUP: 87
                    if ( line!= null && !("").equals(line)) {
                        System.out.println(line);
                        String[] splittedArray = line.split(" -1 "); // for GSP, prefix span
                        // String[] splittedArray = line.split("#"); // for apriori, FP
                        String key = splittedArray[0].trim(); // ex key 41953 43281
                        // System.out.println("Key  : "+ key + " Value: "+splittedArray[1].split(" ")[1]);
                        double value = Double.parseDouble(splittedArray[1].split(" ")[1]);// ex value 87
                        supportDataMap.put(key, value);
                    }
                }
                
                supportDataMap.forEach((key, value) -> {
                    // key has space means more than one method like 41953 43281 -1 #SUP: 87
                    if (key.contains(" ")) { // two or more method pattern
                        PatternMeta patternMeta = new PatternMeta();
                        patternMeta.setPattern(key);
                        patternMeta.setSupport(value); 
                        // confidence (A,B) = support(A,B)/support(A)
                        // support(A,B) is the map value
                        // support(A) should available in supportDataMap since pattern A should 
                        // have a higher support for pattern A,B to exist
                        String[] patternMethodsArray = key.split(" ");
                        patternMeta.setPatternLength(patternMethodsArray.length);
                        // System.out.println("Pattern methods array : "+ patternMethodsArray.toString());
                        if (patternMethodsArray.length == 2 ) {  // two method pattern A B
                            if (supportDataMap.get(patternMethodsArray[0]) != null) {
                                double suppotOfFirstMethod = supportDataMap.get(patternMethodsArray[0]);
                                if (suppotOfFirstMethod > 0) {
                                    double confidence = value/suppotOfFirstMethod;
                                    patternMeta.setConfidence(confidence);
                                } else {
                                    System.out.println("Error in pattern values. Cannot calculate Confidence"+ patternMethodsArray.toString());
                                }
                            } else {
                                System.out.println("Error in pattern values. Cannot calculate Confidence"+ key);
                            }
                        } else { // 3 or more method patterns A B C/ A B C D
                            String[] firstElementsExceptLast = Arrays.copyOfRange(patternMethodsArray, 0, (patternMethodsArray.length-1)); // 0 inclusive, patternMethodsArray.length-1 exclusive, hence no need of patternMethodsArray.length-2
                            String supportKey = String.join(" ", firstElementsExceptLast);
                            if (supportDataMap.get(supportKey) != null) {
                                double suppotOfFirstMethods = supportDataMap.get(supportKey);
                                if (suppotOfFirstMethods > 0) {
                                    double confidence = value/suppotOfFirstMethods;
                                    patternMeta.setConfidence(confidence);
                                } else {
                                    System.out.println("Error in pattern values. Cannot calculate Confidence" + patternMethodsArray.toString());
                                }
                            } else {
                                System.out.println("Error in pattern values. Cannot calculate Confidence" + key);
                            }
                        }


                        // logic for length between events or methods
                        String[] stringNumberArray = key.split(" ");
                        // System.out.println("String array before convert to Integers"+stringNumberArray.toString());
                        List<Integer> integerNumberListOfLocalPattern = new ArrayList<>(0);
                        String methodNamePattern = " ";
                        for(String methodIdString: stringNumberArray) {
                            int methodIdIntValue = Integer.parseInt(methodIdString);
                            integerNumberListOfLocalPattern.add(methodIdIntValue);
                            methodNamePattern = methodNamePattern + getKeyByValue(eventToIDMapping, methodIdIntValue) + " , ";
                        }
                        patternMeta.setPattenwithMethodNames(methodNamePattern);
                        // System.out.println("List with Integer Events "+ integerNumberListOfLocalPattern.toString());
                        double minDepth = 0;
                        int numberOfOccurances = 0;
                        // System.out.println("******************** total events: " + dataMap.size());

                        int accumilatedMinDepth = 0;
                        for (List<Event> eventList : dataMap.values()) { // numbered event list contains the total unique integer event list
                            List<Integer> depthList = isEventList2ContainsIntegerList1(eventList, integerNumberListOfLocalPattern);
                            if (depthList != null) { // null means no full match
                                numberOfOccurances = numberOfOccurances + 1;
                                accumilatedMinDepth = accumilatedMinDepth + Collections.min(depthList);
                            }
                        }
                        minDepth = accumilatedMinDepth/numberOfOccurances;

                        numberOfOccurances = 0;
                        double accumilatedExecutionTime = 0;
                        for (List<Event> eventList : dataMap.values()) { // numbered event list contains the total unique integer event list
                            double executionTime = getPatternExecutionTime(eventList, integerNumberListOfLocalPattern);
                            if (executionTime > 0) { // 0 means no full match
                                numberOfOccurances = numberOfOccurances + 1;
                                accumilatedExecutionTime = accumilatedExecutionTime + executionTime;
                            }
                        }
                        patternMeta.setExecutionTime(accumilatedExecutionTime/numberOfOccurances);


                        double distance = 0;
                        numberOfOccurances = 0;
                        for (List<Integer> orderedMethodIdList : numberedEventList) { // numbered event list contains the total unique integer event list
                            List<Integer> distanceList = isList2ContainsList1(orderedMethodIdList, integerNumberListOfLocalPattern);
                            if (distanceList != null && distanceList.size() > 1) { // null means no full match
                                numberOfOccurances = numberOfOccurances + 1;
                                distance = distance + getDistanceValueFromIndexList(distanceList);
                            }
                        }


                        // System.out.println("TOTAL DISTANCE = "+ distance +" AND NUMBER OF OCCURANCES = " + numberOfOccurances);
                        // get total distance and average by total number of matches
                        // one pattern might occurred 5 times, one 6 times. then averaging to get the
                        // valid distance
                        patternMeta.setDistance(distance/numberOfOccurances); 
                        patternMeta.setAverageDepth(minDepth);
                        patternMeta.setTotalEvents(dataMap.size());
                        // numbered event list contains the total event list provided to the library.
                        // patternMeta.setCost((patternMeta.getSupport()/patternMeta.getTotalEvents()) + patternMeta.getConfidence() + (1/patternMeta.getDistance()));
                        patternMeta.setCost(patternMeta.getSupport() * patternMeta.getConfidence()* patternMeta.getPatternLength() * patternMeta.getAverageDepth()  * (patternMeta.getExecutionTime()/1000));
                        // System.out.println(patternMeta.toString());
                        patternMetaList.add(patternMeta);
                    } else { // one method pattern
                        //discarding one method patterns
                        // System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@ One method patterns - "+key);
                        if (value > 100) {
                            PatternMeta patternMeta = new PatternMeta();
                            patternMeta.setPattern(key);
                            patternMeta.setSupport(value); 
                            patternMeta.setConfidence(1); // cofidence 1 for single methods since no returning to main code from the microservice
                            patternMeta.setPatternLength(1);
                            
    
                            // // logic for length between events or methods
                            // String[] stringNumberArray = key.split(" ");
                            // // key contains the event id number as a string for single method patterns
                            patternMeta.setPattenwithMethodNames(getKeyByValue(eventToIDMapping, Integer.parseInt(key)));
                            // // distance is zero for one method patterns
                            patternMeta.setDistance(1); 
                            patternMeta.setTotalEvents(dataMap.size());
    
                            List<Integer> integerNumberListOfLocalPattern = new ArrayList<>(0);
                            integerNumberListOfLocalPattern.add(Integer.parseInt(key));
                            double minDepth = 0;
                            int numberOfOccurances = 0;
                            int accumilatedMinDepth = 0;
                            for (List<Event> eventList : dataMap.values()) { // numbered event list contains the total unique integer event list
                                List<Integer> depthList = isEventList2ContainsIntegerList1(eventList, integerNumberListOfLocalPattern);
                                if (depthList != null) { // null means no full match
                                    numberOfOccurances = numberOfOccurances + 1;
                                    accumilatedMinDepth = accumilatedMinDepth + Collections.min(depthList);
                                }
                            }
                            minDepth = accumilatedMinDepth/numberOfOccurances;
                            patternMeta.setAverageDepth(minDepth);
    
                            numberOfOccurances = 0;
                            double accumilatedExecutionTime = 0;
                            for (List<Event> eventList : dataMap.values()) { // numbered event list contains the total unique integer event list
                                double executionTime = getPatternExecutionTime(eventList, integerNumberListOfLocalPattern);
                                if (executionTime > 0) { // 0 means no full match
                                    numberOfOccurances = numberOfOccurances + 1;
                                    accumilatedExecutionTime = accumilatedExecutionTime + executionTime;
                                }
                            }
                            patternMeta.setExecutionTime(accumilatedExecutionTime/numberOfOccurances);
                            patternMeta.setCost(patternMeta.getSupport() * patternMeta.getPatternLength() * patternMeta.getAverageDepth()  * (patternMeta.getExecutionTime()/1000));
                            // System.out.println(patternMeta.toString());
                            patternMetaList.add(patternMeta);
                        }
                    }
                });

                double minSupport = 0;
                double maxSupport = 0;
                double minDepth = 0;
                double maxDepth = 0;
                double minExecTime = 0;
                double maxExecTime = 0;
                double minPatternLength = 0;
                double maxPatternLength = 0;

                for (PatternMeta patternMeta: patternMetaList) {
                    if (patternMeta.getSupport() < minSupport) {
                        minSupport = patternMeta.getSupport();
                    }
                    if (patternMeta.getSupport() > maxSupport) {
                        maxSupport = patternMeta.getSupport();
                    }
                    if (patternMeta.getAverageDepth() < minDepth) {
                        minDepth = patternMeta.getAverageDepth();
                    }
                    if (patternMeta.getAverageDepth() > maxDepth) {
                        maxDepth = patternMeta.getAverageDepth();
                    }
                    if (patternMeta.getExecutionTime() < minExecTime) {
                        minExecTime = patternMeta.getExecutionTime();
                    }
                    if (patternMeta.getExecutionTime() > maxExecTime) {
                        maxExecTime = patternMeta.getExecutionTime();
                    }
                    if (patternMeta.getPatternLength() < minPatternLength) {
                        minPatternLength = patternMeta.getPatternLength();
                    }
                    if (patternMeta.getPatternLength() > maxPatternLength) {
                        maxPatternLength = patternMeta.getPatternLength();
                    }
                }

                System.out.println("********* min Exec : "+minExecTime + " Max Exec : "+maxExecTime);

                for (PatternMeta patternMeta : patternMetaList) {
                    patternMeta.setNormalizedSupport((patternMeta.getSupport() - minSupport)/(maxSupport - minSupport));
                    patternMeta.setNormalizedAverageDepth((patternMeta.getAverageDepth() - minDepth) / (maxDepth - minDepth));
                    patternMeta.setNormalizedExecutionTime((patternMeta.getExecutionTime() - minExecTime) / (maxExecTime - minExecTime));
                    patternMeta.setNormalizedPatternLength((patternMeta.getPatternLength() - minPatternLength) / (maxPatternLength - minPatternLength));
                    patternMeta.setCost(patternMeta.getNormalizedSupport() * patternMeta.getConfidence()* patternMeta.getNormalizedPatternLength() * patternMeta.getNormalizedAverageDepth()  * patternMeta.getNormalizedExecutionTime());
                }


                if (patternMetaList.size() >0){
                    patternMetaList.sort(Comparator.comparingDouble(PatternMeta::getCost).reversed());
                }

                // writing output to file        
                File file = new File(patternCostInfoFileLocation);
                try {
                    file.createNewFile();
                    PrintWriter writer = new PrintWriter(file.getAbsolutePath(), "UTF-8");

                    String finalOutput = "";
                    for (PatternMeta patternMeta: patternMetaList) {
                     finalOutput = finalOutput + patternMeta.toString() + " \n \n";
                    }
                    writer.println(finalOutput);
                    writer.close();

                } catch (IOException e1) {
                    e1.printStackTrace();
                }

            } catch (IOException e) {
               System.out.println("Error in patten cost calculation : "+ e.getMessage());
            }

            this.generateExcel(patternMetaList);
            // this.generateExeclFilesSinglePattern(patternMetaList);    
            // this.generateExcelFileTwoPattern();
            // this.generateExcelFileThreePattern();
            // this.generateExcelFileFourPattern();
            // this.generateExeclFilesFivePattern();



            System.out.println("========== PROGRAM ENDS ==================");
            System.exit(0);
    }

    private void generateExcel(List<PatternMeta> patternMetaList){
        String[] arr = {"S", "C", "P", "D", "E"}; // s - support, c - confidence, p - pattern length, d - depth, e - execution time
        List<List<String>> result = new ArrayList<>();
        int n = arr.length;

        // There are 2^n subsets (including the empty subset)
        int totalSubsets = 1 << n; // This is 2^n

        // Loop through from 0 to 2^n - 1
        for (int i = 0; i < totalSubsets; i++) {
            List<String> subset = new ArrayList<>();
            for (int j = 0; j < n; j++) {
                // If the j-th bit in i is set, include arr[j] in the subset
                if ((i & (1 << j)) != 0) {
                    subset.add(arr[j]);
                }
            }
            result.add(subset);
        }

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            for (List<String> subset : result) {
                if (!subset.isEmpty()){
                    
                    String tabNameTemp = subset.toString();
                    String tabName = tabNameTemp.substring(1, tabNameTemp.length() - 1).replace(", ", ",");
                    System.out.println("Generating excel for - " + tabName);
                    
                    for (PatternMeta patternMeta : patternMetaList) {
                        double cost = 1;
                        for (int i =0; i< subset.size(); i++) {
                            if (subset.contains("S")) {
                                cost = cost * patternMeta.getNormalizedSupport();
                            }
                            if (subset.contains("C")) {
                                cost = cost * patternMeta.getConfidence();
                            }
                            if (subset.contains("P")) {
                                cost = cost * patternMeta.getNormalizedPatternLength();
                            }
                            if (subset.contains("D")) {
                                cost = cost * patternMeta.getNormalizedAverageDepth();
                            }
                            if (subset.contains("E")) {
                                cost = cost * patternMeta.getNormalizedExecutionTime();
                            }
                        }
                        patternMeta.setCost(cost);
                    }
                    if (patternMetaList.size() >0){
                        patternMetaList.sort(Comparator.comparingDouble(PatternMeta::getCost).reversed());
                    }

                    XSSFSheet supportSheet = workbook.createSheet(tabName); // Create a new sheet
                    XSSFRow supportTitleRow = supportSheet.createRow(0); // Create the first row
                    supportTitleRow.createCell(0).setCellValue("Pattern"); // Add data to the first cell
                    supportTitleRow.createCell(1).setCellValue("Cost"); // Add data to the second cell
        
                    int i = 0;
                    for (PatternMeta patternMeta: patternMetaList) {
                        i++;
                        XSSFRow contentRow = supportSheet.createRow(i); // Crieate the first row
                        contentRow.createCell(0).setCellValue(patternMeta.getPattenwithMethodNames()); // Add data to the first cell
                        contentRow.createCell(1).setCellValue(patternMeta.getCost()); // Add data to the second cell
                    }
                }
            }
            try (FileOutputStream fos = new FileOutputStream(new File(excelFileLocation))) {
                workbook.write(fos);
                System.out.println("Excel file created and data written successfully.");
            } catch (IOException e) {
                System.out.println("Error creating or writing to the Excel file: " + e.getMessage());
            }
        } catch (Exception e) {
            System.out.println("Error in excel book creation : "+ e.getLocalizedMessage());

        }

    }

    private void generateExeclFilesSinglePattern(List<PatternMeta> patternMetaList) {

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet supportSheet = workbook.createSheet("Support"); // Create a new sheet
            XSSFRow supportTitleRow = supportSheet.createRow(0); // Create the first row
            supportTitleRow.createCell(0).setCellValue("Pattern"); // Add data to the first cell
            supportTitleRow.createCell(1).setCellValue("Support"); // Add data to the second cell

            patternMetaList.sort(Comparator.comparingDouble(PatternMeta::getNormalizedSupport).reversed());
            int i = 0;
            for (PatternMeta patternMeta: patternMetaList) {
                i++;
                XSSFRow contentRow = supportSheet.createRow(i); // Crieate the first row
                contentRow.createCell(0).setCellValue(patternMeta.getPattenwithMethodNames()); // Add data to the first cell
                contentRow.createCell(1).setCellValue(patternMeta.getNormalizedSupport()); // Add data to the second cell
            }

            XSSFSheet confidenceSheet = workbook.createSheet("Confidence"); // Create a new sheet
            XSSFRow confidenceTitleRow = confidenceSheet.createRow(0); // Create the first row
            confidenceTitleRow.createCell(0).setCellValue("Pattern"); // Add data to the first cell
            confidenceTitleRow.createCell(1).setCellValue("Confidence"); // Add data to the second cell
            patternMetaList.sort(Comparator.comparingDouble(PatternMeta::getConfidence).reversed());
            i=0;
            for (PatternMeta patternMeta: patternMetaList) {
                i++;
                XSSFRow contentRow = confidenceSheet.createRow(i); // Create the first row
                contentRow.createCell(0).setCellValue(patternMeta.getPattenwithMethodNames()); // Add data to the first cell
                contentRow.createCell(1).setCellValue(patternMeta.getConfidence()); // Add data to the second cell
            }

            XSSFSheet execTimeSheet = workbook.createSheet("Execution Time"); // Create a new sheet
            XSSFRow execTimeTitleRow = execTimeSheet.createRow(0); // Create the first row
            execTimeTitleRow.createCell(0).setCellValue("Pattern"); // Add data to the first cell
            execTimeTitleRow.createCell(1).setCellValue("Execution Time"); // Add data to the second cell

            patternMetaList.sort(Comparator.comparingDouble(PatternMeta::getNormalizedExecutionTime).reversed());
            i=0;
            for (PatternMeta patternMeta: patternMetaList) {
                i++;
                XSSFRow contentRow = execTimeSheet.createRow(i); // Create the first row
                contentRow.createCell(0).setCellValue(patternMeta.getPattenwithMethodNames()); // Add data to the first cell
                contentRow.createCell(1).setCellValue(patternMeta.getNormalizedExecutionTime()); // Add data to the second cell
            }

            XSSFSheet patLengthSheet = workbook.createSheet("Pattern Length"); // Create a new sheet
            XSSFRow patLengthTitleRow = patLengthSheet.createRow(0); // Create the first row
            patLengthTitleRow.createCell(0).setCellValue("Pattern"); // Add data to the first cell
            patLengthTitleRow.createCell(1).setCellValue("Pattern Length"); // Add data to the second cell

            patternMetaList.sort(Comparator.comparingDouble(PatternMeta::getNormalizedPatternLength).reversed());
            i=0;
            for (PatternMeta patternMeta: patternMetaList) {
                i++;
                XSSFRow contentRow = patLengthSheet.createRow(i); // Create the first row
                contentRow.createCell(0).setCellValue(patternMeta.getPattenwithMethodNames()); // Add data to the first cell
                contentRow.createCell(1).setCellValue(patternMeta.getNormalizedPatternLength()); // Add data to the second cell
            }

            XSSFSheet avgDepthSheet = workbook.createSheet("Average Depth"); // Create a new sheet
            XSSFRow avgDepthTitleRow = avgDepthSheet.createRow(0); // Create the first row
            avgDepthTitleRow.createCell(0).setCellValue("Pattern"); // Add data to the first cell
            avgDepthTitleRow.createCell(1).setCellValue("Average Depth"); // Add data to the second cell

            patternMetaList.sort(Comparator.comparingDouble(PatternMeta::getNormalizedAverageDepth).reversed());
            i=0;
            for (PatternMeta patternMeta: patternMetaList) {
                i++;
                XSSFRow contentRow = avgDepthSheet.createRow(i); // Create the first row
                contentRow.createCell(0).setCellValue(patternMeta.getPattenwithMethodNames()); // Add data to the first cell
                contentRow.createCell(1).setCellValue(patternMeta.getNormalizedAverageDepth()); // Add data to the second cell
            }

            // Write the workbook to the file
            try (FileOutputStream fos = new FileOutputStream(new File(excelFileLocation+"pattern1Output"))) {
                workbook.write(fos);
                System.out.println("Excel file created and data written successfully.");
            }
        } catch (IOException e) {
            System.out.println("Error creating or writing to the Excel file: " + e.getMessage());
        }
    }

    private String getKeyByValue(Map<String, Integer> map, int valueToFind) {
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            if (entry.getValue() == valueToFind) {
                return entry.getKey();  // Return the key if the value matches
            }
        }
        return null;  // Return null if no matching value is found
    }

    private List<Integer> isList2ContainsList1(List<Integer> fullIntegerList, List<Integer> subIntegetrList) {
        List<Integer> elementIndexList = new ArrayList<>(0); //this returns the index of matching events
        for (int element : subIntegetrList) {
            boolean found = false;
            for (int value : fullIntegerList) {
                if (value == element) {
                    found = true;
                    elementIndexList.add(fullIntegerList.indexOf(value));
                    break;
                }
            }
            if (!found) {
                return null; // Element from list2 not found fully in list1
            }
        }
        // System.out.println(fullIntegerList.toString() +" CONTAINS "+ subIntegetrList.toString());
        // System.out.println("ELEMENT INDEX LIST : "+elementIndexList);
        return elementIndexList; // All elements from list2 are in list1
    }

    private List<Integer> isEventList2ContainsIntegerList1(List<Event> fullEventList, List<Integer> subIntegetrList) {
        List<Integer> elementDepthList = new ArrayList<>(0); //this returns the index of matching events
        for (int element : subIntegetrList) {
            boolean found = false;
            for (Event event : fullEventList) {
                if (event.getIntegerId() == element) {
                    found = true;
                    elementDepthList.add(event.getDeptOfCallingStack());
                    break;
                }
            }
            if (!found) {
                return null; // Element from list2 not found fully in list1
            }
        }
        // System.out.println(fullIntegerList.toString() +" CONTAINS "+ subIntegetrList.toString());
        // System.out.println("ELEMENT INDEX LIST : "+elementIndexList);
        return elementDepthList; // All elements from list2 are in list1
    }

    private double getPatternExecutionTime(List<Event> fullEventList, List<Integer> subIntegetrList) {
        double executionTime = 0;
        for (int element : subIntegetrList) {
            boolean found = false;
            for (Event event : fullEventList) {
                if (event.getIntegerId() == element) {
                    found = true;
                    executionTime = executionTime + event.getMethodDuration();
                    break;
                }
            }
            if (!found) {
                return 0; // Element from list2 not found fully in list1
            }
        }
        // System.out.println(fullIntegerList.toString() +" CONTAINS "+ subIntegetrList.toString());
        // System.out.println("ELEMENT INDEX LIST : "+elementIndexList);
        return executionTime; // All elements from list2 are in list1
    }

    private double getDistanceValueFromIndexList(List<Integer> indexList){
        int firstIndex = indexList.get(0);
        int returnValue = 0;
        for (int i=1; i<indexList.size(); i++){ // avoid first index and iterate from second
            returnValue = returnValue + (indexList.get(i) - firstIndex); // first compare with first index
            firstIndex = indexList.get(i); // moving the first index to get the running distance
        }
        // System.out.println(indexList.toString() +" has return distance value "+returnValue);
        return returnValue/(indexList.size()-1);
    }
    
    private static String convertDate(long dateInNano) {
		try {
		// String target = "01/01/1970 12:00:00:000000";  // Your given date string
		String target = "1970-01-01 12:00:00";
		long nanoTime = Math.abs(dateInNano%1000000);
		long millis = TimeUnit.MILLISECONDS.convert(dateInNano, TimeUnit.NANOSECONDS); 
		
		// DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //2008-12-09T08:20:01.527+01:00

		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date date = formatter.parse(target);
		
		long newTimeInmillis = date.getTime() + millis;
		
		Date date2 = new Date(newTimeInmillis);
		String formattedDate =  formatter.format(date2);
		return (formattedDate.split(" ")[0] +"T"+formattedDate.split(" ")[1]+"."+nanoTime);
		// return formatter.format(date2)+"."+nanoTime;
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
		}
		return ""+dateInNano;
	}

}
