package com.example.springboottemplate.dto;

import java.util.ArrayList;
import java.util.List;

public class StoreBusinessHoursRule {
    private List<Integer> days = new ArrayList<>();
    private String start;
    private String end;

    public List<Integer> getDays() {
        return days;
    }

    public void setDays(List<Integer> days) {
        this.days = days;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }
}
