app.service("echartsService",function($http){
	
	
	this.findAllByTimeAndSelled=function(){
		return $http.get("../charts/findAllByTimeAndSelled");
	}
	this.findGoodsByCategoryId=function(){
		return $http.get("../charts/findGoodsByCategoryId");
	}
	this.findAllItemCat=function(){
		return $http.get("../charts/findAllItemCat");
	}
	this.findPayGoods=function(){
		return $http.get("../charts/findPayGoods");
	}
	this.findPayOfYear=function(){
		return $http.get("../charts/findPayOfYear");
	}

	
})