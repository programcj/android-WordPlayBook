#include "cjudpproto.h"
#include <QJsonDocument>
#include <QJsonObject>

/*static class UDPInfo {
        public String type;
        public String wordClass;
        public String wordName;
        public String wordContext;
    }
*/

#define PORT_DEVICE 6589

quint32 IPV4StringToInteger(const QString& ip){
    QStringList ips = ip.split(".");
    if(ips.size() == 4){
        return ips.at(3).toInt()
                | ips.at(2).toInt() << 8
                                       | ips.at(1).toInt() << 16
                                       | ips.at(0).toInt() << 24;
    }
    return 0;
}

QString CJUDPProto::buildUdpInfo(const QString &type, CJUDPProto::UDPInfo &info)
{
    QJsonObject json;
    json["type"]=type;

    QJsonObject titleObj;
    titleObj["name"]=info.wordTitle.name;
    json["wordTitle"]=titleObj;

    QJsonObject wordObj;
    wordObj["id"]=info.wordItem.id;
    wordObj["name"]=info.wordItem.name;
    json["wordItem"]=wordObj;

    return QString(QJsonDocument(json).toJson());
}

bool CJUDPProto::parseUdpInfo(const QString &jsonStr, CJUDPProto::UDPInfo &info)
{
    QJsonParseError error;
    QJsonDocument jsonDocument = QJsonDocument::fromJson(jsonStr.toLocal8Bit(), &error);

    if (error.error != QJsonParseError::NoError){
        qDebug()<<error.errorString();
        return false;
    }

    if( jsonDocument.isNull() ){
        qDebug() <<"is null";
        return false;
    }
    QJsonObject jsonObject = jsonDocument.object();
    if(jsonObject.contains("type")){
        info.type=jsonObject.take("type").toString();
    }

    if(jsonObject.contains("wordTitle")){

        if(jsonObject["wordTitle"].isObject() ){
            QJsonObject item=jsonObject["wordTitle"].toObject();
            if(item.contains("id")) {
                info.wordTitle.id=item["id"].toInt();
            }
            if(item.contains("name")) {
                info.wordTitle.name=item["name"].toString();
            }
            if(item.contains("seqNumber")) {
                info.wordTitle.seqNumber=item["seqNumber"].toInt();
            }
        }
    }

    if(jsonObject.contains("wordItem")){
        if(jsonObject["wordItem"].isObject() ){
            QJsonObject item=jsonObject["wordItem"].toObject();
            if(item.contains("id")) {
                info.wordItem.id=item["id"].toInt();
            }
            if(item.contains("pid")) {
                info.wordItem.pid=item["pid"].toInt();
            }
            if(item.contains("name")) {
                info.wordItem.name=item["name"].toString();
            }
            if(item.contains("content")) {
                info.wordItem.name=item["content"].toString();
            }
        }
    }

    return true;
}

CJUDPProto::CJUDPProto(QObject *parent) : QObject(parent)
{
    this->tUdpSocket = new QUdpSocket;
    this->tUdpSocket->bind(63782);
    connect(tUdpSocket, SIGNAL(readyRead()), this, SLOT(onReceive()));
}

CJUDPProto::~CJUDPProto()
{
    if(tUdpSocket!=nullptr){
        delete tUdpSocket;
    }
}

void CJUDPProto::sendBroadSearchDevice()
{
    if(tUdpSocket==nullptr)
        return;
    CJUDPProto::UDPInfo info;
    QString data=CJUDPProto::buildUdpInfo("deviceBroadSearch",info);
    QHostAddress address;
    address.setAddress(QHostAddress::Broadcast);
    //address.setAddress("255.255.255.255");//发送者要把数据发送到的ip地址
    tUdpSocket->writeDatagram(data.toLocal8Bit(),address,PORT_DEVICE);
    qDebug()<<"send broad:" <<data;
}

void CJUDPProto::sendGetAllTitle(const QString &ip)
{
    CJUDPProto::UDPInfo info;
    QString data=CJUDPProto::buildUdpInfo("getAllTitleReq",info);
    QHostAddress address;
    address.setAddress(IPV4StringToInteger(ip));
    tUdpSocket->writeDatagram(data.toLocal8Bit(),address,PORT_DEVICE);

    qDebug()<<"send broad ->"<<address << ":" <<data;
}

void CJUDPProto::sendGetAllTitleWordItem(const QString &ip, const QString &title)
{
    CJUDPProto::UDPInfo info;
    info.wordTitle.name=title;
    QString data=CJUDPProto::buildUdpInfo("getAllTitleWordItemReq",info);
    QHostAddress address;
    address.setAddress(IPV4StringToInteger(ip));
    tUdpSocket->writeDatagram(data.toLocal8Bit(),address,PORT_DEVICE);

    qDebug()<<"send broad ->"<<address << ":" <<title<<","<<data;
}

void CJUDPProto::onReceive()
{
    QByteArray ba;
    while(tUdpSocket->hasPendingDatagrams())
    {
        QHostAddress sender;
        quint16 Pic_port;

        ba.resize(tUdpSocket->pendingDatagramSize());
        tUdpSocket->readDatagram(ba.data(), ba.size(), &sender,&Pic_port);
        qDebug()<<"from:"<<sender
               <<"接收到数据:"<< ba.data();

        QString str(ba.data());
        CJUDPProto::UDPInfo info;

        if(!CJUDPProto::parseUdpInfo(str,info))
            break;
        quint32 ip=sender.toIPv4Address();
        QString ipStr= QString("%1.%2.%3.%4")
                .arg((ip >> 24) & 0xFF)
                .arg((ip >> 16) & 0xFF)
                .arg((ip >> 8) & 0xFF)
                .arg(ip & 0xFF);

        QString type=info.type;//.toUpper();

        qDebug()<<type;
        if(type.compare("deviceBroadSearchRes")==0){


            emit this->sigDeviceBroadSearchRes(
                        ipStr,
                        Pic_port);
            break;
        }

        if(type.compare("getAllTitleRes")==0){
            emit this->sigGetAllTitleRes(ipStr,info);
            break;
        }

        if(type.compare("getAllTitleWordItemRes")==0) {
            qDebug()<<"emit sigGetAllTitleWordItemRes";
            emit this->sigGetAllTitleWordItemRes(ipStr,info);
        }
    }
}
