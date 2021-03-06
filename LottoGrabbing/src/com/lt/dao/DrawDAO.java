package com.lt.dao;

import java.util.List;

import org.framework.support.hibernate.GenericHibernateDao;

import com.ct.lk.domain.Draw;

public class DrawDAO {
	
	private GenericHibernateDao genericHibernateDao;

	public void setGenericHibernateDao(GenericHibernateDao genericHibernateDao) {
		this.genericHibernateDao = genericHibernateDao;
	}
    
    public List<Draw> getCountOfStatusL(String market, String gameCode, String date) {
    	StringBuffer checkSql = new StringBuffer();
    	checkSql.append("Select * from draw ");
		checkSql.append("where draw_date = '" + date + "' ");
		checkSql.append("and begin_time < CURRENT_TIMESTAMP ");
		checkSql.append("and game_code = '" + gameCode + "' ");
		checkSql.append("and market = '" + market + "' ");
		checkSql.append("and status = 'L' ");
		
		return genericHibernateDao.findBySql(Draw.class, checkSql.toString());
	}
	
	public List<Draw> selectByDrawNumberAndMarket(String market, String drawNumber, String gameCode) {
		StringBuffer checkSql = new StringBuffer();
		checkSql.append("Select * from draw ");
		checkSql.append("where draw_number = '" + drawNumber + "' ");
		checkSql.append("and game_code = '" + gameCode + "' ");
		checkSql.append("and market = '" + market + "' ");
		
		return genericHibernateDao.findBySql(Draw.class, checkSql.toString());
	}
	
	public List<Draw> getDrawNum(String gameCode ,String market,String nowNumber) {
		String sql = "SELECT * from draw where game_code='"+ gameCode +"' and market='" + market
				+ "' and draw_number ='" + nowNumber + "' and result is NULL";
		List<Draw> drawlist = genericHibernateDao.findBySql(Draw.class, sql);
		return drawlist;
	}
    
    public List<Draw> getDrawNumList(String gameCode ,String market,String startNumber,String endNumber) {
	   String sql = "SELECT * from draw where game_code='"+ gameCode +"' and market='" + market
			+ "' and result is NULL and draw_number between '" + startNumber + "' and '" + endNumber 
			+ "' and draw_number != '" + endNumber  
			+ "' order by draw_number DESC";
	   List<Draw> drawlist = genericHibernateDao.findBySql(Draw.class, sql);
	   return drawlist;
	}
    
    public void updateDrawResult(String gameCode ,String market,String drawNumber,String result) {
    	String sql = "UPDATE draw SET result = '"+result+"' where draw_number ='" + drawNumber + "' and "
		   		+ "game_code='"+gameCode+"' and market='"+ market +"'";		   
    	genericHibernateDao.executeSql(sql);
	}
}
