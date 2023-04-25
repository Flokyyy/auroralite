package de.flokyy.auroralite.solana;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class NewWallet extends Thread {
	
	/**
 * Constructor for creating a new wallet.
 * @param string a string parameter that is currently unused.
 */
public NewWallet(String string) {
    // TODO Auto-generated constructor stub
}

/**
 * Generates a new Solana wallet for the given user ID.
 * @param userID the ID of the user for whom the wallet is being generated.
 * @return the public key of the newly generated wallet.
 */
public String run(String userID) {
    String s = "";
    Process p;
    String publicKey = "";
    ArrayList list = new ArrayList<>();
    
    try {
        // Executes the command to generate a new Solana wallet with the given user ID.
        p = Runtime.getRuntime().exec("solana-keygen new --no-passphrase --outfile /home/Aurora/wallets/" + userID + ".json");
        
        // Reads the output of the command and stores it in an ArrayList.
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        p.waitFor(1, TimeUnit.MINUTES);
        while ((s = br.readLine()) != null) {
            list.add(s);
        }
        
        // Extracts the public key from the output of the command.
        for (int i = 0; i < list.size(); i++) {
            String pubKey = (String) list.get(i);
            if(pubKey.contains("pubkey:")) {
                publicKey = pubKey; 
            }
        }
        
        // Cleans up the process and stops reading the output.
        p.destroyForcibly();
        stop();
        
        // Parses the public key and returns it.
        String[] parts = publicKey.split(":");
        String part1 = parts[1].replaceAll("\\s+","");
        return part1;
    } catch (Exception e) {
        // Returns an error message if an exception occurs.
        return "ERROR";
    }
  }
}
