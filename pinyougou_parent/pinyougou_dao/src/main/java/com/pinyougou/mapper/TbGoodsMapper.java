package com.pinyougou.mapper;

import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TbGoodsMapper {
    int countByExample(TbGoodsExample example);

    int deleteByExample(TbGoodsExample example);

    int deleteByPrimaryKey(Long id);

    int insert(TbGoods record);

    int insertSelective(TbGoods record);

    List<TbGoods> selectByExample(TbGoodsExample example);

    TbGoods selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") TbGoods record, @Param("example") TbGoodsExample example);

    int updateByExample(@Param("record") TbGoods record, @Param("example") TbGoodsExample example);

    int updateByPrimaryKeySelective(TbGoods record);

    int updateByPrimaryKey(TbGoods record);

	List<Long> GroupByCategory1Id();

	List<Long> selectByCategory1Id(Long category1Id);

	List<Long> selectByCategory2IdAndGroup(Long category2Id);

	int selectByCategory3IdCount(Long category3Id);
}