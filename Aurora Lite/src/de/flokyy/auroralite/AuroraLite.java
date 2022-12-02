package de.flokyy.auroralite;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.security.auth.login.LoginException;

import org.apache.commons.collections4.functors.IfClosure;

import de.flokyy.auroralite.listener.CommandListener;
import de.flokyy.auroralite.mysql.MySQL;
import de.flokyy.auroralite.mysql.MySQLStatements;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class AuroraLite {

	public static boolean startup;
	public static JDABuilder builder;
	public static JDA jda;
	
	public static MySQL mysql;

	public MySQL getMySQL() {
	  return mysql;
	}

	public static void connectMySQL() {
	   mysql = new MySQL(MySQL.HOST, MySQL.DATABASE, MySQL.USER, MySQL.PASSWORD);
	   mysql.update("CREATE TABLE IF NOT EXISTS auroraLite(SERVER text, AMOUNT double, ROLE long, VAULT text)");
	   mysql.update("CREATE TABLE IF NOT EXISTS auroraLiteTransaction(UUID text, MEMBER text, TIMESTAMP long, AMOUNT double, SERVER text)");
	}
	
	public static void main(String[] args) {
		try {
			AuroraLite.connectMySQL(); // Connect to database
		} catch (Exception e) {
			System.out.println("" + e.getMessage());
			System.exit(1);
		}
		
		builder = JDABuilder.createDefault(""); // Discord Bot Secret Key
		builder.enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_INVITES);
		builder.setStatus(OnlineStatus.ONLINE);
		
		builder.addEventListeners(new CommandListener());

		OptionData option1 = new OptionData(OptionType.ROLE, "role", "choose the role that the user gets assigned once the subscription is paid", true);
		OptionData option2 = new OptionData(OptionType.STRING, "amount", "set the subscription amount (e.g 0.1 SOL)", true);
		OptionData option3 = new OptionData(OptionType.STRING, "vault-wallet", "set vault wallet where all funds are being sent to", true);
		
		List<CommandData> commandData = new ArrayList<>();
	    commandData.add(Commands.slash("setup", "Set up the Aurora LITE system on your discord").addOptions(option1, option2, option3));
	    
	    OptionData option4 = new OptionData(OptionType.STRING, "amount", "update the subscription price", true);
	    commandData.add(Commands.slash("update", "Update the subscription price").addOptions(option4));

		try {
			jda = builder.build(); // Start jda
		} catch (LoginException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

        jda.updateCommands().addCommands(commandData).queue();	

	
        // This timer functions is to check if a users subscription time frame (30d) is over so we remove their roles.
        Timer timer = new Timer();
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				try {
					ArrayList<String> uuids = MySQLStatements.getAllTransactions(); // Gets all uuids from the database
					Enumeration<String> uuidList = Collections.enumeration(uuids);
					while (uuidList.hasMoreElements()) { // Loop through all entries
						String uuid = uuidList.nextElement(); // This is the next uuid
						
						Long timestamp = MySQLStatements.getTimeStampFromUUID(uuid); // timestamp when the rank was assigned
					
						String serverID = MySQLStatements.getServerFromUUID(uuid); // Getting the old server id where the rank was assigned
						Long roleID = MySQLStatements.getServerAssignRole(serverID);
						
						LocalDate currentDate = LocalDate.now();
					    
						Date date =new java.util.Date((long)timestamp*1000);
						
						Instant instant = date.toInstant();
						ZonedDateTime zdt = instant.atZone(ZoneId.systemDefault());
						LocalDate oldDate = zdt.toLocalDate();

						long diff = ChronoUnit.DAYS.between(currentDate, oldDate);
						 
					    if (diff <= -30) { // Timestamp is older than 30 days
					    	  
					    	  try { // New try catch in case the role is invaild
					    		  Guild g = jda.getGuildById(serverID); //Getting the old server guild by using the saved server uuid
					    		  Role role = g.getRoleById(roleID); //Getting the subscription role from the database
					    		  
					    		  if(role == null) {
					    			MySQLStatements.removeEntry(uuid); // Removing entry from database
					    			System.out.println("Can't remove the role as it was deleted previously.");
					    			return;
					    		  }
					    		  if(role != null) {
					    			String memberID = MySQLStatements.getMemberFromUUID(uuid);
					    			Member m = g.getMemberById(memberID); 
					    			  
					    		  if(m.getRoles().contains(role)) { // If user still has the role
					    			g.removeRoleFromMember(m, role).queue(); // Removing the role
					    			System.out.println("Role from user: " + m.getId() + " has successfully been removed from guild: " + g.getName());
					    			MySQLStatements.removeEntry(uuid); // Removing entry from database
					    			return;
					    		  }
					    			  
					    		  if(!m.getRoles().contains(role)) { //If the user doesn't got the role anymore
					    			System.out.println("The user " + m.getId() + " does not have the role: " + role.getId() + " anymore so we can't remove it :(");
					    			MySQLStatements.removeEntry(uuid); // Removing entry from database
					    			return;
						    	}
					    	}	  
					    }
					   catch(Exception e) {
					    System.out.println("ERROR | Couldn't remove the role: " + roleID + " serverID: " + serverID);
					    continue;
					    }     
					   }
					}
			}
			catch (Exception e2) {
			  System.out.println("Error: " + e2.getMessage());
			  }
			}
		};
	  timer.schedule(timerTask, 0l, 600000); // Update every 10 minutes	
	  
	}
}