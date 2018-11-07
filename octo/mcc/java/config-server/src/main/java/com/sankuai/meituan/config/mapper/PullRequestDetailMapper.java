package com.sankuai.meituan.config.mapper;

import com.sankuai.meituan.config.domain.PullRequestDetail;
import com.sankuai.meituan.config.domain.PullRequestDetailExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

public interface PullRequestDetailMapper {
    int countByExample(PullRequestDetailExample example);

    int deleteByExample(PullRequestDetailExample example);

    int deleteByPrimaryKey(Integer prDetailId);

    int insert(PullRequestDetail record);

    int insertSelective(PullRequestDetail record);

    List<PullRequestDetail> selectByExampleWithRowbounds(PullRequestDetailExample example, RowBounds rowBounds);

    List<PullRequestDetail> selectByExample(PullRequestDetailExample example);

    PullRequestDetail selectByPrimaryKey(Integer prDetailId);

    int updateByExampleSelective(@Param("record") PullRequestDetail record, @Param("example") PullRequestDetailExample example);

    int updateByExample(@Param("record") PullRequestDetail record, @Param("example") PullRequestDetailExample example);

    int updateByPrimaryKeySelective(PullRequestDetail record);

    int updateByPrimaryKey(PullRequestDetail record);
}