package de.flokyy.auroralite.solana;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class SolanaTransfer extends Thread {

	/**
	 * Constructor for creating a new SolanaTransfer object.
	 * @param sender the sender's wallet file name.
	 * @param receipt the recipient's wallet address.
	 * @param amount the amount to transfer.
	*/
	public SolanaTransfer(String sender, String receipt, Double amount) {
	    // TODO Auto-generated constructor stub
	}

	/**
	 * Runs the Solana transfer command to transfer Solana tokens from the sender to the recipient.
	 * @param sender the sender's wallet file name.
	 * @param receipt the recipient's wallet address.
	 * @param amount the amount to transfer.
	 * @return the transaction signature if successful, or an error message if an exception occurs.
	*/
	public static String run(String sender, String receipt, Double amount) {
	    String s = "";
	    Process p;
	    String balance = "";

	    ArrayList list = new ArrayList<>();
	    try {
		// Executes the Solana transfer command with the provided parameters.
		p = Runtime.getRuntime().exec("solana transfer --from /home/Aurora/wallets/" + sender + ".json " + receipt + " " + amount + " --allow-unfunded-recipient --url https://api.mainnet-beta.solana.com --fee-payer /home/Aurora/wallets/" + sender + ".json --no-wait");

		// Reads the output of the command and stores it in an ArrayList.
		BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
		p.waitFor(1, TimeUnit.MINUTES);
		while ((s = br.readLine()) != null) {
		    list.add(s);
		}

		// Extracts the transaction signature from the output of the command.
		for (int i = 0; i < list.size(); i++) {
		    String pubKey = (String) list.get(i);
		    if(pubKey.contains("Signature:")) {
			balance = pubKey; 
		    }
		}

		// Cleans up the process and returns the transaction signature.
		p.destroyForcibly();
		return balance;
	    } catch (Exception e) {
		// Returns an error message if an exception occurs.
		return "ERROR";
	    }
	}
}
