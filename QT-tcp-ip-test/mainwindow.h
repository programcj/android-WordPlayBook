#ifndef MAINWINDOW_H
#define MAINWINDOW_H

#include <QListWidgetItem>
#include <QMainWindow>
#include <QHeaderView>

#include "cjapp.h"

namespace Ui {
class MainWindow;
}

class MainWindow : public QMainWindow
{
    Q_OBJECT

public:
    explicit MainWindow(QWidget *parent = nullptr);
    ~MainWindow();

private slots:
    void on_btSearch_clicked();
    void onDeviceBroadSearchRes(QString ip, quint16 port);

    void on_listAllDevWidget_itemClicked(QListWidgetItem *item);
    void onGetAllTitleRes(QString ip, CJUDPProto::UDPInfo & info);

    void on_listWidgetAllTitle_itemDoubleClicked(QListWidgetItem *item);
    void onGetAllTitleWordItemRes(QString ip, CJUDPProto::UDPInfo & info);

    void on_btAddWordItem_clicked();

private:
    Ui::MainWindow *ui;
};

#endif // MAINWINDOW_H
