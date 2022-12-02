package de.flokyy.auroralite.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.mysql.jdbc.ResultSetMetaData;

import de.flokyy.auroralite.AuroraLite;

public class MySQLStatements {

	public static boolean serverExists(String server) {
		try {
			ResultSet rs = AuroraLite.mysql.query("SELECT SERVER FROM auroraLite WHERE SERVER='" + server + "'");
			if (rs.next()) {
				if (rs.getString("SERVER") != null) {
					return true;
				}
			}
		} catch (SQLException localSQLException) {
		}
		return false;
	}
	
	public static void createNewServer(String server, Double amount, Long role, String vault) {
		AuroraLite.mysql.update("INSERT INTO auroraLite(SERVER) VALUES ('" + server + "')");
		AuroraLite.mysql.update("UPDATE auroraLite SET AMOUNT='" + amount + "'WHERE SERVER='" + server + "'");
		AuroraLite.mysql.update("UPDATE auroraLite SET ROLE='" + role + "'WHERE SERVER='" + server + "'");
		AuroraLite.mysql.update("UPDATE auroraLite SET VAULT='" + vault + "'WHERE SERVER='" + server + "'");
	}
	
	public static void createNewEntry(String uuid, String member, Long timestamp, Double amount, String server) {
		AuroraLite.mysql.update("INSERT INTO auroraLiteTransaction(UUID) VALUES ('" + uuid + "')");
		AuroraLite.mysql.update("UPDATE auroraLiteTransaction SET MEMBER='" + member + "'WHERE UUID='" + uuid + "'");
		AuroraLite.mysql.update("UPDATE auroraLiteTransaction SET TIMESTAMP='" + timestamp + "'WHERE UUID='" + uuid + "'");
		AuroraLite.mysql.update("UPDATE auroraLiteTransaction SET SERVER='" + server + "'WHERE UUID='" + uuid + "'");
		AuroraLite.mysql.update("UPDATE auroraLiteTransaction SET AMOUNT='" + amount + "'WHERE UUID='" + uuid + "'");
	}
	
	public static String getServerVaultWallet(String server) {
		try {
			ResultSet rs = AuroraLite.mysql.query("SELECT VAULT FROM auroraLite WHERE SERVER='" + server + "'");
			if (rs.next()) {
				String s = rs.getString("VAULT");
				if (s != null)
					return s;
			}
		} catch (SQLException sQLException) {
		}
		return null;
	}
	

	
	public static Double getServerSubscriptionAmount(String server) {
		try {
			ResultSet rs = AuroraLite.mysql.query("SELECT AMOUNT FROM auroraLite WHERE SERVER='" + server + "'");
			if (rs.next()) {
				Double s = rs.getDouble("AMOUNT");
				if (s != null)
					return s;
			}
		} catch (SQLException sQLException) {
		}
		return null;
	}
	
	public static void updateCollectionAmount(String server, Double amount) {
		AuroraLite.mysql.update("UPDATE auroraLite SET AMOUNT='" + amount + "'WHERE SERVER='" + server + "'");
	}
	
	public static ArrayList getAllTransactions() {
		try {
			ArrayList<String> uuids = new ArrayList<String>();
			ResultSet rs = AuroraLite.mysql.query("SELECT UUID FROM auroraLiteTransaction");
			while (rs.next()) {
				String s = rs.getString("UUID");
				if (s != null) {
					uuids.add(s);
				}
			}
			return uuids;
		} catch (SQLException sQLException) {
		}
		return null;
	}
	
	public static Long getTimeStampFromUUID(String uuid) {
		try {
			ResultSet rs = AuroraLite.mysql.query("SELECT TIMESTAMP FROM auroraLiteTransaction WHERE UUID='" + uuid + "'");
			if (rs.next()) {
				Long s = rs.getLong("TIMESTAMP");
				if (s != null)
					return s;
			}
		} catch (SQLException sQLException) {
		}
		return null;
	}
	
	public static void removeEntry(String id) {
		try {
			AuroraLite.mysql.update("DELETE FROM auroraLiteTransaction WHERE UUID='" + id + "'");
		} catch (Exception e) {
			System.out.println("Error when trying to delete " + e.getMessage());
		}
}
	
	public static String getServerFromUUID(String uuid) {
		try {
			ResultSet rs = AuroraLite.mysql.query("SELECT SERVER FROM auroraLiteTransaction WHERE UUID='" + uuid + "'");
			if (rs.next()) {
				String s = rs.getString("SERVER");
				if (s != null)
					return s;
			}
		} catch (SQLException sQLException) {
		}
		return null;
	}
	
	public static String getMemberFromUUID(String uuid) {
		try {
			ResultSet rs = AuroraLite.mysql.query("SELECT MEMBER FROM auroraLiteTransaction WHERE UUID='" + uuid + "'");
			if (rs.next()) {
				String s = rs.getString("MEMBER");
				if (s != null)
					return s;
			}
		} catch (SQLException sQLException) {
		}
		return null;
	}
	
	
	public static Long getServerAssignRole(String server) {
		try {
			ResultSet rs = AuroraLite.mysql.query("SELECT ROLE FROM auroraLite WHERE SERVER='" + server + "'");
			if (rs.next()) {
				Long s = rs.getLong("ROLE");
				if (s != null)
					return s;
			}
		} catch (SQLException sQLException) {
		}
		return null;
	}
}
