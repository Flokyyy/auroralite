package de.flokyy.auroralite.listener;

import java.awt.Color;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import de.flokyy.auroralite.mysql.MySQLStatements;
import de.flokyy.auroralite.solana.CheckTransaction;
import de.flokyy.auroralite.solana.CheckWalletBalance;
import de.flokyy.auroralite.solana.NewWallet;
import de.flokyy.auroralite.solana.SolanaTransfer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CommandListener extends ListenerAdapter {

	public static ArrayList blocked = new ArrayList();
	
	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		try {
			if (event.isAcknowledged()) {
				return;
			}

			if (event.getName().equalsIgnoreCase("setup")) {
				event.deferReply(true).queue(); // Let the user know we received the command before doing anything else
				InteractionHook hook = event.getHook();
				hook.setEphemeral(true);

				if(event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
				OptionMapping roleObject = event.getOption("role");
				if (roleObject == null) {
					hook.sendMessage("Role not provided for some reason").queue();
					return;
				}


				OptionMapping amountObject = event.getOption("amount");
				if (amountObject == null) {
					hook.sendMessage("Amount not provided for some reason").queue();
					return;
				}

				OptionMapping walletObject = event.getOption("vault-wallet");
				if (walletObject == null) {
					hook.sendMessage("Wallet not provided for some reason").queue();
					return;
				}

				if (MySQLStatements.serverExists(event.getGuild().getId())) { // Check if server has already been
																						// setup
					hook.sendMessage("This server has already been setup!").queue();
					return;
				}

				Role role = roleObject.getAsRole();
		
				String amountString = amountObject.getAsString();
				String vaultwallet = walletObject.getAsString();

				Double amountDouble = 0.0;
				try {
					amountDouble = Double.parseDouble(amountString);
				} catch (Exception e) {
					hook.sendMessage("The given amount is invaild.").queue();
					return;
				}
				
				if(amountDouble == 0.0 || amountDouble == 0 || amountDouble < 0.0) {
					hook.sendMessage("The given amount is invaild.")
					.queue();
					return;
				}
				
				if(amountDouble >= 0) {
					MySQLStatements.createNewServer(event.getGuild().getId(), amountDouble, role.getIdLong(), vaultwallet);
					hook.sendMessage("Your server has successfully been setup for AuroraLITE. Subscription-Price: " + amountDouble + " | Role: " + role.getAsMention() + " | Vault-Wallet: " + vaultwallet)
					.queue();
				}

			}
				else {
					hook.sendMessage("You need administrator permission in order to perform this command").queue();
					return;
				}
			}
			
			if (event.getName().equalsIgnoreCase("update")) {
				event.deferReply(true).queue(); // Let the user know we received the command before doing anything else
				InteractionHook hook = event.getHook();
				hook.setEphemeral(true);

				if(event.getMember().hasPermission(Permission.ADMINISTRATOR)) {

				OptionMapping amountObject = event.getOption("amount");
				if (amountObject == null) {
					hook.sendMessage("Amount not provided for some reason").queue();
					return;
				}

				if (!MySQLStatements.serverExists(event.getGuild().getId())) { // Check if server has not already been setup
					hook.sendMessage("This server hasn't been setup yet!").queue();
					return;
				}
		
				String amountString = amountObject.getAsString();
				Double amountDouble = 0.0;
				try {
					amountDouble = Double.parseDouble(amountString);
				} catch (Exception e) {
					hook.sendMessage("The given amount is invaild.").queue();
					return;
				}
				
				if(amountDouble == 0.0 || amountDouble == 0 || amountDouble < 0.0) {
					hook.sendMessage("The given amount is invaild.")
					.queue();
					return;
				}
				
				if(amountDouble >= 0) {
					MySQLStatements.updateCollectionAmount(event.getGuild().getId(), amountDouble);
					hook.sendMessage("Your subscription price has successfully been updated to ``" + amountDouble + "`` SOL")
					.queue();
				}

			}
				else {
					hook.sendMessage("You can't perform this command.").queue();
					return;
				}
			}
		} catch (Exception e) {
			System.out.println("AuroraLITE Error: " + e.getMessage());
			return;
		}
	}
	 

	@Override
	public void onButtonInteraction(ButtonInteractionEvent event) {
		if (event.getComponentId().equals("Cancel")) {

			event.deferReply(true).queue(); // Let the user know we received the command before doing anything else
			InteractionHook hook = event.getHook(); // This is a special webhook that allows you to send messages
													// without having permissions in the channel and also allows
													// ephemeral messages
			hook.setEphemeral(true);

			
			if(blocked.contains(event.getMember())) {
				hook.sendMessage("You already canceled this request.").queue();
				return;
			}
			
			if(!blocked.contains(event.getMember())) {
			   blocked.add(event.getMember()); // Adding user to the blocked arraylist for later usage
			   hook.sendMessage("Request has been cancelled. This channel will get removed in some seconds.").queue();
			}
			
			event.getChannel().delete().queueAfter(4, TimeUnit.SECONDS);
		}
		

		if (event.getComponentId().equals("Start")) {
			event.deferReply(true).queue(); // Let the user know we received the command before doing anything else
			InteractionHook hook = event.getHook(); // This is a special webhook that allows you to send messages
													// without having permissions in the channel and also allows
													// ephemeral messages
			hook.setEphemeral(true);
			
			if(!blocked.contains(event.getMember())) {
				String uuid = java.util.UUID.randomUUID().toString().replace("-", "");
				NewWallet myThread = new NewWallet(uuid);

				String wallet = myThread.run(uuid);
				myThread.stop();

				if (wallet.equalsIgnoreCase("ERROR")) { // If wallet creation failed

					EmbedBuilder builder = new EmbedBuilder();
					builder.setTitle("ERROR");

					builder.setDescription(":exclamation: I'm sorry " + event.getMember().getAsMention()
							+ ", but an error occured. Please try again!");
					builder.setColor(Color.red);

					builder.setFooter("Powered by AuroraLITE",
							"https://media.discordapp.net/attachments/1041799650623103007/1043166916941975552/logo.png?width=676&height=676");
					builder.setTimestamp(OffsetDateTime.now(Clock.systemDefaultZone()));
					TextChannel tc = event.getTextChannel();
					tc.sendMessageEmbeds(builder.build()).queue();
					return;
				}

				hook.sendMessage("Great! We will now continue with the payment.").queue();
				EmbedBuilder builder = new EmbedBuilder();
				builder.setTitle("SUBSCRIPTION PAYMENT");

				builder.setDescription("Please send the subscription payment to: ``" + wallet
						+ "``\n\nMore information is displayed down below :point_down: \nMake sure the amount you send is **equal** or **above** to the provided total amount. \n\nAuroraLITE __automatically__ detects if funds have been deposited and will post further information if done.");

				Double totalAmount = MySQLStatements.getServerSubscriptionAmount(event.getGuild().getId());
				builder.addField("Total Amount", "" + totalAmount + " SOL", false);
				builder.addField("Project", "``" + event.getGuild().getName() + "``", false);
			
				TextChannel tc = event.getTextChannel();

				builder.setColor(new Color(144, 238, 144));
				builder.setFooter("Powered by AuroraLITE",
						"https://media.discordapp.net/attachments/1041799650623103007/1043166916941975552/logo.png?width=676&height=676");
				builder.setTimestamp(OffsetDateTime.now(Clock.systemDefaultZone()));
				
				tc.sendMessageEmbeds(builder.build())
						.setActionRow(Button.danger("Cancel", "Cancel Request")).queue();

				Timer timer1112 = new Timer();
				TimerTask hourlyTask1112 = new TimerTask() {

					int tries = 20; // Max tries

					@Override
					public void run() {
						try {
							tries--;

							if (event.getChannel() == null) { // In case the channel is deleted
								cancel();
								return;
							}
							
							if (tries <= 0) {
								EmbedBuilder builder = new EmbedBuilder();
								builder.setTitle("PAYMENT TIMEOUT");
								builder.setDescription("The Subscription Payment was not sent within ``7 minutes`` leading to a cancellation. Please try again.");

								builder.setColor(Color.red);
								builder.setFooter("Powered by AuroraLITE",
										"https://media.discordapp.net/attachments/1041799650623103007/1043166916941975552/logo.png?width=676&height=676");
								builder.setTimestamp(OffsetDateTime.now(Clock.systemDefaultZone()));

								if (event.getChannel() != null) {
									TextChannel tc = event.getTextChannel();
									tc.sendMessageEmbeds(builder.build()).queue();
								}
								cancel(); // Cancel Timer
								if (event.getChannel() != null) {
									
									try {
									event.getChannel().delete().queueAfter(40, TimeUnit.SECONDS); // Delete channel after 3 minutes
									}
									catch(Exception e) {
										
									}
								}
								return;

							} else {
								CheckWalletBalance myThread = new CheckWalletBalance(wallet);

								String balance = myThread.run(wallet).replace("SOL", "").replaceAll("\\s+", "");
								myThread.stop();

								Double walletBalance = Double.parseDouble(balance);

								if (walletBalance >= totalAmount) { // If wallet balance >= the set total amount fetched from the database
									try {
										EmbedBuilder builder = new EmbedBuilder();
										builder.setTitle("SUBSCRIPTION PAID");
										builder.setDescription("We successfully received your payment "
												+ event.getMember().getAsMention()
												+ ". \nThis channel will get deleted automatically in ``3 minutes`` \n\nThank you for using AuroraLITE <3");

										builder.setColor(Color.green);
										builder.setFooter("Powered by AuroraLITE",
												"https://media.discordapp.net/attachments/1041799650623103007/1043166916941975552/logo.png?width=676&height=676");
										builder.setTimestamp(OffsetDateTime.now(Clock.systemDefaultZone()));
										TextChannel tc = event.getTextChannel();
										tc.sendMessageEmbeds(builder.build()).queue();

										cancel();

										try {
											String uuid = java.util.UUID.randomUUID().toString().replace("-", "");
											long unixTime = Instant.now().getEpochSecond();
											MySQLStatements.createNewEntry(uuid, event.getMember().getId(), unixTime, totalAmount, event.getGuild().getId()); //Saving the paid royalty in the database
										}
										catch(Exception e) {
											String uuid = java.util.UUID.randomUUID().toString().replace("-", "");
											long unixTime = Instant.now().getEpochSecond();
											MySQLStatements.createNewEntry(uuid, event.getMember().getId(), unixTime, totalAmount, event.getGuild().getId()); //Trying again
										}
										
										Double amount = walletBalance - 0.001; // Amount on the wallet - network fee
																				// otherwise the transaction will fail

										Long roleID = MySQLStatements.getServerAssignRole(event.getGuild().getId());
										Role role = event.getGuild().getRoleById(roleID);
										if (role != null) {
											if (!event.getMember().getRoles().contains(role)) {
												event.getGuild().addRoleToMember(event.getMember(), role).queue();
											}
										}

										String vaultWallet = MySQLStatements.getServerVaultWallet(event.getGuild().getId()); // get
																																// vault
																																// from
																																// the
																																// given
																																// project
																																// from
																																// the
																																// database
										SolanaTransfer transfer = new SolanaTransfer(uuid, vaultWallet, amount); // Transfer
																													// the
																													// SOL
																													// to
																													// the
																													// vault

										String transaction = transfer.run(uuid, vaultWallet, amount);
										transfer.stop();

										String[] parts = transaction.split(":");
										String part1 = parts[1].replaceAll("\\s+", "");

										if (!part1.equalsIgnoreCase("ERROR")) {

											CheckTransaction check = new CheckTransaction(part1);

											String confirmation = check.run(part1);
											check.stop();

											EmbedBuilder builder1 = new EmbedBuilder();
											builder1.setTitle("PAYMENT DISTRIBUTED");
											builder1.setDescription(
													"We successfully sent the payment to the vault of the project. Save this transaction for potential later usage.");
											builder1.setColor(Color.green);
											builder1.setFooter("Powered by AuroraLITE",
													"https://media.discordapp.net/attachments/1041799650623103007/1043166916941975552/logo.png?width=676&height=676");
											builder1.setTimestamp(OffsetDateTime.now(Clock.systemDefaultZone()));
											
											tc.sendMessageEmbeds(builder1.build()).setActionRow(
													Button.link("https://solana.fm/tx/" + part1, "SolanaFM Transaction"))
													.queue();
										} else {
											System.out.println("Error when trying to send the vault transaction for: " + vaultWallet + " User: " + event.getMember().getId());
											cancel();
										}

										if (event.getChannel() != null) {
											event.getChannel().delete().queueAfter(3, TimeUnit.MINUTES); // Delete channel	
										}
										return;

									} catch (Exception e) {
										cancel(); // Cancel Timer
									}
								}
							}

						} catch (Exception e) {
							System.out.println("[LOG] | Error " + e.getMessage());
							return;
						}
					}
				};
				timer1112.schedule(hourlyTask1112, 0l, 20000);
			}
			else {
				event.reply("You already canceled this payment process!").queue();
				return;
			}
		}

		if (event.getComponentId().equals("Pay")) {
			event.deferReply(true).queue(); // Let the user know we received the command before doing anything else
			InteractionHook hook = event.getHook();
			hook.setEphemeral(true);

			if (!MySQLStatements.serverExists(event.getGuild().getId())) { // Check if server has already been setup
				hook.sendMessage(
						"This server hasnt been setup for AuroraLITE yet! Please tell the owner to use the /setup command of AuroraLITE to continue.")
						.queue();
				return;
			}
			
			Long roleID = MySQLStatements.getServerAssignRole(event.getGuild().getId());
			try {
			
				Role role = event.getGuild().getRoleById(roleID);
				if(event.getMember().getRoles().contains(role)) { // Checking if the user already has the subscription role
					hook.sendMessage("You can't start the payment process as you already have the subscription role.").queue();
					return;
				}
			}
			catch(Exception e) {
				hook.sendMessage("Subscription role is invaild. Please contact the server owner.").queue();
				return;
			}

			for (TextChannel tc : event.getGuild().getTextChannels()) {
				if (tc.getTopic() != null) {
					if (tc.getTopic().equalsIgnoreCase(event.getMember().getId())) { // Checking for existing tickets
																						// with the topic.
						hook.sendMessage(
								"You already got a subscription payment request! Please __close__ or __finish__ the other one before.")
								.setEphemeral(true).queue();
						return;
					}
				}
			}

			if(blocked.contains(event.getMember())) {
				blocked.remove(event.getMember());
			}
			
			
			event.getGuild().createTextChannel("subscription-payment").setTopic(event.getMember().getId()) // Sets channel
																										// topic to
																										// users id to
																										// check later
																										// that they
																										// don't open
																										// multiple
			
			.addPermissionOverride(event.getMember(), EnumSet.of(Permission.VIEW_CHANNEL), null)
			.addPermissionOverride(event.getGuild().getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL))
					
					.queue(channel -> {
						TextChannel txtChannel = event.getJDA().getTextChannelById(channel.getIdLong());

						EmbedBuilder builder = new EmbedBuilder();
						builder.setTitle("INFORMATION");

						builder.setDescription("Hey there, " + event.getMember().getAsMention()
								+ "\nThank you for choosing to pay the monthly subscription. \nIn our next step we will start with the payment process. \n\nTo continue please click ``Start``.");
						builder.setColor(new Color(144, 238, 144));
						builder.setThumbnail(event.getUser().getAvatarUrl());
						builder.setFooter("Powered by AuroraLITE",
								"https://media.discordapp.net/attachments/1041799650623103007/1043166916941975552/logo.png?width=676&height=676");
						builder.setTimestamp(OffsetDateTime.now(Clock.systemDefaultZone()));
						
						List<Button> buttons = new ArrayList<Button>();
						buttons.add(Button.danger("Cancel", "Cancel the payment"));
						buttons.add(Button.secondary("Start", "Start"));
					
						 
						
						txtChannel.sendMessageEmbeds(builder.build()).setActionRow(buttons).queue();
					
						builder.clear();

						hook.sendMessage("Request sent. Please check: " + txtChannel.getAsMention()).setEphemeral(true)
								.queue();

					});
		}
	}

	
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		try {
			String msg = event.getMessage().getContentDisplay();
			if (msg.equalsIgnoreCase("+setup")) {
				event.getMessage().delete().queue();
				if (event.getMember().hasPermission(Permission.MANAGE_SERVER)) {
					EmbedBuilder builder = new EmbedBuilder();
					builder.setTitle("SUBSCRIPTION PAYMENT");

					builder.setDescription("Please click on the button to pay the subscription fee for ``"
							+ event.getGuild().getName() + "``");
					builder.setColor(new Color(144, 238, 144));
					builder.setFooter("Powered by AuroraLITE",
							"https://media.discordapp.net/attachments/1041799650623103007/1043166916941975552/logo.png?width=676&height=676");

					event.getChannel().sendMessageEmbeds(builder.build())
							.setActionRow(Button.primary("Pay", "Pay Subscription")).queue();

				}
			}

		} catch (Exception e) {
			System.out.println("Exception: " + e.getMessage());
		}
	}
}
