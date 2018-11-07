package com.sankuai.meituan.config.mapper;

import com.sankuai.meituan.config.domain.Review;
import com.sankuai.meituan.config.domain.ReviewExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

public interface ReviewMapper {
    int countByExample(ReviewExample example);

    int deleteByExample(ReviewExample example);

    int deleteByPrimaryKey(Integer reviewId);

    int insert(Review record);

    int insertSelective(Review record);

    List<Review> selectByExampleWithRowbounds(ReviewExample example, RowBounds rowBounds);

    List<Review> selectByExample(ReviewExample example);

    Review selectByPrimaryKey(Integer reviewId);

    int updateByExampleSelective(@Param("record") Review record, @Param("example") ReviewExample example);

    int updateByExample(@Param("record") Review record, @Param("example") ReviewExample example);

    int updateByPrimaryKeySelective(Review record);

    int updateByPrimaryKey(Review record);
}