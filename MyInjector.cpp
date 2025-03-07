#include <windows.h>
#include <tlhelp32.h>
#include <iostream>
#include <stdio.h>
#include <time.h>

using namespace std;


DWORD getPID(char* processName){
    HANDLE snapshot = CreateToolhelp32Snapshot(TH32CS_SNAPPROCESS, 0);
    if(snapshot == INVALID_HANDLE_VALUE){
        printf("Couldnt get the process' snapshot, proceeding to inject into itself...");
        return 0;
    }
    PROCESSENTRY32 pe32;
    pe32.dwSize = sizeof(PROCESSENTRY32);
    if(!Process32First(snapshot, &pe32)){
        CloseHandle(snapshot);
        return 0;
    }
    do{
        if(!strcmp(pe32.szExeFile, processName)){
            CloseHandle(snapshot);
            return pe32.th32ProcessID;
        }
    }while(Process32Next(snapshot, &pe32));

    return 0;
}


int main(int argc, char** argv){
    srand(time(NULL)); 
    Sleep(rand() % 6000);
    // if(argc != 3){
    //     printf("Usage: %s <process_name> <dll_path>\n", argv[0]);
    // }

    char* processName = argv[1];
    // PCSTR dllPath = argv[2];
    DWORD PID = getPID(processName);
    printf("%d", PID);

}



