package com.sankuai.octo.msgp.domain.report;

public class WeekTend {
    private String title;
    private SeriesData series;

    public WeekTend(String title, SeriesData series) {
        this.title = title;
        this.series = series;
    }

    public SeriesData getSeries() {
        return series;
    }

    public void setSeries(SeriesData series) {
        this.series = series;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}

