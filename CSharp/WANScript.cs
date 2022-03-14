// See https://aka.ms/new-console-template for more information

using System.Text.RegularExpressions;

string filename = "result.txt";

StreamWriter fileOut = new StreamWriter(filename);


if (args.Length == 1)
{
    filename = args[0];
}

List<string>? SSIDs = getSSID();
Dictionary<string, string> map = new Dictionary<string, string>();

foreach (string SSID in SSIDs)
{
    map.Add(SSID, "");
}

Console.WriteLine("Gettin passwords");

getPassword(ref map);


foreach (KeyValuePair<string, string> kvp in map)
{
    fileOut.WriteLine($"SSID:\t{kvp.Key}\nKey :\t{kvp.Value}\n");
    Console.WriteLine($"SSID:\t{kvp.Key}\nKey :\t{kvp.Value}\n");

}

fileOut.Close();







void getPassword(ref Dictionary<string, string> map)
{
    foreach (string? key in map.Keys)
    {

        System.Diagnostics.Process process = new System.Diagnostics.Process();
        System.Diagnostics.ProcessStartInfo startInfo = new System.Diagnostics.ProcessStartInfo
        {
            WindowStyle = System.Diagnostics.ProcessWindowStyle.Hidden,
            FileName = "cmd.exe",
            Arguments = $"/c netsh wlan show profile \"{key}\" key=clear",
            RedirectStandardOutput = true
        };
        process.StartInfo = startInfo;
        process.Start();

        StreamReader outputStream = process.StandardOutput;
        int nLine = 0;
        string? line = "";
        string pwd = "";

        while ((line = outputStream.ReadLine()) != null && nLine <= 33)
        {
            if (nLine == 33 && line.Contains("Contenuto chiave"))
            {
                pwd = line.Substring(34);
            }
            nLine++;
        }
        //Console.WriteLine($"Key:{key} Pwd:{pwd}");
        map[key] = pwd;
        outputStream.Close();
    }
}

List<string> getSSID()
{
    List<string> ssidList = new List<string>(20);

    System.Diagnostics.Process process = new System.Diagnostics.Process();
    System.Diagnostics.ProcessStartInfo startInfo = new System.Diagnostics.ProcessStartInfo
    {
        WindowStyle = System.Diagnostics.ProcessWindowStyle.Hidden,
        FileName = "cmd.exe",
        Arguments = "/c netsh wlan show profile",
        RedirectStandardOutput = true
    };

    process.StartInfo = startInfo;
    process.Start();
    StreamReader outputStream = process.StandardOutput;

    string pattern = @": (.+)";
    RegexOptions options = RegexOptions.Multiline;
    Regex r = new Regex(pattern, options);
    string? toCheck = "";
    while ((toCheck = outputStream.ReadLine()) != null)
    {
        //Console.WriteLine(toCheck);

        Match m = r.Match(toCheck);
        while (m.Success)
        {
            //Console.WriteLine(m.Groups[1].Value);
            ssidList.Add(m.Groups[1].Value);
            m = m.NextMatch();
        }

    }


    return ssidList;
}

