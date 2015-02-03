#include <stdio.h>
#include <iostream>
#include <sstream>
#include <fstream>
#include <string>
#include <stdlib.h>
#include <vector>

using namespace std;

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

            cout << maskIP(ip, mask) << endl;

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
