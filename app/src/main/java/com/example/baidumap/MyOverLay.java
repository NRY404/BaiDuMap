package com.example.baidumap;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiSearch;

import overlayutil.overlayutil.PoiOverlay;

public class MyOverLay extends PoiOverlay {
    PoiSearch poiSearch;

    /**
     * 构造函数
     *
     * @param baiduMap 该 PoiOverlay 引用的 BaiduMap 对象
     */
    public MyOverLay(BaiduMap baiduMap) {
        super(baiduMap);
    }
    public boolean onPoiClick(int i){
        super.onPoiClick(i);
        PoiInfo poiInfo = getPoiResult().getAllPoi().get(i);
        poiSearch.searchPoiDetail(new PoiDetailSearchOption().poiUid(poiInfo.uid));
        return true;
    }
}
