package com.sankuai.octo.mworth.service;

import com.sankuai.octo.mworth.common.model.WorthEvent;

/**
 * Created by zava on 15/12/7.
 */
public interface WorthEventSevice {
    void save(WorthEvent worthEvent);
    void saveAsyn(WorthEvent worthEvent);
}
