package com.example.visionary;

import java.io.Serializable;

/**<p>
 * Created by Angad on 02-01-2017.
 * </p>
 */

public class PointData implements Serializable {
    private double[] start, end;
    private String duration, distance;
    private String instruction;

    public double[] getStart() {
        return start;
    }

    public void setStart(double[] start) {
        this.start = start;
    }

    public double[] getEnd() {
        return end;
    }

    public void setEnd(double[] end) {
        this.end = end;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }
}
