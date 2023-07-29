package com.zj.demoproxy.template.handler;

import com.alibaba.fastjson.JSONObject;
import com.zj.demoproxy.template.BaseTemplate;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.Super;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author 19242
 */
public class BaseTemplateImpl {

    @RuntimeType
    public <T> List<T> getList(int size, Supplier<T> func) {
        List<T> resultList = new ArrayList<>();
        if (0 == size) {
            return resultList;
        }
        for (int i = 0; i < size; i++) {
            resultList.add(func.get());
        }
        return resultList;
    }

    @RuntimeType
    public <T> T toObject(String jsonStr, @Super BaseTemplate<T> baseCustom) {
        return JSONObject.parseObject(jsonStr, baseCustom.getTypeClz());
    }
}