package com.kpfactors.external.workflow;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

public class GetNotificationList extends AbstractMediator {
	private static final Log log = LogFactory.getLog(GetNotificationList.class);
	private PreparedStatement ps;
	private ResultSet rs;
	private Connection con;
	private JSONArray resultArray;
	private JSONObject resultObject;

	public boolean mediate(MessageContext context) {
		String user_id = context.getProperty("USER_ID").toString();
		resultArray = new JSONArray();
		resultObject = new JSONObject();
		try {
			Hashtable<String, String> environment = new Hashtable<>();
			environment.put("java.naming.factory.initial", "org.wso2.micro.tomcat.jndi.CarbonJavaURLContextFactory");
			Context initContext = new InitialContext(environment);
			DataSource ds = (DataSource) initContext.lookup("jdbc/WF_RDBMS");
			con = ds.getConnection();
			ps = con.prepareStatement("SELECT * FROM wf_alerts_notifications WHERE user_id =?");
			ps.setString(1, user_id);
			rs = ps.executeQuery();
			while (rs.next()) {
				resultObject = new JSONObject();
				resultObject.put("subject", rs.getString("subject"));
				resultObject.put("message", rs.getString("message"));
				resultObject.put("read_date", formatSQLTimeStamp(rs.getTimestamp("read_date")));
				resultObject.put("created_date", formatSQLTimeStamp(rs.getTimestamp("created_date")));
				resultObject.put("user_id", rs.getString("user_id"));
				resultObject.put("tenant_id", rs.getInt("tenant_id"));
				resultObject.put("type", rs.getString("type"));
				resultObject.put("channel", rs.getString("channel"));
				resultObject.put("r_status", rs.getString("r_status"));
				resultArray.put(resultObject);
			}
			context.setProperty("STATUS", "SUCCESS");
			context.setProperty("MESSAGE", resultArray.length() + " Records Fetched Sucessfully.");
			context.setProperty("RESULT", StringUtils.trimToEmpty(resultArray.toString()));
		} catch (Exception e) {
			log.error(e.getMessage());
			context.setProperty("STATUS", "ERROR");
			context.setProperty("MESSAGE", " Error in Fetching Roles." + e.getLocalizedMessage());
			context.setProperty("RESULT", StringUtils.trimToEmpty(resultArray.toString()));
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
			} catch (Exception e) {
			}
			try {
				if (ps != null) {
					ps.close();
				}
			} catch (Exception e) {
			}
			try {
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
			}
		}
		return true;
	}
	
	public static String formatSQLTimeStamp(java.sql.Timestamp dateTime)
	{
		String timestamp="";
		if(dateTime!=null)
		{
			Date date=new Date(dateTime.getTime());
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			timestamp = simpleDateFormat.format(date);
		}
		return timestamp;
	}
	
}
