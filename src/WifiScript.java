
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class WifiScript {
    /**
     * @param args accetta un solo parametro: il nome del file dove salvare i risultati
     * @author Antonio De Luca (antonio-dl)
     * <p>
     * Questo script usa netsh per ottenere tutti gli SSIDs del pc
     * Poi usa netsh per ottenere la password attraverso l' attributo "key=clear"
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        String nameFile = "Wifi Passwords.txt";
        if (args.length == 1)
            nameFile = args[0];

        PrintWriter pw = new PrintWriter(nameFile);
        List<String> SSIDs = getSSIDs();
        List<String> passwords = getPasswords(SSIDs);

        for (String s : passwords) {
            pw.println(s);
        }
        pw.close();


    }

    private static List<String> getSSIDs() throws IOException, InterruptedException {
        String cmd = "cmd /c netsh wlan show profile";
        String netshResult = exec_netsh(cmd);
        //System.out.println(netshResult);
        String regex = ": .+";
        final Pattern pattern =
                Pattern.compile(regex, Pattern.MULTILINE);
        final Matcher matcher = pattern.matcher(netshResult);

        List<String> SSIDs = new ArrayList<>();
        while (matcher.find()) {
            // System.out.println("Full match: " + matcher.group(0).substring(2));
            SSIDs.add(matcher.group(0).substring(2));
        }
        return SSIDs;
    }

    private static List<String> getPasswords(List<String> SSIDs) throws IOException, InterruptedException {
        List<String> password = new ArrayList<>();
        for (String ssid : SSIDs) {
            System.out.println("SSID:" + ssid);
            String cmd = "cmd /c netsh wlan show profile \"" + ssid + "\" key=clear";
            String result = exec_netsh_psw(cmd);
            //System.out.println(result);
            // Ottimizzazione: prendere solo la n-esima riga dove c'e' quello che ci interessa
            String regex = "Contenuto chiave \s+: (.+)";
            final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
            final Matcher matcher = pattern.matcher(result);

            while (matcher.find()) {
                //System.out.println("Full match: " + matcher.group(1));
                password.add("SSID: " + ssid);
                password.add("Key : " + matcher.group(1));
                password.add("");
            }
        }
        return password;
    }

    private static String exec_netsh_psw(String cmd) throws IOException, InterruptedException {
        Runtime run = Runtime.getRuntime();
        Process pr = run.exec(cmd);
        pr.waitFor();
        BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
        String line;
        String netshResult = "";
        int i = 0;
        while ((line = buf.readLine()) != null && i <= 33) {
            // System.out.println(i +") " + line);
            if (i == 33) {
                netshResult = line;
                System.out.println("DEBUG: " + line);
            }
            i++;
        }
        return netshResult;

    }

    private static String exec_netsh(String cmd) throws IOException, InterruptedException {
        Runtime run = Runtime.getRuntime();
        Process pr = run.exec(cmd);
        pr.waitFor();
        BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
        String line;
        StringBuilder netshResult = new StringBuilder();

        while ((line = buf.readLine()) != null) {
            netshResult.append(line).append("\n");
            // System.out.println(i + ") " + line);
        }
        return netshResult.toString();

    }
}
