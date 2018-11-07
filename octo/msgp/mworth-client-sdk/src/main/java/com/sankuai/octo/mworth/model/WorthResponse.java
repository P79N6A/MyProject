package com.sankuai.octo.mworth.model;

import java.util.List;

/**
 */
public class WorthResponse <T> {

    private List<T> result;
    private Page page;


    public List<T> getResult() {
        return result;
    }

    public void setResult(List<T> result) {
        this.result = result;
    }

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }
}
