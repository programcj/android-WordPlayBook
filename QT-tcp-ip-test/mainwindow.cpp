#include "mainwindow.h"
#include "ui_mainwindow.h"
#include <QMessageBox>

MainWindow::MainWindow(QWidget *parent) :
    QMainWindow(parent),
    ui(new Ui::MainWindow)
{
    ui->setupUi(this);

    connect(CJApp::getInstance().cJUDPProto,SIGNAL(sigDeviceBroadSearchRes(QString,quint16)),
            this, SLOT(onDeviceBroadSearchRes(QString,quint16)));

    connect(CJApp::getInstance().cJUDPProto,SIGNAL( sigGetAllTitleRes(QString , CJUDPProto::UDPInfo &) ),
            this, SLOT(onGetAllTitleRes(QString, CJUDPProto::UDPInfo &)));

    connect(CJApp::getInstance().cJUDPProto, SIGNAL(sigGetAllTitleWordItemRes(QString , CJUDPProto::UDPInfo &) ),
            this, SLOT(onGetAllTitleWordItemRes(QString, CJUDPProto::UDPInfo &)));
}

MainWindow::~MainWindow()
{
    delete ui;
}

void MainWindow::on_btSearch_clicked()
{
    //QMessageBox::information(NULL, "Title", "Content", QMessageBox::Yes | QMessageBox::No, QMessageBox::Yes);
    CJApp::getInstance().cJUDPProto->sendBroadSearchDevice();
    ui->listAllDevWidget->clear();
}

void MainWindow::onDeviceBroadSearchRes(QString ip, quint16 port)
{
    QString txt=QString("%1:%2").arg(ip).arg(port);

    this->ui->listAllDevWidget->addItem(txt);
}




void MainWindow::on_listAllDevWidget_itemClicked(QListWidgetItem *item)
{
    qDebug()<<item->text();
    int end=item->text().indexOf(':');
    QString ip=item->text().mid(0,end);
    this->ui->listWidgetAllTitle->clear();
    CJApp::getInstance().cJUDPProto->sendGetAllTitle(ip);
}

void MainWindow::onGetAllTitleRes(QString ip, CJUDPProto::UDPInfo &info)
{
    qDebug()<<"onGetAllTitleRes:"<<ip;
    this->ui->listWidgetAllTitle->addItem(info.wordTitle.name);
}


void MainWindow::on_listWidgetAllTitle_itemDoubleClicked(QListWidgetItem *item)
{
    ui->tableWidgetWord->clearContents();
    int RowCont;
    RowCont=ui->tableWidgetWord->rowCount();
    for(int i=0;i<RowCont;i++)
        ui->tableWidgetWord->removeRow(0);

    QListWidgetItem *itemTitle=ui->listAllDevWidget->currentItem();

    QString txtIp=itemTitle->text();
    int end=txtIp.indexOf(':');
    QString ip=txtIp.mid(0,end);

    CJApp::getInstance().cJUDPProto->sendGetAllTitleWordItem(ip, item->text());
}

void MainWindow::onGetAllTitleWordItemRes(QString ip, CJUDPProto::UDPInfo &info)
{
    qDebug()<<"onGetAllTitleWordItemRes:"<<ip <<info.wordItem.name;

    int RowCont;
    RowCont=ui->tableWidgetWord->rowCount();
    ui->tableWidgetWord->insertRow(RowCont);//增加一行

    //插入元素
    ui->tableWidgetWord->setItem(RowCont,0,new QTableWidgetItem(info.wordItem.name));

    ui->tableWidgetWord->horizontalHeader()->setSectionResizeMode(0, QHeaderView::ResizeToContents);
    //ui->tableWidgetWord->horizontalHeader()->setResizeMode(QHeaderView::Stretch); //自适应列宽
}



void MainWindow::on_btAddWordItem_clicked()
{
    QListWidgetItem *itemTitle=ui->listAllDevWidget->currentItem();

    QString txtIp=itemTitle->text();
    int end=txtIp.indexOf(':');
    QString ip=txtIp.mid(0,end);

    QString title=ui->listWidgetAllTitle->currentItem()->text();

    CJApp::getInstance().cJUDPProto->sendAddWordItemReq(ip, title,ui->lineEditWord->text());
}
