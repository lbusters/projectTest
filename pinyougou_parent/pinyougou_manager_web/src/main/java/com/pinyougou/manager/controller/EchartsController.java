package com.pinyougou.manager.controller;

import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.sellergoods.service.GoodsService;
import com.pinyougou.sellergoods.service.ItemCatService;

import groupEntity.ChartsView;

@RestController
@RequestMapping("/charts")
public class EchartsController {

	@Reference
	private OrderService orderService;
	
	@Reference
	private GoodsService goodsService;
	
	@Reference
	private ItemCatService itemCatService;
	
	
	@RequestMapping("/findAllByTimeAndSelled")
	public List<ChartsView> findAllByTimeAndSelled() {
		return orderService.findAllByTimeAndSelled();
	}
	
	@RequestMapping("/findGoodsByCategoryId")
	public ChartsView findGoodsByCategoryId() {
		return goodsService.findGoodsByCategoryId();
	}
	
	@RequestMapping("/findAllItemCat")
	public List<TbItemCat> findAllItemCat() {
		return itemCatService.findAll();
	}
	
	@RequestMapping("/findPayGoods")
	public ChartsView findPayGoods() {
		return orderService.findPayGoods();
	}
	@RequestMapping("/findPayOfYear")
	public ChartsView findPayOfYear() {
		return orderService.findPayOfYear();
	}
}
