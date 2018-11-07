package com.sankuai.meituan.config.mapper;

import com.sankuai.meituan.config.domain.PullRequest;
import com.sankuai.meituan.config.domain.PullRequestExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

public interface PullRequestMapper {
    int countByExample(PullRequestExample example);

    int deleteByExample(PullRequestExample example);

    int deleteByPrimaryKey(Integer prId);

    int insert(PullRequest record);

    int insertSelective(PullRequest record);

    List<PullRequest> selectByExampleWithRowbounds(PullRequestExample example, RowBounds rowBounds);

    List<PullRequest> selectByExample(PullRequestExample example);

    PullRequest selectByPrimaryKey(Integer prId);

    int updateByExampleSelective(@Param("record") PullRequest record, @Param("example") PullRequestExample example);

    int updateByExample(@Param("record") PullRequest record, @Param("example") PullRequestExample example);

    int updateByPrimaryKeySelective(PullRequest record);

    int updateByPrimaryKey(PullRequest record);
}