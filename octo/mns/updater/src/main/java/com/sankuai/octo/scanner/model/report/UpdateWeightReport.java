package com.sankuai.octo.scanner.model.report;


public class UpdateWeightReport extends ScannerReport {
    private int weight;
    private int newWeight;

    public UpdateWeightReport(int level, String category, String content,
           int weight, int newWeight,
            String identifier) {
        super(level, category, content, identifier);
        this.weight = weight;
        this.newWeight = newWeight;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getNewWeight() {
        return newWeight;
    }

    public void setNewWeight(int newWeight) {
        this.newWeight = newWeight;
    }
}
