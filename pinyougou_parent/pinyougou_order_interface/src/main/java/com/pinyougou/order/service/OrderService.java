package com.pinyougou.order.service;

import java.util.List;

import com.pinyougou.pojo.TbOrder;

import groupEntity.ChartsView;

public interface OrderService {

	void save(TbOrder order);

	List<ChartsView> findAllByTimeAndSelled();

	ChartsView findPayGoods();

	ChartsView findPayOfYear();


}
