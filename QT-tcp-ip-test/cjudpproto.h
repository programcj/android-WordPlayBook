#ifndef CJUDPPROTO_H
#define CJUDPPROTO_H

#include <QtCore>
#include <QObject>
#include <QUdpSocket>

class CJUDPProto : public QObject
{
    Q_OBJECT
public:

    class WordTitle {
    public:
        int id;
        QString name;
        int seqNumber;
        int timeCreate;
    };

    class WordItem {
    public:
        int id;
        int pid;
        QString name;
        QString content;
    };

    class UDPInfo {
    public:
        QString type;
        WordTitle wordTitle;
        WordItem wordItem;
    };

    static QString buildUdpInfo(const QString &type, CJUDPProto::UDPInfo &info);

    static bool parseUdpInfo(const QString &jsonStr, UDPInfo &info);

    explicit CJUDPProto(QObject *parent = nullptr);

    ~CJUDPProto();

    void sendBroadSearchDevice();
    void sendGetAllTitle(const QString &ip);

    void sendGetAllTitleWordItem(const QString &ip,const QString &title);

signals:
    void sigDeviceBroadSearchRes(QString ip, quint16 port);
    void sigGetAllTitleRes(QString ip, CJUDPProto::UDPInfo &info);
    void sigGetAllTitleWordItemRes(QString ip,  CJUDPProto::UDPInfo &info);

public slots:
    void onReceive();

private:
    QUdpSocket *tUdpSocket;

};

#endif // CJUDPPROTO_H
