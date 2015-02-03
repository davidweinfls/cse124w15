#include <stdio.h>
#include <iostream>
#include <sstream>
#include <fstream>
#include <string>
#include <stdlib.h>
#include <vector>
#include <stdint.h>
#include <bitset>
#include <arpa/inet.h>

using namespace std;

#define MASK 0xFFFFFFFF
#define IP_SIZE 32

struct ht {
    string ip;
    int mask;
    string method;
    string domain;
};

int maskIP(string ip, int mask) {
    if (ip == "") {
        cerr << "error when doing maskIP" << endl;
        return -1;
    }

    size_t cur = 0, prev = 0;
    cur = ip.find_first_of('.');
    string s1 = ip.substr(prev, cur - prev);
    prev = cur + 1;

    cur = ip.find_first_of('.', prev);
    string s2 = ip.substr(prev, cur - prev);
    prev = cur + 1;

    cur = ip.find_first_of('.', prev);
    string s3 = ip.substr(prev, cur - prev);
    prev = cur + 1;

    cur = ip.find_first_of('.', prev);
    string s4 = ip.substr(prev, cur - prev);
    prev = cur + 1;

    string temp = s1 + s2 + s3 + s4;
    int ip_int = atoi(temp.c_str());

    return ip_int & mask;
}

bool readHtAccess(vector<ht>& ht_access) {
    ifstream ifs;

    ifs.open(".htaccess", ifstream::in);

    if (ifs.is_open()) {
        cout << "opened .htacces file" << endl;
        string line;

        while (getline(ifs, line)) {
            size_t cur = 0, prev = 0;
            // get method
            cur = line.find_first_of(" ", prev);
            string method = line.substr(prev, cur - prev);
            prev = cur + 1;

            // skip "from"
            cur = line.find_first_of(" ", prev);
            prev = cur + 1;
            
            string ip_mask = line.substr(prev);
            string ip, domain;
            int mask;

            if (ip_mask.find('/') != string::npos) {
                ip = ip_mask.substr(0, ip_mask.find('/'));
                mask = atoi(ip_mask.substr(ip_mask.find('/') + 1).c_str());
            } else {
                domain = ip_mask;
            }

            //cout << maskIP(ip, mask) << endl;

            ht container = {ip, mask, method, domain};
            ht_access.push_back(container);
        }

        ifs.close();
        return true;
    } else {
        cout << "No .htaccess file exists" << endl;
        return false;
    }
}

uint32_t IPToInt(string ip) {
    int a, b, c, d;
    uint32_t addr = 0;

    if (sscanf(ip.c_str(), "%d.%d.%d.%d", &a, &b, &c, &d) != 4)
        return 0;
 
    addr = a << 24;
    addr |= b << 16;
    addr |= c << 8;
    addr |= d;
    return addr;
}

string IntToIP(const uint32_t ip) {
    char dot_ip[] = "";
    sprintf(dot_ip, "%d.%d.%d.%d", (ip>>24)&0xff, (ip>>16)&0xff, (ip>>8)&0xff, ip&0xff); 
    string res (dot_ip);
    return res;
}

void printBinary(const uint32_t ip) {
    bitset<IP_SIZE> res (ip);
    cout << "binary expr: " << res << endl;
}

string generateMaskIP(const string network_ip, int mask) {
    uint32_t ip = IPToInt(network_ip);
    int shift = IP_SIZE - mask;

    uint32_t mask_ip = (0xFFFFFFFF << shift) & ip;
    //printBinary(mask_ip);

    string res = IntToIP(mask_ip);

    return res;
}

uint32_t createUpperBoundMask(const int mask) {
    uint32_t m = 0;
    int shift = IP_SIZE - mask;

    while (shift > 0) {
        m = m << 1;
        m += 1;
        --shift;
    }

    return m;
}

bool isIPInRange(const string ip, const string network_ip, const int mask) {
    uint32_t ip_bi = IPToInt(ip);
    //cout << "ip_bi: ";
    //printBinary(ip_bi);

    uint32_t network_bi = IPToInt(network_ip);
    //cout << "network_bi: ";
    //printBinary(network_bi);
    string mask_ip = generateMaskIP(network_ip, mask);
    uint32_t mask_bi = IPToInt(mask_ip);
    //cout << "mask_bi: ";
    //printBinary(mask_bi);

    uint32_t lower_bound = network_bi & mask_bi;
    uint32_t upper_bound_mask = createUpperBoundMask(mask);
    uint32_t upper_bound = network_bi | upper_bound_mask;

    string l = IntToIP(lower_bound);
    string u = IntToIP(upper_bound);

    //cout << "lower: " << l << endl;
    //cout << "upper: " << u << endl;
    //cout << "~mask_bi: ";
    //printBinary(~mask_bi);

    if (ip_bi >= lower_bound && ip_bi <= upper_bound) {
        return true;
    } else {
        return false;
    }
}

bool checkAccessPermission(const string ip) {
    vector<ht> commands;
    bool access = false;

    if (readHtAccess(commands)) {
        int i = 0;
        for (; i < commands.size(); ++i) {
            string network_ip = commands[i].ip;
            string method = commands[i].method;
            string domain = commands[i].domain;
            int mask = commands[i].mask;

            if (domain.empty()) {
                if (method == "deny") {
                    if (isIPInRange(ip, network_ip, mask)) {
                        access = false;
                        break;
                    }
                } else if (method == "allow") {
                    if (isIPInRange(ip, network_ip, mask)) {
                        access = true;
                        break;
                    }
                }
            } else { // handle domain check

            }
        }
    } else {
        cout << "No need to check .htaccess" << endl;
        access = true;
    }
    return access;
}

/*
int main() {
    //cout << isIPInRange("192.168.0.1", "192.168.0.0", 0) << endl;
    cout << checkAccessPermission("172.22.16.19") << endl;
    return 0;
}
*/
