#include "cjapp.h"

static CJApp *cjAPP=nullptr;

CJApp &CJApp::getInstance()
{
    return *cjAPP;
}

CJApp::CJApp(int &argc, char **argv, int f):QApplication(argc, argv, f)
{
    cjAPP=this;
    cJUDPProto=new CJUDPProto;
}

CJApp::~CJApp()
{
    if(cJUDPProto!=nullptr)
        delete cJUDPProto;
    cjAPP=nullptr;
}
