package com.pinyougou.order.service.impl;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbOrderItemMapper;
import com.pinyougou.mapper.TbOrderMapper;
import com.pinyougou.mapper.TbPayLogMapper;
import com.pinyougou.mapper.TbSellerMapper;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbOrderExample;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.TbOrderItemExample;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.pojo.TbSeller;

import groupEntity.Cart;
import groupEntity.ChartsView;
import util.IdWorker;
@Service
@SuppressWarnings("all")
public class OrderServiceImpl implements OrderService {

	@Autowired
	private IdWorker idWorker;
	
	@Autowired
	private TbOrderMapper orderMapper;
	@Autowired
	private TbOrderItemMapper orderItemMapper;
	
	@Autowired
	private TbPayLogMapper payLogMapper;
	
	@Autowired
	private RedisTemplate redisTemplate;
	
	@Autowired
	private TbSellerMapper sellerMapper;
	@Override
	public void save(TbOrder order) {
//		 `user_id` varchar(50) COLLATE utf8_bin DEFAULT NULL COMMENT '用户id',	
//		 `order_id` bigint(20) NOT NULL COMMENT '订单id',
		String userId = order.getUserId();
//		查询购物车数据
		List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(userId);
		String orderList="";
		double totalFee = 0.00;
		for (Cart cart : cartList) {
//			 `payment_type` varchar(1) COLLATE utf8_bin DEFAULT NULL COMMENT '支付类型，1、在线支付，2、货到付款',
//			  `receiver_area_name` varchar(100) COLLATE utf8_bin DEFAULT NULL COMMENT '收货人地区名称(省，市，县)街道',
//			  `receiver_mobile` varchar(12) COLLATE utf8_bin DEFAULT NULL COMMENT '收货人手机',
//			  `receiver` varchar(50) COLLATE utf8_bin DEFAULT NULL COMMENT '收货人',
//			  `source_type` varchar(1) COLLATE utf8_bin DEFAULT NULL COMMENT '订单来源：1:app端，2：pc端，3：M端，4：微信端，5：手机qq端',
//
			TbOrder tbOrder = new TbOrder();
			tbOrder.setPaymentType(order.getPaymentType());
			tbOrder.setReceiverAreaName(order.getReceiverAreaName());
			tbOrder.setReceiverMobile(order.getReceiverMobile());
			tbOrder.setReceiver(order.getReceiver());
			tbOrder.setSourceType(order.getSourceType());
			
//			 `order_id` bigint(20) NOT NULL COMMENT '订单id',
			long orderId = idWorker.nextId();
			tbOrder.setOrderId(orderId);
			orderList+=orderId+",";
			double payment =0.00;
			for(TbOrderItem orderItem:cart.getOrderItemList()) {
				payment+=orderItem.getTotalFee().doubleValue();
//				 保存订单项
//				   `id` bigint(20) NOT NULL,
//				   `order_id` bigint(20) NOT NULL COMMENT '订单id',
				orderItem.setId(idWorker.nextId());
				orderItem.setOrderId(orderId);
				orderItemMapper.insert(orderItem);
			}
			totalFee+=payment;
//			 `payment` decimal(20,2) DEFAULT NULL COMMENT '实付金额。精确到2位小数;单位:元。如:200.07，表示:200元7分',
//			当前订单的实付金额
			tbOrder.setPayment(new BigDecimal(payment));
//			 `status` varchar(1) COLLATE utf8_bin DEFAULT NULL COMMENT '状态：1、未付款，2、已付款，3、未发货，4、已发货，5、交易成功，6、交易关闭,7、待评价',
			tbOrder.setStatus("1");
// 			`create_time` datetime DEFAULT NULL COMMENT '订单创建时间',
//			 `update_time` datetime DEFAULT NULL COMMENT '订单更新时间',
			tbOrder.setCreateTime(new Date());
			tbOrder.setUpdateTime(new Date());
			
//			 `user_id` varchar(50) COLLATE utf8_bin DEFAULT NULL COMMENT '用户id',
			tbOrder.setUserId(userId);
//			  `buyer_rate` varchar(2) COLLATE utf8_bin DEFAULT NULL COMMENT '买家是否已经评价', 0未评价  1已评价
			tbOrder.setBuyerRate("0");
//			`seller_id` varchar(100) COLLATE utf8_bin DEFAULT NULL COMMENT '商家ID',
			tbOrder.setSellerId(cart.getSellerId());
			
			orderMapper.insert(tbOrder);
		}
		
//		添加一条支付日志数据
		TbPayLog payLog = new TbPayLog();
		payLog.setCreateTime(new Date());
		payLog.setOrderList(orderList.substring(0, orderList.length()-1)); //去除最后一个多余的逗号
		payLog.setOutTradeNo(idWorker.nextId()+"");
		payLog.setPayType(order.getPaymentType());
		payLog.setTotalFee((long) (totalFee*100));
		payLog.setTradeState("0");
		payLog.setUserId(userId);
		payLogMapper.insert(payLog);
		
		redisTemplate.boundHashOps("payLog").put(userId, payLog);
//		清空当前用户购物车
		redisTemplate.boundHashOps("cartList").delete(userId);
		
	}
	
	//按时间统计每个商家销售额
	public List<ChartsView> findAllByTimeAndSelled() {
		List<ChartsView> chartsList = new ArrayList<>();
		TbOrderExample example = new TbOrderExample();
		example.createCriteria().andStatusEqualTo("2")
				.andUpdateTimeGreaterThanOrEqualTo(FristMonth())
				.andUpdateTimeLessThanOrEqualTo(LastMonth());
		//查找筛选的已完成的订单
		List<TbOrder> list = orderMapper.selectByExample(example);
		//查找商户ID
		List<String> sellerIdList = orderMapper.selectSellerId();
		for (String sellerId : sellerIdList) {
			ChartsView chartsView = new ChartsView();
			//查找商户名
			TbSeller tbSeller = sellerMapper.selectByPrimaryKey(sellerId);
			String seller = tbSeller.getName();
			chartsView.setDateName(seller);
			chartsView.setExtraDate(sellerId);
			int count=0;
			String moneyType="";
			long[] arr = new long[6];
			for (TbOrder tbOrder : list) {
				if(tbOrder.getSellerId().equals(sellerId)) {
					componyTimeAndAddPrice(arr,tbOrder.getUpdateTime(),tbOrder.getPayment());
				}
			}
			moneyType = new StringBuffer().append(0).append(",")
					.append(Long.toString(arr[0])).append(",")
					.append(Long.toString(arr[0]+arr[1])).append(",")
					.append(Long.toString(arr[0]+arr[1]+arr[2])).append(",")
					.append(Long.toString(arr[0]+arr[1]+arr[2]+arr[3])).append(",")
					.append(Long.toString(arr[0]+arr[1]+arr[2]+arr[3]+arr[4])).append(",")
					.append(Long.toString(arr[0]+arr[1]+arr[2]+arr[3]+arr[4]+arr[5])).toString();
			chartsView.setDateParam(moneyType);
			
			chartsList.add(chartsView);
		}
		
		return chartsList;
	}
	//比较时间
	private void componyTimeAndAddPrice(long[] arr, Date paymentTime, BigDecimal money) {
		long fristMonth = FristMonth().getTime();
		long startTime = paymentTime.getTime();
		long fiveDay = 5*24*60*60*1000;
		//5天内
		if(fristMonth+fiveDay>startTime) {
			arr[0]+= money.longValue();
		//5-10天内
		}else if(fristMonth+fiveDay*2>startTime) {
			arr[1]+= money.longValue();
		//10-15天内	
		}else if(fristMonth+fiveDay*3>startTime) {
			arr[2]+= money.longValue();
		//15-20天内
		}else if(fristMonth+fiveDay*4>startTime) {
			arr[3]+= money.longValue();
		//20-25天内	
		}else if(fristMonth+fiveDay*5>startTime) {
			arr[4]+= money.longValue();
		//25-30天内	
		}else if(fristMonth+fiveDay*6>startTime) {
			arr[5]+= money.longValue();
		}
	}
	//上个月第一天
	private Date FristMonth() {
		Calendar fristmonth = Calendar.getInstance();
		fristmonth.add(Calendar.MONTH, -1);
		fristmonth.set(Calendar.DAY_OF_MONTH,1);
		fristmonth.set(Calendar.HOUR_OF_DAY, 0);
		fristmonth.set(Calendar.SECOND, 0);
		fristmonth.set(Calendar.MINUTE, 0);
		fristmonth.set(Calendar.MILLISECOND, 0);
		return fristmonth.getTime();
	}
	//上个月最后一天
	private Date LastMonth() {
		Calendar lastmonth = Calendar.getInstance();
		lastmonth.set(Calendar.DAY_OF_MONTH,1);
		lastmonth.add(Calendar.DATE, -1);
		lastmonth.set(Calendar.HOUR_OF_DAY, 23);
		lastmonth.set(Calendar.SECOND, 59);
		lastmonth.set(Calendar.MINUTE, 59);
		lastmonth.set(Calendar.MILLISECOND, 59);
		return lastmonth.getTime();
	}
	
	@Autowired
	private TbGoodsMapper goodsMapper;
	
	
	/*
	 * SELECT order_id FROM tb_order WHERE `status`='2'
		SELECT goods_id,total_fee FROM tb_order_item WHERE order_id='3';
		SELECT category3_id FROM tb_goods WHERE id='149187842867954'
	 * */
	//统计每类商品的销售额 
	public ChartsView findPayGoods() {
		TbOrderExample orderExample = new TbOrderExample();
		orderExample.createCriteria().andStatusEqualTo("2");
		StringBuilder dateName = new StringBuilder();
		StringBuilder dateParam = new StringBuilder();
		Map<Long,Double> map = new HashMap<>();
		List<TbOrder> orderList = orderMapper.selectByExample(orderExample);
		for (TbOrder tbOrder : orderList) {
			Long orderId = tbOrder.getOrderId();
			TbOrderItemExample example = new TbOrderItemExample();
			example.createCriteria().andOrderIdEqualTo(orderId);
			List<TbOrderItem> orderItemlist = orderItemMapper.selectByExample(example);
			for (TbOrderItem tbOrderItem : orderItemlist) {
				Long goodsId = tbOrderItem.getGoodsId();
				TbGoods tbGoods = goodsMapper.selectByPrimaryKey(goodsId);
				BigDecimal totalFee = tbOrderItem.getTotalFee();
				Long category3Id = tbGoods.getCategory3Id();
				if(map.containsKey(category3Id)) {
					Double fee = map.get(tbGoods.getCategory3Id());
					fee+=totalFee.doubleValue();
					map.put(category3Id, fee);
				}else {
					map.put(category3Id, Double.parseDouble(totalFee.toString()));
				}
			}
		}
		ChartsView chartsView = new ChartsView();
		Set<Long> keySet = map.keySet();
		for (Long id : keySet) {
			dateName.append(id+",");
			dateParam.append(getTwoDecimal(map.get(id))+",");
		}
		chartsView.setDateName(dateName.toString().substring(0, dateName.lastIndexOf(",")));
		chartsView.setDateParam(dateParam.toString().substring(0, dateParam.lastIndexOf(",")));
 		return chartsView;
	}
	
	//保留两位小数并四舍五入
	private Double getTwoDecimal(Double num) {
        DecimalFormat dFormat = new DecimalFormat("#.00");
        String yearString = dFormat.format(num);
        Double temp= Double.valueOf(yearString);
        return temp;
   }

	//统计一年销售额（销售量）的趋势图
	public ChartsView findPayOfYear() {
		StringBuilder dateName = new StringBuilder();
		StringBuilder dateParam = new StringBuilder();
		StringBuilder extraDate = new StringBuilder();
		Map<String,Map> param = new HashMap<>();
		List<TbOrder> orderList = orderMapper.selectByStatusAndPayment("2","2018-01-01","2018-12-31");
		for (TbOrder tbOrder : orderList) {
			//数据的当前月份
			Date current = tbOrder.getPaymentTime();
			
			
			int month = getCurrentMonth(current);
			Long orderId = tbOrder.getOrderId();
			TbOrderItemExample example = new TbOrderItemExample();
			example.createCriteria().andOrderIdEqualTo(orderId);
			List<TbOrderItem> orderItemList = orderItemMapper.selectByExample(example);
			for (TbOrderItem tbOrderItem : orderItemList) {
				double price = tbOrderItem.getPrice().doubleValue();
				Integer num = tbOrderItem.getNum();
				//将数据组合到一起
				groupParam(param,month,price,num);
			}
		}
		//获取平均销售值并合并数据
		getAverageAndGroup(param,dateName,dateParam,extraDate);
		ChartsView chartsView =new ChartsView();
		chartsView.setDateName(dateName.toString().substring(0, dateName.lastIndexOf(",")));
		chartsView.setDateParam(dateParam.toString().substring(0, dateParam.lastIndexOf(",")));
		chartsView.setExtraDate(extraDate.toString().substring(0, extraDate.lastIndexOf(",")));
		return chartsView;
	}
	private void getAverageAndGroup(Map<String, Map> param, StringBuilder dateName, StringBuilder dateParam,
			StringBuilder extraDate) {
		Map price = param.get("dateName");
		Map num = param.get("dateParam");
		for(int i = 1; i<=12;i++) {
			Double p = (Double) price.get(i);
			Integer n = (Integer) num.get(i);
			dateName.append(p+",");
			dateParam.append(n+",");
			//获取当月的平均销售额
			extraDate.append(getTwoDecimal(p/lastDay(i))+",");
		}
	}
	//获取指定月份的天数
	private int lastDay(int month) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MONTH, month+1);
		calendar.set(Calendar.DAY_OF_MONTH,0);
		return calendar.get(Calendar.DAY_OF_MONTH);
	}
	//将数据组合到一起
	private void groupParam(Map<String, Map> param, int month, double price, Integer num) {
		//销售额
		if(param.containsKey("dateName")) {
			Map<Integer,Double> map = param.get("dateName");
			if(map.containsKey(month)) {
				Double total = map.get(month);
				total+=price;
				map.put(month, total);
			}else {
				map.put(month, price);
			}
		}else {
			Map<Integer,Double> map = new HashMap<>();
			map.put(month, price);
			param.put("dateName", map);
		}
		//销售量
		if(param.containsKey("dateParam")) {
			Map<Integer,Integer> map = param.get("dateParam");
			if(map.containsKey(month)) {
				Integer total = map.get(month);
				total+=num;
				map.put(month, total);
			}else {
				map.put(month, num);
			}
		}else {
			Map<Integer,Integer> map = new HashMap<>();
			map.put(month, num);
			param.put("dateParam", map);
		}
		
	}

	//当前输入的日期月份
	private int getCurrentMonth(Date date) {
			
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			int i = calendar.get(Calendar.MONTH)+1;
			return i;
	}
}
