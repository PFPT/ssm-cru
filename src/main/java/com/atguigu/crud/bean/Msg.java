package com.atguigu.crud.bean;

import java.util.HashMap;
import java.util.Map;

/**
 * 返回Json数据
 * 通用的返回的类
 */
public class Msg {
    //状态码 100-成功 200-失败
    private  int code;
    //提示信息
    private String msg;

    //用户要返回给浏览器的数据
    private Map<String,Object> extendEmp=new HashMap<String, Object>();

    public static Msg success(){
        Msg resault=new Msg();
        resault.setCode(100);
        resault.setMsg("处理成功！");
        return resault;
    }
    public static Msg fail(){
        Msg resault=new Msg();
        resault.setCode(200);
        resault.setMsg("处理失败呜呜呜！");
        return resault;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Map<String, Object> getExtend() {
        return extendEmp;
    }

    public void setExtend(Map<String, Object> extend) {
        this.extendEmp = extend;
    }

    public Msg add(String key, Object value) {
        this.getExtend().put(key,value);
        return this;
    }
}
