package com.uniMelb.msGenerator.entities;

/**
 * This class contains the pattern meta information. pattern key, support, 
 * confience, distance, and total cost information. pattern in numbered method format,
 * not as string
 */
public class PatternMeta {
    private String pattern; // integer converted pattern
    private double support;
    private double confidence;
    private double distance;
    private double cost;
    private String pattenwithMethodNames; // string pattern with original method names
    private int totalEvents = 1;
    private int patternLength = 0;
    private double averageDepth = 0;
    private double executionTime = 0;
    private double normalizedSupport = 0;
    private double normalizedPatternLength = 0;
    private double normalizedAverageDepth = 0;
    private double normalizedExecutionTime = 0;

    
    public String getPattern() {
        return pattern;
    }
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
    public double getSupport() {
        return support;
    }
    public void setSupport(double support) {
        this.support = support;
    }
    public double getConfidence() {
        return confidence;
    }
    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }
    public double getDistance() {
        return distance;
    }
    public void setDistance(double distance) {
        this.distance = distance;
    }
    public double getCost() {
        return cost;
    }
    public void setCost(double cost) {
        this.cost = cost;
    }
    public String getPattenwithMethodNames() {
        return pattenwithMethodNames;
    }
    public void setPattenwithMethodNames(String pattenwithMethodNames) {
        this.pattenwithMethodNames = pattenwithMethodNames;
    }

    public int getTotalEvents() {
        return totalEvents;
    }
    public void setTotalEvents(int totalEvents) {
        this.totalEvents = totalEvents;
    }

    public int getPatternLength() {
        return patternLength;
    }
    public void setPatternLength(int patternLength) {
        this.patternLength = patternLength;
    }
    public double getAverageDepth() {
        return averageDepth;
    }
    public void setAverageDepth(double averageDepth) {
        this.averageDepth = averageDepth;
    }
    public double getExecutionTime() {
        return executionTime;
    }
    public void setExecutionTime(double executionTime) {
        this.executionTime = executionTime;
    }

    public double getNormalizedSupport() {
        return normalizedSupport;
    }
    public void setNormalizedSupport(double normalizedSupport) {
        this.normalizedSupport = normalizedSupport;
    }
    public double getNormalizedPatternLength() {
        return normalizedPatternLength;
    }
    public void setNormalizedPatternLength(double normalizedPatternLength) {
        this.normalizedPatternLength = normalizedPatternLength;
    }
    public double getNormalizedAverageDepth() {
        return normalizedAverageDepth;
    }
    public void setNormalizedAverageDepth(double normalizedAverageDepth) {
        this.normalizedAverageDepth = normalizedAverageDepth;
    }
    public double getNormalizedExecutionTime() {
        return normalizedExecutionTime;
    }
    public void setNormalizedExecutionTime(double normalizedExecutionTime) {
        this.normalizedExecutionTime = normalizedExecutionTime;
    }
    @Override
    public String toString() {
        return "PatternMeta [pattern=" + pattern + ", support=" + support + ", confidence=" + confidence + ", distance="
                + distance + ", cost=" + cost + ", pattenwithMethodNames=" + pattenwithMethodNames + ", totalEvents="
                + totalEvents + ", patternLength=" + patternLength + ", averageDepth=" + averageDepth
                + ", executionTime=" + executionTime + ", normalizedSupport=" + normalizedSupport
                + ", normalizedPatternLength=" + normalizedPatternLength + ", normalizedAverageDepth="
                + normalizedAverageDepth + ", normalizedExecutionTime=" + normalizedExecutionTime + "]";
    }


}

