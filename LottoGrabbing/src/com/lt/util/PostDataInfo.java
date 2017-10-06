package com.lt.util;

public class PostDataInfo {
	private String __EVENTTARGET;
	private String __VIEWSTATE;
	private String __EVENTVALIDATION;
	
	
	public String get__EVENTTARGET() {
		return __EVENTTARGET;
	}
	public void set__EVENTTARGET(String __EVENTTARGET) {
		this.__EVENTTARGET = __EVENTTARGET;
	}
	
	public String get__VIEWSTATE() {
		return __VIEWSTATE;
	}
	public void set__VIEWSTATE(String __VIEWSTATE) {
		this.__VIEWSTATE = __VIEWSTATE;
	}
	
	public String get__EVENTVALIDATION() {
		return __EVENTVALIDATION;
	}
	public void set__EVENTVALIDATION(String __EVENTVALIDATION) {
		this.__EVENTVALIDATION = __EVENTVALIDATION;
	}
	
	public String toString() {
	
		String str = "{__EVENTTARGET:"+__EVENTTARGET+"},{__VIEWSTATE:"+__VIEWSTATE+"},{__EVENTVALIDATION:"+__EVENTVALIDATION+"}";
		System.out.println(str);
		
		return str;
	}
}
