package com.meituan.service.mobile.mtthrift.client.cell;

import com.sankuai.inf.octo.mns.util.ProcessInfoUtil;


public class DefaultCellPolicy implements ICellPolicy {

    private static String localCell = ProcessInfoUtil.getCell();

    @Override
    public String getCell(RouterMetaData routerMetaData) {
        return localCell;
    }

}
