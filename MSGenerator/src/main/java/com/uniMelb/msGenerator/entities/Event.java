package com.uniMelb.msGenerator.entities;

/**
 * This class contains the event information extracted from 
 * kieker log. Trace or use case is a collection of events.
 */
public class Event {

    private long sequenceId;
    private String methodSignature;
    private String sessionId;
    private String traceId;
    private long inTime;
    private long outTime;
    private int callingOrder;
    private int deptOfCallingStack;
    private String formattedDate;
    private int integerId;

    public Event() {
    }


    public void setSequenceId(long sequenceId) {
        this.sequenceId = sequenceId;
    }
    public void setMethodSignature(String methodSignature) {
        this.methodSignature = methodSignature;
    }
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
    public void setInTime(long inTime) {
        this.inTime = inTime;
    }
    public void setOutTime(long outTime) {
        this.outTime = outTime;
    }
    public void setCallingOrder(int callingOrder) {
        this.callingOrder = callingOrder;
    }
    public void setDeptOfCallingStack(int deptOfCallingStack) {
        this.deptOfCallingStack = deptOfCallingStack;
    }
    public long getSequenceId() {
        return sequenceId;
    }
    public String getMethodSignature() {
        return methodSignature;
    }
    public String getSessionId() {
        return sessionId;
    }
    public String getTraceId() {
        return traceId;
    }
    public long getInTime() {
        return inTime;
    }
    public long getOutTime() {
        return outTime;
    }
    public int getCallingOrder() {
        return callingOrder;
    }
    public int getDeptOfCallingStack() {
        return deptOfCallingStack;
    }

    public void setFormattedDate(String formattedDate) {
        this.formattedDate = formattedDate;
    }

    public String getFormattedDate() {
        return formattedDate;
    }

    public double getMethodDuration() {
        
        if(this.inTime > 0 & this.outTime > 0) {
            return Double.parseDouble("" + (this.outTime - this.inTime));
        }
        return 0;
    }

    public String methodName() {
        if (this.methodSignature != null) {
            int methodIndex = methodSignature.split(" ").length-1;
            return this.methodSignature.split(" ")[methodIndex];
        }
        return "";
    }
    public String shortMethodName() {
        if (this.methodSignature != null) {
            return this.methodSignature;
        }
        if (this.methodSignature != null) {
            String parameters = "";
            String tempMethodSignature = "";
            if (this.methodSignature.contains("(")) {
                tempMethodSignature = this.methodSignature.split("(?=\\()")[0];
                parameters = this.methodSignature.split("(?=\\()")[1];
            } else {
                tempMethodSignature = methodSignature;
            }

            int methodIndex = tempMethodSignature.split(" ").length-1;
            String fullMethodName = tempMethodSignature.split(" ")[methodIndex];
            String className = fullMethodName.split("\\(")[0];
            String[] MethodNameArray = className.split("\\.");
            if (MethodNameArray != null & MethodNameArray.length >=2) {
            String shortName = MethodNameArray[MethodNameArray.length - 2]+"."+MethodNameArray[MethodNameArray.length - 1];
            if (shortName.contains("string") || shortName.contains("lang")){ //) {
                System.out.println("String Entry + ######################### "+ tempMethodSignature);
            }
            // System.out.println("Method Signagure : " + methodSignature + " Short Name : +" + shortName+ " Parameters : "+ parameters );
            return shortName; //+parameters;
            
        }
            
        }
        return "";
    }

    @Override
    public String toString() {
        return "Event [sequenceId=" + sequenceId + ", methodSignature=" + methodSignature + ", sessionId=" + sessionId
                + ", traceId=" + traceId + ", inTime=" + inTime + ", outTime=" + outTime + ", callingOrder="
                + callingOrder + ", deptOfCallingStack=" + deptOfCallingStack + ", formattedDate=" + formattedDate
                + ", integerId=" + integerId + "]";
    }


    public int getIntegerId() {
        return integerId;
    }


    public void setIntegerId(int integerId) {
        this.integerId = integerId;
    }

    
    
}
