app.controller("echartsController",function($scope, $location, echartsService){ 
	//

	 var dateArray ={'dateParam':[],'dateName':[],'extraDate':[],'month':'',length:0};
	 $scope.itemCatList = [];
	 $scope.flag=false;
	 $scope.right=true;
	 $scope.left=true;
	 var count = 3;
	 
	 $scope.checkCharts=function(num){
		 count+=num;
		 if(count>=dateArray['length']){
			 $scope.right=false;
			 count = dateArray['length']-1;
		 }else{
			 $scope.right=true;
		 }
		 if(count<=0){
			 $scope.left=false;
		 }else{
			 $scope.left=true;
		 }
		 findSelleds(); 
	 }
	 function findSelleds(){
		 echartsService.findAllByTimeAndSelled().success(function(response){
			 $scope.list = response;
			 $scope.flag=true;
			 dateArray['dateName'] = response[count].dateName;
			 dateArray['month'] = response[count].month;
			 dateArray['dateParam'] = response[count].dateParam.split(',');
			 dateArray['length']=response.length;
			 charts();
		 })
	 }
	 
	 function charts(){
		 var option = {
				 title : {
						text : dateArray['dateName']+'商家'+dateArray['month']+'月销售',
						x : 'center'
					},
					xAxis : {
						type : 'category',
						boundaryGap : false,
						data : [ '0', '5', '10', '15', '20', '25', '30' ]
					},
					yAxis : {
						type : 'value'
					},
					series : [ {//[ 0, 1932, 2901, 2934, 3290, 3330, 3520 ]
						data : dateArray['dateParam'],
						type : 'line',
						smooth : true,
						label : {
							normal : {
								show : true,
								position : 'top'
							}
						},
					} ]
		 }
		 var myChart = echarts.init(document.getElementById('container'),'macarons');  
         myChart.setOption(option)
	 }
	 
	 $scope.selectType=function(){
		 var type = $location.search()['typeId'];
		 if(type=='1'){
			//按时间统计每个商家销售额 
			 findSelleds();
		 }
		 if(type=='2'){
			 //统计每个商品分类下的商品数量
			 echartsService.findGoodsByCategoryId().success(function(response){
				 var id = response.dateName.split(",");
				 var num = response.dateParam.split(",");
				 for(var i=0; i<id.length;i++){
					 var date = {};//{value:335, name:'直接访问'}
					 dateArray['dateName'].push($scope.itemCatList[id[i]]);//['a','b','c']
					 date['value']=num[i];
					 date['name']=$scope.itemCatList[id[i]];
					 dateArray['dateParam'].push(date);
				 }
				 var option = {
						 title : {
						        text: '商品分类下的商品数量',
						        subtext: '纯属虚构',
						        x:'center'
						    },
						    tooltip : {
						        trigger: 'item',
						        formatter: "{a} <br/>{b} : {c} ({d}%)"
						    },
						    legend: {
						        orient: 'vertical',
						        left: 'left',//['直接访问','邮件营销','联盟广告','视频广告','搜索引擎']
						        data: dateArray['dateName']
						    },
						    series : [
						        {
						            name: '访问来源',
						            type: 'pie',
						            radius : '55%',
						            center: ['50%', '60%'],
						            data:dateArray['dateParam'],
						            itemStyle: {
						                emphasis: {
						                    shadowBlur: 10,
						                    shadowOffsetX: 0,
						                    shadowColor: 'rgba(0, 0, 0, 0.5)'
						                }
						            }
						        }
						    ]
				 }
				 var myChart = echarts.init(document.getElementById('container'),'macarons');  
		         myChart.setOption(option)
			 });
		 }
		 if(type=='3'){
			 //统计每类商品的销售额
			 echartsService.findPayGoods().success(function(response){
				 var id = response.dateName.split(',');
				 var date = response.dateParam.split(',');
				 for(var i=0; i<id.length; i++){
					 dateArray['dateName'].push($scope.itemCatList[id[i]]);
					 dateArray['dateParam'].push(date[i]);
				 }
				 var option = {
						 title : {
								text : '商品分类下的商品数量',
								x : 'center'
							},
						 color: ['#3398DB'],
						    tooltip : {
						        trigger: 'axis',
						        axisPointer : {            // 坐标轴指示器，坐标轴触发有效
						            type : 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
						        }
						    },
						    grid: {
						        left: '3%',
						        right: '4%',
						        bottom: '3%',
						        containLabel: true
						    },
						    xAxis : [
						        {
						            type : 'category',//['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
						            data : dateArray['dateName'],
						            axisTick: {
						                alignWithLabel: true
						            }
						        }
						    ],
						    yAxis : [
						        {
						            type : 'value'
						        }
						    ],
						    series : [
						        {
						            name:'直接访问',
						            type:'bar',
						            barWidth: '60%',//[10, 52, 200, 334, 390, 330, 220]
						            data:dateArray['dateParam']
						        }
						    ]
				 }
				 var myChart = echarts.init(document.getElementById('container'),'macarons');  
		         myChart.setOption(option)
			 })
		 }
		 if(type=='4'){
			 //统计一年销售额（销售量）的趋势图
			 echartsService.findPayOfYear().success(function(response){
				 var price = response.dateName.split(',');
				 var num = response.dateParam.split(',');
				 var avg = response.extraDate.split(',');
				 var totalFee = 0;
				 var totalNum = 0;
				 for(var i=0; i<price.length; i++){
					 dateArray['dateName'].push(parseFloat(price[i]));
					 totalFee+=parseFloat(price[i]);
					 dateArray['dateParam'].push(parseInt(num[i]));
					 totalNum+=parseFloat(num[i]);
					 dateArray['extraDate'].push(parseFloat(avg[i]));
					 
				 }
				 var option = {
						 title: {
						        text: '2017年销售总额:'+parseInt(totalFee)+',总销售量:'+totalNum,
						 },
						 tooltip: {
						        trigger: 'axis',
						        axisPointer: {
						            type: 'cross',
						            crossStyle: {
						                color: '#999'
						            }
						        }
						    },
						    toolbox: {
						        feature: {
						            dataView: {show: true, readOnly: false},
						            magicType: {show: true, type: ['line', 'bar']},
						            restore: {show: true},
						            saveAsImage: {show: true}
						        }
						    },
						    legend: {
						        data:['销售额','销售量','月平均销售额']
						    },
						    xAxis: [
						        {
						            type: 'category',
						            data: ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
						            axisPointer: {
						                type: 'shadow'
						            }
						        }
						    ],
						    yAxis: [
						        {
						            type: 'value',
						            name: '销售额',
						            interval: 2000,
						            axisLabel: {
						                formatter: '{value} 元'
						            }
						        },
						        {
						            type: 'value',
						            name: '销售量',
						            interval: 100,
						            axisLabel: {
						                formatter: '{value}'
						            }
						        }
						    ],
						    series: [
						        {
						            name:'销售额',
						            type:'bar',
						            data:dateArray['dateName']
						        },//[2.0, 4.9, 7.0, 23.2, 25.6, 76.7, 135.6, 162.2, 32.6, 20.0, 6.4, 3.3]
						        {
						            name:'销售量',
						            type:'bar',
						            yAxisIndex: 1,
						            data:dateArray['dateParam']
						        },//[2.6, 5.9, 9.0, 26.4, 28.7, 70.7, 175.6, 182.2, 48.7, 18.8, 6.0, 2.3]
						        {
						            name:'月平均销售额',
						            type:'line',
						            yAxisIndex: 1,
						            data:dateArray['extraDate']
						        }//[2.0, 2.2, 3.3, 4.5, 6.3, 10.2, 20.3, 23.4, 23.0, 16.5, 12.0, 6.2]
						    ]
				 }
				 var myChart = echarts.init(document.getElementById('container'),'macarons');  
		         myChart.setOption(option) 
			 })
		 }
	 }
	 
	 $scope.findItemCat=function(){
		 echartsService.findAllItemCat().success(function(response){
			 //response[i]={"id":1,"name":"图书、音像、电子书刊","parentId":0,"typeId":35}
			 for(var i=0; i<response.length;i++){
				$scope.itemCatList[response[i].id]=response[i].name;//$scope.itemCat[1]="图书、音像、电子书刊"
			 }
		 });
	 }
	
	
})