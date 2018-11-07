package com.sankuai.octo.scanner.model.report;


public class DuplicateRegistryReport extends ScannerReport {
    private int duplicateFactor = 1;

    public DuplicateRegistryReport(int level, String category, String content
            , String identifier, int duplicateFactor) {
        super(level, category, content, identifier);
        this.duplicateFactor = duplicateFactor;
    }

    public int getDuplicateFactor() {
        return duplicateFactor;
    }

    public void setDuplicateFactor(int duplicateFactor) {
        this.duplicateFactor = duplicateFactor;
    }
}
