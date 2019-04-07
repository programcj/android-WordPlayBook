#ifndef CJAPP_H
#define CJAPP_H

#include <QObject>
#include <QApplication>
#include <QWidget>

#include "cjudpproto.h"

class CJApp : public QApplication
{
    Q_OBJECT
public:
    static CJApp &getInstance();

    CJApp(int &argc, char **argv, int f= ApplicationFlags);
    ~CJApp();

    CJUDPProto *cJUDPProto;
};

#endif // CJAPP_H
