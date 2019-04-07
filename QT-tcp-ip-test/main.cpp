#include "mainwindow.h"
#include <QApplication>
#include "cjapp.h"

int main(int argc, char *argv[])
{
    CJApp a(argc, argv);
    MainWindow w;
    w.show();

    return a.exec();
}
